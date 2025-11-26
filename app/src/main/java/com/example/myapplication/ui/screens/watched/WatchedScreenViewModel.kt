package com.example.myapplication.ui.screens.watched

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FavoritesRepository
import com.example.myapplication.data.repositories.RatingRepository
import com.example.myapplication.data.repositories.WatchedRepository
import com.example.myapplication.ui.screens.favorites.FavoritesItem
import com.example.myapplication.ui.screens.ratings.RatingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WatchedScreenState(
    val watchedItems: List<WatchedItem> = emptyList(),
    val favorites: List<FavoritesItem> = emptyList(),
    val ratings: List<RatingItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class WatchedScreenViewModel(
    private val watchedRepository: WatchedRepository = WatchedRepository(),
    private val favoritesRepository: FavoritesRepository = FavoritesRepository(),
    private val ratingRepository: RatingRepository = RatingRepository(),
    private val userId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchedScreenState())
    val uiState: StateFlow<WatchedScreenState> = _uiState.asStateFlow()

    init {
        loadScreenData()
    }

    fun loadScreenData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val watched = watchedRepository.getWatched(userId)
                val favorites = favoritesRepository.getFavorites(userId)
                val ratings = ratingRepository.getRatings(userId)
                
                _uiState.value = _uiState.value.copy(
                    watchedItems = watched,
                    favorites = favorites,
                    ratings = ratings,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load data"
                )
            }
        }
    }

    fun removeFromWatched(movieId: String) {
        viewModelScope.launch {
            try {
                watchedRepository.removeWatched(movieId)
                _uiState.value = _uiState.value.copy(
                    watchedItems = _uiState.value.watchedItems.filter { it.movieId != movieId }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to remove from watched"
                )
            }
        }
    }

    fun isFavorite(movieId: String): Boolean {
        return _uiState.value.favorites.any { it.movieId == movieId }
    }

    fun getRating(movieId: String): Float? {
        return _uiState.value.ratings.find { it.movieId == movieId }?.rating
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

class WatchedScreenViewModelFactory(
    private val userId: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WatchedScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WatchedScreenViewModel(userId = userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

