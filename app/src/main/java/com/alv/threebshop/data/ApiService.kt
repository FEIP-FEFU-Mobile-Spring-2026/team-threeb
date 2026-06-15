package com.alv.threebshop.data

import com.alv.threebshop.models.ApiCatalogResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {
    @GET("catalog")
    suspend fun getCatalog(
        @Header("Authorization") token: String
    ): ApiCatalogResponse
}