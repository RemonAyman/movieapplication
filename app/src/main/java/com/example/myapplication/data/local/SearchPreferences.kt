package com.example.myapplication.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "search_prefs",
        Context.MODE_PRIVATE
    )

    private val gson = Gson()
    private val RECENT_SEARCHES_KEY = "recent_searches"

    fun saveRecentSearches(searches: List<String>) {
        val json = gson.toJson(searches)
        prefs.edit().putString(RECENT_SEARCHES_KEY, json).apply()
    }

    fun getRecentSearches(): List<String> {
        val json = prefs.getString(RECENT_SEARCHES_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearRecentSearches() {
        prefs.edit().remove(RECENT_SEARCHES_KEY).apply()
    }
}