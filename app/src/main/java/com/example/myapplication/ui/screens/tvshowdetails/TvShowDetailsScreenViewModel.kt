package com.example.myapplication.ui.screens.tvshowdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TvShowDetailsScreenState(
    val tvShow: TvShowDetailsApiModel? = null,
    val selectedSeason: SeasonDetailsApiModel? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class TvShowDetailsScreenViewModel(
    private val tvShowId: Int,
    private val apiService: MovieApiService = MovieApiService.create()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TvShowDetailsScreenState())
    val uiState: StateFlow<TvShowDetailsScreenState> = _uiState.asStateFlow()

    init {
        loadTvShowDetails()
    }

    private fun loadTvShowDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val tvShow = apiService.getTvShowDetails(tvShowId)
                _uiState.value = _uiState.value.copy(
                    tvShow = tvShow,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load TV show details"
                )
            }
        }
    }

    fun loadSeasonDetails(seasonNumber: Int) {
        viewModelScope.launch {
            try {
                val season = apiService.getSeasonDetails(tvShowId, seasonNumber)
                _uiState.value = _uiState.value.copy(selectedSeason = season)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load season details"
                )
            }
        }
    }

    fun clearSelectedSeason() {
        _uiState.value = _uiState.value.copy(selectedSeason = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

class TvShowDetailsScreenViewModelFactory(
    private val tvShowId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TvShowDetailsScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TvShowDetailsScreenViewModel(tvShowId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}