package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FavoritesRepository
import com.example.myapplication.ui.screens.favorites.FavoritesItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: FavoritesRepository = FavoritesRepository()
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<FavoritesItem>>(emptyList())
    val favorites: StateFlow<List<FavoritesItem>> = _favorites

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _favorites.value = repository.getFavorites()
        }
    }

    fun addToFavorites(item: FavoritesItem) {
        viewModelScope.launch {
            repository.addFavorite(item)
            _favorites.value = _favorites.value + item
        }
    }

    fun removeFromFavorites(movieId: String) {
        viewModelScope.launch {
            repository.removeFavorite(movieId)
            _favorites.value = _favorites.value.filter { it.movieId != movieId }
        }
    }
}