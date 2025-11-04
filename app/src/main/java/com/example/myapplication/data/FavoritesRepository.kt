package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.data.remote.MovieApiModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ✅ لازم تكون خارج أي كلاس
val Context.favoriteDataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites_prefs")

class FavoritesRepository(private val context: Context) {

    private val gson = Gson()
    private val FAVORITES_KEY = stringSetPreferencesKey("favorites_movies")

    // ✅ استرجاع المفضلات
    val favoritesFlow: Flow<List<MovieApiModel>> = context.favoriteDataStore.data.map { prefs ->
        val set = prefs[FAVORITES_KEY] ?: emptySet()
        set.map { gson.fromJson(it, MovieApiModel::class.java) }
    }

    // ✅ حفظ المفضلات
    suspend fun saveFavorites(favorites: List<MovieApiModel>) {
        val set = favorites.map { gson.toJson(it) }.toSet()
        context.favoriteDataStore.edit { prefs ->
            prefs[FAVORITES_KEY] = set
        }
    }
}
