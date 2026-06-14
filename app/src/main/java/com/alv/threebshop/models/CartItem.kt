package com.alv.threebshop.models

data class CartItem(
    val productId: String,
    val sizeId: String,
    val quantity: Int = 1
)