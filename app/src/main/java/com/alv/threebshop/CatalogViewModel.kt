package com.alv.threebshop

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alv.threebshop.data.RetrofitClient
import com.alv.threebshop.models.Product
import com.alv.threebshop.models.Size
import kotlinx.coroutines.launch

class CatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.apiService
    private val authToken = "Bearer Cmt7wdwFgDIi1_SRX8hlJIExs0jJKPr4axflLpExAxM"
    var uiState: CatalogUiState by mutableStateOf(CatalogUiState())
        private set

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            try {
                val response = apiService.getCatalog(authToken)

                // Маппинг категорий
                val categoryNames = response.categories.associate { it.id to it.name }

                // Маппинг ProductDto → Product
                val products = response.items.map { dto ->
                    Product(
                        id = dto.id,
                        name = dto.name,
                        priceInKopecks = dto.priceInKopecks,
                        imageUrl = dto.imageUrl,
                        category = categoryNames[dto.categoryId] ?: "Неизвестно",  // ← Маппим categoryId → category
                        tags = dto.tags,
                        longDescription = dto.longDescription,
                        sizes = dto.sizes.map { Size(it.id, it.name) },
                        material = dto.material,
                        weight = dto.weight,
                        season = dto.season,
                        countryOfOrigin = dto.countryOfOrigin
                    )
                }

                // Формируем список категорий
                val distinctCategories = products.map { it.category }.distinct()
                val hasNewItems = products.any { "New" in it.tags }

                val categories = mutableListOf<String>()
                if (hasNewItems) categories.add("Новинки")
                categories.addAll(distinctCategories.filter { it != "Неизвестно" })

                uiState = uiState.copy(
                    products = products,
                    categories = categories,
                    selectedCategory = categories.firstOrNull() ?: "",
                    isLoading = false,
                    error = null
                )

                println("✅ Загружено ${products.size} товаров из API")

            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Не удалось загрузить товары: ${e.message}"
                )
                println("❌ Ошибка загрузки из API: ${e.message}")
                e.printStackTrace()
            }
        }
    }    fun selectCategory(category: String) {
        uiState = uiState.copy(selectedCategory = category)
    }

    fun getFilteredProducts(): List<Product> {
        return if (uiState.selectedCategory == "Новинки") {
            uiState.products.filter { "New" in it.tags }
        } else {
            uiState.products.filter { it.category == uiState.selectedCategory }
        }
    }
    fun retryLoad() {
        loadProducts()
    }
}



data class CatalogUiState(
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)