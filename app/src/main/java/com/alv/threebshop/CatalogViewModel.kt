package com.alv.threebshop

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alv.threebshop.models.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import com.alv.threebshop.data.RetrofitClient

private val apiService = RetrofitClient.apiService
private val authToken = "Bearer Cmt7wdwFgDIi1_SRX8hlJIExs0jJKPr4axflLpExAxM"

var uiState: CatalogUiState by mutableStateOf(CatalogUiState())
    private set


data class CatalogUiState(
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "Новинки",
    val isLoading: Boolean = false,
    val error: String? = null
)

class CatalogViewModel(application: Application) : AndroidViewModel(application) {

    var uiState: CatalogUiState by mutableStateOf(CatalogUiState())
        private set

    init {
        loadProducts()
    }
    val categoryNames = mapOf(
        "cat_jeans" to "Джинсы",
        "cat_tshirts" to "Футболки",
        "cat_shirts" to "Рубашки",
        "cat_shoes" to "Обувь",
        "cat_outerwear" to "Верхняя одежда"
    )

    init {
        loadProducts()
    }
    fun retryLoad() {
        loadProducts()
    }
    private fun loadProducts() {
        viewModelScope.launch {
            try {
                val jsonString = withContext(Dispatchers.IO) {
                    getApplication<Application>().assets.open("products.json").use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).readText()
                    }
                }

                // 1. Парсим как объект (не массив!)
                val jsonObject = org.json.JSONObject(jsonString)

                // 2. Берём массив items
                val itemsArray = jsonObject.getJSONArray("items")

                // 👇 Маппинг ID категорий → человекочитаемые названия 👇
                val categoryNames = mapOf(
                    "cat_jeans" to "Джинсы",
                    "cat_tshirts" to "Футболки",
                    "cat_shirts" to "Рубашки",
                    "cat_shoes" to "Обувь",
                    "cat_outerwear" to "Верхняя одежда"
                )

                val products = mutableListOf<Product>()

                for (i in 0 until itemsArray.length()) {
                    val item = itemsArray.getJSONObject(i)

                    // Парсим теги
                    val tags = mutableListOf<String>()
                    if (item.has("tags")) {
                        val tagsArray = item.getJSONArray("tags")
                        for (j in 0 until tagsArray.length()) {
                            tags.add(tagsArray.getString(j))
                        }
                    }

                    products.add(
                        Product(
                            id = item.getString("id"),
                            name = item.getString("name"),
                            priceInKopecks = item.getInt("priceInKopecks"),
                            imageUrl = item.getString("imageUrl"),
                            // 👇 Используем маппинг 👇
                            category = categoryNames[item.getString("categoryId")] ?: "Неизвестно",
                            tags = tags
                        )
                    )
                }

                // 👇👇👇 САМОЕ ВАЖНОЕ: формируем категории и обновляем состояние 👇👇👇

                // 3. Формируем список категорий
                val distinctCategories = products.map { it.category }.distinct()
                val hasNewItems = products.any { "New" in it.tags }

                val categories = mutableListOf<String>()
                if (hasNewItems) categories.add("Новинки")  // Новинки всегда первые
                categories.addAll(distinctCategories.filter { it != "Неизвестно" })

                // 4. Обновляем uiState — без этого UI не увидит данные!
                uiState = uiState.copy(
                    products = products,
                    categories = categories,
                    selectedCategory = if (categories.isNotEmpty()) categories[0] else ""
                )

                // Для отладки (можно удалить потом)
                println("✅ Загружено товаров: ${products.size}")
                println("✅ Категории: $categories")

            } catch (e: Exception) {
                println("❌ Ошибка загрузки JSON: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    fun selectCategory(category: String) {
        uiState = uiState.copy(selectedCategory = category)
    }

    fun getFilteredProducts(): List<Product> {
        return when (uiState.selectedCategory) {
            "Новинки" -> uiState.products.filter { "New" in it.tags }
            else -> uiState.products.filter { it.category == uiState.selectedCategory }
        }
    }
}