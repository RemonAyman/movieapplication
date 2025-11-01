package com.example.myapplication.data

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.remote.MovieApiModel

class FavoritesViewModel : ViewModel() {
    var favorites = mutableStateListOf<MovieApiModel>()
        private set

    fun addToFavorites(movie: MovieApiModel) {
        if (!favorites.contains(movie)) favorites.add(movie)
    }

    fun removeFromFavorites(movie: MovieApiModel) {
        favorites.remove(movie)
    }
}
