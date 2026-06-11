package com.alv.threebshop.models

data class Product(
    val id: String,
    val name: String,
    val priceInKopecks: Int,
    val imageUrl: String,
    val category: String,
    val tags: List<String> = emptyList(),
    val longDescription: String = "",
    val sizes: List<Size> = emptyList(),
    val material: String? = null,
    val weight: String? = null,
    val season: String? = null,
    val countryOfOrigin: String? = null
)

data class Size(
    val id: String,
    val name: String
)