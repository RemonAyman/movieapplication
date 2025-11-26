package com.example.myapplication.ui.screens.ratings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FavoritesRepository
import com.example.myapplication.data.repositories.RatingRepository
import com.example.myapplication.ui.screens.favorites.FavoritesItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RatingsScreenState(
    val ratings: List<RatingItem> = emptyList(),
    val favorites: List<FavoritesItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class RatingsScreenViewModel(
    private val ratingRepository: RatingRepository = RatingRepository(),
    private val favoritesRepository: FavoritesRepository = FavoritesRepository(),
    private val userId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(RatingsScreenState())
    val uiState: StateFlow<RatingsScreenState> = _uiState.asStateFlow()

    init {
        loadScreenData()
    }

    fun loadScreenData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val ratings = ratingRepository.getRatings(userId)
                val favorites = favoritesRepository.getFavorites(userId)
                
                _uiState.value = _uiState.value.copy(
                    ratings = ratings,
                    favorites = favorites,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load ratings"
                )
            }
        }
    }

    fun removeRating(movieId: String) {
        viewModelScope.launch {
            try {
                ratingRepository.removeRating(movieId)
                _uiState.value = _uiState.value.copy(
                    ratings = _uiState.value.ratings.filter { it.movieId != movieId }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to remove rating"
                )
            }
        }
    }

    fun isFavorite(movieId: String): Boolean {
        return _uiState.value.favorites.any { it.movieId == movieId }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

class RatingsScreenViewModelFactory(
    private val userId: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RatingsScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RatingsScreenViewModel(userId = userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

