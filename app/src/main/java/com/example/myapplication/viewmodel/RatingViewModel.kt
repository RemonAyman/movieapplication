package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repositories.RatingRepository
import com.example.myapplication.ui.screens.ratings.RatingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RatingViewModel(
    private val repository: RatingRepository = RatingRepository(),
    private val userId: String? = null
) : ViewModel() {

    private val _ratings = MutableStateFlow<List<RatingItem>>(emptyList())
    val ratings: StateFlow<List<RatingItem>> = _ratings

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    init {
        loadRatings(userId)
    }

    fun loadRatings(userId: String? = null) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                _ratings.value = repository.getRatings(userId ?: this@RatingViewModel.userId)
                _errorState.value = null
            } catch (e: Exception) {
                _ratings.value = emptyList()
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun addRating(item: RatingItem) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                repository.addRating(item)
                loadRatings()
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun updateRating(movieId: String, rating: Float, review: String) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                repository.updateRating(movieId, rating, review)
                loadRatings()
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun removeRating(movieId: String) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                repository.removeRating(movieId)
                _ratings.value = _ratings.value.filter { it.movieId != movieId }
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }

    suspend fun getRating(movieId: String): RatingItem? {
        return try {
            repository.getRating(movieId)
        } catch (e: Exception) {
            null
        }
    }
}

class RatingViewModelFactory(
    private val userId: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RatingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RatingViewModel(userId = userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}