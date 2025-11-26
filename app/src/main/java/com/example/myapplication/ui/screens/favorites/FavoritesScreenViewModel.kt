package com.example.myapplication.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FavoritesRepository
import com.example.myapplication.data.repositories.RatingRepository
import com.example.myapplication.ui.screens.ratings.RatingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FavoritesScreenState(
    val favorites: List<FavoritesItem> = emptyList(),
    val ratings: List<RatingItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FavoritesScreenViewModel(
    private val favoritesRepository: FavoritesRepository = FavoritesRepository(),
    private val ratingRepository: RatingRepository = RatingRepository(),
    private val userId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesScreenState())
    val uiState: StateFlow<FavoritesScreenState> = _uiState.asStateFlow()

    init {
        loadScreenData(userId)
    }

    // ✅ تحميل كل البيانات المطلوبة للشاشة (Favorites + Ratings)
    fun loadScreenData(userId: String? = this.userId) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val favorites = favoritesRepository.getFavorites(userId)
                val ratings = ratingRepository.getRatings(userId)
                _uiState.value = _uiState.value.copy(
                    favorites = favorites,
                    ratings = ratings,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "حدث خطأ غير متوقع"
                )
            }
        }
    }

    // ✅ حذف فيلم من المفضلة
    fun removeFromFavorites(movieId: String) {
        viewModelScope.launch {
            try {
                favoritesRepository.removeFavorite(movieId, userId)
                _uiState.value = _uiState.value.copy(
                    favorites = _uiState.value.favorites.filter { it.movieId != movieId }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "فشل في حذف الفيلم من المفضلة"
                )
            }
        }
    }

    // ✅ الحصول على تقييم فيلم معين
    fun getRatingForMovie(movieId: String): Float? {
        return _uiState.value.ratings.find { it.movieId == movieId }?.rating
    }

    // ✅ مسح رسالة الخطأ
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

class FavoritesScreenViewModelFactory(
    private val userId: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesScreenViewModel(userId = userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

