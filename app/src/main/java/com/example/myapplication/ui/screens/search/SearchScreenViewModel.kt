package com.example.myapplication.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.MovieApiModel
import com.example.myapplication.data.remote.MovieApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchScreenState(
    val query: String = "",
    val searchResults: List<MovieApiModel> = emptyList(),
    val recentSearches: List<String> = listOf("Action", "Comedy", "Drama", "Sci-Fi"),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SearchScreenViewModel(
    private val apiService: MovieApiService = MovieApiService.create()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchScreenState())
    val uiState: StateFlow<SearchScreenState> = _uiState.asStateFlow()

    fun updateQuery(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
    }

    fun search(query: String = _uiState.value.query) {
        if (query.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = apiService.searchMovies(query)
                val currentRecentSearches = _uiState.value.recentSearches.toMutableList()
                if (!currentRecentSearches.contains(query)) {
                    currentRecentSearches.add(0, query)
                }
                _uiState.value = _uiState.value.copy(
                    searchResults = response.results,
                    recentSearches = currentRecentSearches,
                    isLoading = false,
                    query = query
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Search failed"
                )
            }
        }
    }

    fun searchByVoice(spokenText: String) {
        if (spokenText.isEmpty()) return
        _uiState.value = _uiState.value.copy(query = spokenText)
        search(spokenText)
    }

    fun clearResults() {
        _uiState.value = _uiState.value.copy(
            searchResults = emptyList(),
            query = ""
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

