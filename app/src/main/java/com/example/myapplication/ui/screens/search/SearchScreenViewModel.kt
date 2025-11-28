package com.example.myapplication.ui.screens.search

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.SearchPreferences
import com.example.myapplication.data.remote.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SearchCategory {
    MOVIES, TV_SHOWS, ACTORS
}

data class SearchScreenState(
    val query: String = "",
    val selectedCategory: SearchCategory = SearchCategory.MOVIES,
    val movieResults: List<MovieApiModel> = emptyList(),
    val tvShowResults: List<TvShowApiModel> = emptyList(),
    val actorResults: List<ActorSearchResult> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SearchScreenViewModel(
    private val apiService: MovieApiService = MovieApiService.create(),
    private var searchPreferences: SearchPreferences? = null  // ✅ هنا
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchScreenState())
    val uiState: StateFlow<SearchScreenState> = _uiState.asStateFlow()

    // 📝 تهيئة SearchPreferences من الـ Screen
    fun initPreferences(context: Context) {
        if (searchPreferences == null) {
            searchPreferences = SearchPreferences(context)
            loadRecentSearches()
        }
    }

    fun updateQuery(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
    }

    fun selectCategory(category: SearchCategory) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            movieResults = emptyList(),
            tvShowResults = emptyList(),
            actorResults = emptyList()
        )
    }

    fun search(query: String = _uiState.value.query) {
        if (query.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                when (_uiState.value.selectedCategory) {
                    SearchCategory.MOVIES -> {
                        val response = apiService.searchMovies(query)
                        _uiState.value = _uiState.value.copy(
                            movieResults = response.results,
                            isLoading = false
                        )
                    }
                    SearchCategory.TV_SHOWS -> {
                        val response = apiService.searchTvShows(query)
                        _uiState.value = _uiState.value.copy(
                            tvShowResults = response.results,
                            isLoading = false
                        )
                    }
                    SearchCategory.ACTORS -> {
                        val response = apiService.searchActors(query)
                        _uiState.value = _uiState.value.copy(
                            actorResults = response.results,
                            isLoading = false
                        )
                    }
                }
                saveRecentSearch(query)
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

    // ✅ حفظ البحث الأخير
    private fun saveRecentSearch(query: String) {
        val currentList = _uiState.value.recentSearches.toMutableList()
        currentList.remove(query) // إزالة لو موجود
        currentList.add(0, query) // إضافة في البداية
        if (currentList.size > 10) currentList.removeAt(currentList.size - 1) // حد أقصى 10

        _uiState.value = _uiState.value.copy(recentSearches = currentList)
        searchPreferences?.saveRecentSearches(currentList)
    }

    // ✅ تحميل Recent Searches من SharedPreferences
    private fun loadRecentSearches() {
        val savedSearches = searchPreferences?.getRecentSearches() ?: emptyList()
        _uiState.value = _uiState.value.copy(recentSearches = savedSearches)
    }

    fun clearResults() {
        _uiState.value = _uiState.value.copy(
            movieResults = emptyList(),
            tvShowResults = emptyList(),
            actorResults = emptyList(),
            query = ""
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}