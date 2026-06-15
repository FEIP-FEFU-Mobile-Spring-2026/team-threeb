package com.alv.threebshop

data class Product(
    val id: String,
    val name: String,
    val priceInKopecks: Int,
    val imageUrl: String,
    val category: String,
    val tags: List<String> = emptyList(),
    val longDescription: String = ""
)