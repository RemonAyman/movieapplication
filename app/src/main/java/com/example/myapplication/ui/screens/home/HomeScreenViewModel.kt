package com.example.myapplication.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.MoviesRepository
import com.example.myapplication.data.remote.MovieApiModel
import com.example.myapplication.data.remote.MovieApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeScreenState(
    val popularMovies: List<MovieApiModel> = emptyList(),
    val upcomingMovies: List<MovieApiModel> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    // Filtered categories
    val actionMovies: List<MovieApiModel> get() = popularMovies.filter { it.genre_ids.contains(28) }
    val comedyMovies: List<MovieApiModel> get() = popularMovies.filter { it.genre_ids.contains(35) }
    val romanceMovies: List<MovieApiModel> get() = popularMovies.filter { it.genre_ids.contains(10749) }
    val cartoonMovies: List<MovieApiModel> get() = popularMovies.filter { it.genre_ids.contains(16) }
    val animeMovies: List<MovieApiModel> get() = popularMovies.filter {
        it.genre_ids.contains(16) && it.title.contains("Anime", ignoreCase = true)
    }
    val arabicMovies: List<MovieApiModel> get() = (popularMovies + upcomingMovies).filter {
        it.original_language == "ar"
    }
}

class HomeScreenViewModel(
    private val repository: MoviesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenState())
    val uiState: StateFlow<HomeScreenState> = _uiState.asStateFlow()

    init {
        loadAllMovies()
    }

    fun loadAllMovies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val popular = repository.getPopular100Movies()
                val upcoming = repository.getUpcomingMovies()
                _uiState.value = _uiState.value.copy(
                    popularMovies = popular,
                    upcomingMovies = upcoming,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Unknown error"
                )
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            try {
                val popular = repository.getPopular100Movies()
                val upcoming = repository.getUpcomingMovies()
                _uiState.value = _uiState.value.copy(
                    popularMovies = popular,
                    upcomingMovies = upcoming,
                    isRefreshing = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = e.localizedMessage ?: "Unknown error"
                )
            }
        }
    }

    fun loadPopularMovies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val popular = repository.getPopular100Movies()
                _uiState.value = _uiState.value.copy(
                    popularMovies = popular,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage
                )
            }
        }
    }

    fun loadUpcomingMovies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val upcoming = repository.getUpcomingMovies()
                _uiState.value = _uiState.value.copy(
                    upcomingMovies = upcoming,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

class HomeScreenViewModelFactory(
    private val repository: MoviesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeScreenViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
