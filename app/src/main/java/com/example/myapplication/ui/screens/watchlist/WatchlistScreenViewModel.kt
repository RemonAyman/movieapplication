package com.example.myapplication.ui.screens.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.WatchlistRepository
import com.example.myapplication.ui.watchlist.WatchlistItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class WatchlistScreenState(
    val watchlistItems: List<WatchlistItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class WatchlistScreenViewModel(
    private val repository: WatchlistRepository,
    private val userId: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistScreenState())
    val uiState: StateFlow<WatchlistScreenState> = _uiState.asStateFlow()

    init {
        loadWatchlist()
    }

    private fun loadWatchlist() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getWatchlistFlow(userId).collectLatest { list ->
                    _uiState.value = _uiState.value.copy(
                        watchlistItems = list,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load watchlist",
                    isLoading = false
                )
            }
        }
    }

    fun addToWatchlist(movie: WatchlistItem) {
        viewModelScope.launch {
            try {
                repository.addToWatchlist(movie, userId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add to watchlist"
                )
            }
        }
    }

    fun removeFromWatchlist(movieId: String) {
        viewModelScope.launch {
            try {
                repository.removeFromWatchlist(movieId, userId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to remove from watchlist"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

class WatchlistScreenViewModelFactory(
    private val repository: WatchlistRepository,
    private val userId: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WatchlistScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WatchlistScreenViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
