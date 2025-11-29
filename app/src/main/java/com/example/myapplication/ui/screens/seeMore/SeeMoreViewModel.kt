package com.example.myapplication.ui.screens.seeMore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.MovieApiModel
import com.example.myapplication.data.MoviesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SeeMoreUiState(
    val movies: List<MovieApiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMorePages: Boolean = true
)

class SeeMoreMoviesViewModel(
    private val repository: MoviesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeeMoreUiState())
    val uiState: StateFlow<SeeMoreUiState> = _uiState.asStateFlow()

    fun loadMovies(category: String) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val response = when (category) {
                    "topRated" -> repository.getTopRatedMovies(_uiState.value.currentPage)
                    "popular" -> repository.getPopularMovies(_uiState.value.currentPage)
                    "upcoming" -> repository.getUpcomingMovies(_uiState.value.currentPage)
                    "nowPlaying" -> repository.getNowPlayingMovies(_uiState.value.currentPage)
                    "action" -> repository.getMoviesByGenre(28, _uiState.value.currentPage)
                    "comedy" -> repository.getMoviesByGenre(35, _uiState.value.currentPage)
                    "romance" -> repository.getMoviesByGenre(10749, _uiState.value.currentPage)
                    "animation" -> repository.getMoviesByGenre(16, _uiState.value.currentPage)
                    "anime" -> repository.getAnimeMovies(_uiState.value.currentPage)
                    "arabic" -> repository.getArabicMovies(_uiState.value.currentPage)
                    else -> repository.getPopularMovies(_uiState.value.currentPage)
                }

                _uiState.value = _uiState.value.copy(
                    movies = _uiState.value.movies + response.results,
                    isLoading = false,
                    currentPage = _uiState.value.currentPage + 1,
                    hasMorePages = _uiState.value.currentPage < response.total_pages
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun retry(category: String) {
        _uiState.value = SeeMoreUiState()
        loadMovies(category)
    }
}