package com.alv.threebshop.data

import android.content.Context
import android.content.SharedPreferences
import com.alv.threebshop.models.CartItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CartRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val cartKey = "cart_items"

    fun getCartItems(): List<CartItem> {
        val json = prefs.getString(cartKey, null)
        return if (json != null) {
            val type = object : TypeToken<List<CartItem>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun saveCartItems(items: List<CartItem>) {
        val json = gson.toJson(items)
        prefs.edit().putString(cartKey, json).apply()
    }

    fun clearCart() {
        prefs.edit().remove(cartKey).apply()
    }
}