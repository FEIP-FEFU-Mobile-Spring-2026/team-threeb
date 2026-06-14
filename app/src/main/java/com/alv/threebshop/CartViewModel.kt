// app/src/main/java/com/alv/threebshop/CartViewModel.kt
package com.alv.threebshop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alv.threebshop.data.CartRepository
import com.alv.threebshop.models.CartItem
import com.alv.threebshop.models.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val cartRepository = CartRepository(application)

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Общий счетчик товаров для бейджа
    val totalItemsCount: StateFlow<Int> = _cartItems
        .map { items -> items.sumOf { it.quantity } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0
        )
    // Общая стоимость корзины
    fun getTotalPrice(products: List<Product>): Double {
        return _cartItems.value.sumOf { cartItem ->
            val product = products.find { it.id == cartItem.productId }
            if (product != null) {
                (product.priceInKopecks * cartItem.quantity) / 100.0
            } else 0.0
        }
    }

    init {
        loadCart()
    }

    private fun loadCart() {
        _cartItems.value = cartRepository.getCartItems()
    }

    fun addToCart(productId: String, sizeId: String) {
        val currentItems = _cartItems.value.toMutableList()

        // Проверяем, есть ли уже такой товар с таким размером
        val existingIndex = currentItems.indexOfFirst {
            it.productId == productId && it.sizeId == sizeId
        }

        if (existingIndex != -1) {
            // Увеличиваем количество
            currentItems[existingIndex] = currentItems[existingIndex].copy(
                quantity = currentItems[existingIndex].quantity + 1
            )
        } else {
            // Добавляем новый товар
            currentItems.add(CartItem(productId, sizeId, 1))
        }

        _cartItems.value = currentItems
        saveCart()
    }

    fun removeFromCart(productId: String, sizeId: String) {
        val currentItems = _cartItems.value.toMutableList()
        currentItems.removeAll {
            it.productId == productId && it.sizeId == sizeId
        }
        _cartItems.value = currentItems
        saveCart()
    }

    fun updateQuantity(productId: String, sizeId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId, sizeId)
            return
        }

        val currentItems = _cartItems.value.toMutableList()
        val index = currentItems.indexOfFirst {
            it.productId == productId && it.sizeId == sizeId
        }

        if (index != -1) {
            currentItems[index] = currentItems[index].copy(quantity = quantity)
            _cartItems.value = currentItems
            saveCart()
        }
    }

    fun increaseQuantity(productId: String, sizeId: String) {
        val item = _cartItems.value.find {
            it.productId == productId && it.sizeId == sizeId
        }
        if (item != null) {
            updateQuantity(productId, sizeId, item.quantity + 1)
        }
    }

    fun decreaseQuantity(productId: String, sizeId: String) {
        val item = _cartItems.value.find {
            it.productId == productId && it.sizeId == sizeId
        }
        if (item != null) {
            updateQuantity(productId, sizeId, item.quantity - 1)
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        cartRepository.clearCart()
    }

    private fun saveCart() {
        cartRepository.saveCartItems(_cartItems.value)
    }

    fun checkout() {
        clearCart()
    }
}