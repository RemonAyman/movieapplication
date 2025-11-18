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

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                _favorites.value = repository.getFavorites()
                _errorState.value = null
            } catch (e: Exception) {
                _favorites.value = emptyList()
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun addToFavorites(item: FavoritesItem) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                repository.addFavorite(item)
                _favorites.value = _favorites.value + item
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun removeFromFavorites(movieId: String) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                repository.removeFavorite(movieId)
                _favorites.value = _favorites.value.filter { it.movieId != movieId }
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }
}
