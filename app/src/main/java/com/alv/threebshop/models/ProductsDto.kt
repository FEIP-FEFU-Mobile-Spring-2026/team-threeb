package com.alv.threebshop.models

import com.google.gson.annotations.SerializedName

data class ProductsDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("shortDescription") val shortDescription: String,
    @SerializedName("longDescription") val longDescription: String,
    @SerializedName("priceInKopecks") val priceInKopecks: Int,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("categoryId") val categoryId: String,  // ← Вот это поле из JSON
    @SerializedName("tags") val tags: List<String>,
    @SerializedName("sizes") val sizes: List<SizeDto>,
    @SerializedName("material") val material: String?,
    @SerializedName("weight") val weight: String?,
    @SerializedName("season") val season: String?,
    @SerializedName("countryOfOrigin") val countryOfOrigin: String?
)

data class SizeDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)