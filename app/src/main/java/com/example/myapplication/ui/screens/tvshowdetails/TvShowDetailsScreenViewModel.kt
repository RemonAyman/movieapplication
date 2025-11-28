package com.example.myapplication.ui.screens.tvshowdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FavoritesRepository
import com.example.myapplication.data.WatchlistRepository
import com.example.myapplication.data.remote.*
import com.example.myapplication.data.repositories.RatingRepository
import com.example.myapplication.data.repositories.WatchedRepository
import com.example.myapplication.ui.screens.favorites.FavoritesItem
import com.example.myapplication.ui.screens.ratings.RatingItem
import com.example.myapplication.ui.screens.watched.WatchedItem
import com.example.myapplication.ui.watchlist.WatchlistItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class TvShowDetailsScreenState(
    val tvShow: TvShowDetailsApiModel? = null,
    val selectedSeason: SeasonDetailsApiModel? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isInWatchlist: Boolean = false,
    val isWatched: Boolean = false,
    val userRating: Float? = null,
    val isLiked: Boolean = false
)

class TvShowDetailsScreenViewModel(
    private val tvShowId: Int,
    private val apiService: MovieApiService = MovieApiService.create(),
    private val favoritesRepository: FavoritesRepository = FavoritesRepository(),
    private val watchlistRepository: WatchlistRepository = WatchlistRepository(),
    private val watchedRepository: WatchedRepository = WatchedRepository(),
    private val ratingRepository: RatingRepository = RatingRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TvShowDetailsScreenState())
    val uiState: StateFlow<TvShowDetailsScreenState> = _uiState.asStateFlow()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    init {
        loadTvShowDetails()
        checkUserInteractions()
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

    private fun checkUserInteractions() {
        if (currentUserId == null) return
        viewModelScope.launch {
            try {
                // Check Favorites
                val favorites = favoritesRepository.getFavorites(currentUserId)
                val isLiked = favorites.any { it.movieId == "tv_$tvShowId" }

                // Check Watchlist - استخدام first() لأخذ أول قيمة من الـ Flow
                val watchlist = watchlistRepository.getWatchlistFlow(currentUserId).first()
                val isInWatchlist = watchlist.any { it.movieId == "tv_$tvShowId" }

                // Check Watched
                val watched = watchedRepository.getWatched(currentUserId)
                val isWatched = watched.any { it.movieId == "tv_$tvShowId" }

                // Check Rating
                val rating = ratingRepository.getRating("tv_$tvShowId")

                _uiState.value = _uiState.value.copy(
                    isInWatchlist = isInWatchlist,
                    isWatched = isWatched,
                    userRating = rating?.rating,
                    isLiked = isLiked
                )
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }

    fun toggleWatchlist() {
        if (currentUserId == null) return
        val tvShow = _uiState.value.tvShow ?: return

        viewModelScope.launch {
            try {
                val item = WatchlistItem(
                    movieId = "tv_$tvShowId",
                    title = tvShow.name,
                    poster = "https://image.tmdb.org/t/p/w500${tvShow.poster_path}"
                )

                if (_uiState.value.isInWatchlist) {
                    watchlistRepository.removeFromWatchlist("tv_$tvShowId", currentUserId)
                    _uiState.value = _uiState.value.copy(isInWatchlist = false)
                } else {
                    watchlistRepository.addToWatchlist(item, currentUserId)
                    _uiState.value = _uiState.value.copy(isInWatchlist = true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to update watchlist")
            }
        }
    }

    fun toggleWatched() {
        if (currentUserId == null) return
        val tvShow = _uiState.value.tvShow ?: return

        viewModelScope.launch {
            try {
                if (_uiState.value.isWatched) {
                    watchedRepository.removeWatched("tv_$tvShowId")
                    _uiState.value = _uiState.value.copy(isWatched = false)
                } else {
                    watchedRepository.addWatched(
                        WatchedItem(
                            movieId = "tv_$tvShowId",
                            title = tvShow.name,
                            poster = "https://image.tmdb.org/t/p/w500${tvShow.poster_path}",
                            rating = 0,
                            vote_average = (tvShow.vote_average ?: 0.0).toInt()
                        )
                    )
                    _uiState.value = _uiState.value.copy(isWatched = true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to update watched status")
            }
        }
    }

    fun toggleLike() {
        if (currentUserId == null) return
        val tvShow = _uiState.value.tvShow ?: return

        viewModelScope.launch {
            try {
                val item = FavoritesItem(
                    movieId = "tv_$tvShowId",
                    title = tvShow.name,
                    poster = "https://image.tmdb.org/t/p/w500${tvShow.poster_path}"
                )

                if (_uiState.value.isLiked) {
                    favoritesRepository.removeFavorite("tv_$tvShowId", currentUserId)
                    _uiState.value = _uiState.value.copy(isLiked = false)
                } else {
                    favoritesRepository.addFavorite(item, currentUserId)
                    _uiState.value = _uiState.value.copy(isLiked = true)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to update favorites")
            }
        }
    }

    fun rateShow(rating: Float) {
        if (currentUserId == null) return
        val tvShow = _uiState.value.tvShow ?: return

        viewModelScope.launch {
            try {
                ratingRepository.addRating(
                    RatingItem(
                        movieId = "tv_$tvShowId",
                        title = tvShow.name,
                        poster = "https://image.tmdb.org/t/p/w500${tvShow.poster_path}",
                        rating = rating,
                        review = "",
                        vote_average = tvShow.vote_average ?: 0.0
                    )
                )

                _uiState.value = _uiState.value.copy(userRating = rating)

                // Add to watched automatically
                if (!_uiState.value.isWatched) {
                    watchedRepository.addWatched(
                        WatchedItem(
                            movieId = "tv_$tvShowId",
                            title = tvShow.name,
                            poster = "https://image.tmdb.org/t/p/w500${tvShow.poster_path}",
                            rating = 0,
                            vote_average = (tvShow.vote_average ?: 0.0).toInt()
                        )
                    )
                    _uiState.value = _uiState.value.copy(isWatched = true)
                }

                // Remove from watchlist if exists
                if (_uiState.value.isInWatchlist) {
                    watchlistRepository.removeFromWatchlist("tv_$tvShowId", currentUserId)
                    _uiState.value = _uiState.value.copy(isInWatchlist = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to save rating")
            }
        }
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