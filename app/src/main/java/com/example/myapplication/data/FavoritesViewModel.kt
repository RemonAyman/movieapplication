package com.example.myapplication.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.MovieApiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoritesViewModel(private val repository: FavoritesRepository) : ViewModel() {

    private val _favoritesFlow = MutableStateFlow<List<MovieApiModel>>(emptyList())
    val favoritesFlow: StateFlow<List<MovieApiModel>> get() = _favoritesFlow

    init {
        viewModelScope.launch {
            repository.favoritesFlow.collect { list ->
                _favoritesFlow.value = list
            }
        }
    }

    fun addToFavorites(movie: MovieApiModel) {
        val currentList = _favoritesFlow.value.toMutableList()
        if (!currentList.contains(movie)) {
            currentList.add(movie)
            _favoritesFlow.value = currentList
            viewModelScope.launch {
                repository.saveFavorites(currentList)
            }
        }
    }

    fun removeFromFavorites(movie: MovieApiModel) {
        val currentList = _favoritesFlow.value.toMutableList()
        if (currentList.remove(movie)) {
            _favoritesFlow.value = currentList
            viewModelScope.launch {
                repository.saveFavorites(currentList)
            }
        }
    }
}
