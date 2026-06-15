package com.alv.threebshop.models

data class ApiCatalogResponse(
    val items: List<ProductsDto>, // Список товаров
    val categories: List<Category> // Список категорий
)