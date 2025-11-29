package com.example.myapplication.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.MoviesRepository
import com.example.myapplication.data.WatchlistRepository
import com.example.myapplication.data.remote.MovieApiModel
import com.example.myapplication.ui.screens.home.mapper.toMovieApiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeScreenState(
    val popularMovies: List<MovieApiModel> = emptyList(),
    val upcomingMovies: List<MovieApiModel> = emptyList(),
    val topRatedMovies: List<MovieApiModel> = emptyList(),
    val fromYourWatchListMovies: List<MovieApiModel> = emptyList(),
    val arabicMovies: List<MovieApiModel> = emptyList(),

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
}

class HomeScreenViewModel(
    private val moviesRepository: MoviesRepository,
    private val watchlistRepository: WatchlistRepository
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
                val popular = moviesRepository.getPopular100Movies()
                val upcoming = moviesRepository.getUpcomingMovies()
                val topRated = moviesRepository.getTop100Movies()
                val  arabic = moviesRepository.getArabicMoviesForHome()
                val fromYourWatchList = watchlistRepository.getWatchlistFlow().toMovieApiModel()
                _uiState.value = _uiState.value.copy(
                    popularMovies = popular,
                    upcomingMovies = upcoming,
                    fromYourWatchListMovies = fromYourWatchList,
                    arabicMovies = arabic,
                    topRatedMovies = topRated,
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
                val popular = moviesRepository.getPopular100Movies()
                val upcoming = moviesRepository.getUpcomingMovies()
                val topRated = moviesRepository.getTop100Movies()
                val fromYouWatchlist = watchlistRepository.getWatchlistFlow().toMovieApiModel()
                _uiState.value = _uiState.value.copy(
                    popularMovies = popular,
                    upcomingMovies = upcoming,
                    topRatedMovies = topRated,
                    fromYourWatchListMovies = fromYouWatchlist,
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
    fun loadTopRatedMovies(){
        viewModelScope.launch {
            _uiState.value=_uiState.value.copy(isRefreshing = true)
            try {
                val topRated = moviesRepository.getTop100Movies()
                _uiState.value = _uiState.value.copy(topRatedMovies = topRated)
            }catch (e: Exception){
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage
                )            }
        }
    }
    fun loadWatchlistMovies(){
        viewModelScope.launch {
            _uiState.value=_uiState.value.copy(isRefreshing = true)
            try {
                val fromyorWatchlist =watchlistRepository.getWatchlistFlow().toMovieApiModel()
                _uiState.value = _uiState.value.copy(fromYourWatchListMovies = fromyorWatchlist)
            }catch (e: Exception){
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage
                )            }
        }
    }

    fun loadPopularMovies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val popular = moviesRepository.getPopular100Movies()
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
                val upcoming = moviesRepository.getUpcomingMovies()
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
    private val moviesRepository: MoviesRepository,
    private val watchlisRepository: WatchlistRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeScreenViewModel( moviesRepository,watchlisRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
