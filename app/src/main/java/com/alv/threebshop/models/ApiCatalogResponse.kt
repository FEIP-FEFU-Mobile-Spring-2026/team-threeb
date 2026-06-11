package com.alv.threebshop.models

data class ApiCatalogResponse(
    val items: List<Product>, // Список товаров
    val categories: List<Category> // Список категорий
)