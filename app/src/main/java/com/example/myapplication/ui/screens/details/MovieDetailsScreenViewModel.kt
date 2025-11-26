package com.example.myapplication.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FavoritesRepository
import com.example.myapplication.data.remote.CastMember
import com.example.myapplication.data.remote.MovieApiModel
import com.example.myapplication.data.remote.MovieApiService
import com.example.myapplication.data.repositories.RatingRepository
import com.example.myapplication.data.repositories.WatchedRepository
import com.example.myapplication.ui.screens.favorites.FavoritesItem
import com.example.myapplication.ui.screens.ratings.RatingItem
import com.example.myapplication.ui.screens.watched.WatchedItem
import com.example.myapplication.ui.watchlist.WatchlistItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MovieDetailsScreenState(
    val movie: MovieApiModel? = null,
    val trailerKey: String? = null,
    val castList: List<CastMember> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val isWatched: Boolean = false,
    val isInWatchlist: Boolean = false,
    val userRating: Float = 0f,
    val posterVisible: Boolean = false
)

class MovieDetailsScreenViewModel(
    private val movieId: Int,
    private val apiService: MovieApiService = MovieApiService.create(),
    private val favoritesRepository: FavoritesRepository = FavoritesRepository(),
    private val watchedRepository: WatchedRepository = WatchedRepository(),
    private val ratingRepository: RatingRepository = RatingRepository()
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(MovieDetailsScreenState())
    val uiState: StateFlow<MovieDetailsScreenState> = _uiState.asStateFlow()

    init {
        loadMovieDetails()
    }

    fun loadMovieDetails() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Load movie details
                val movieDetails = apiService.getMovieDetails(movieId)
                val movie = MovieApiModel(
                    id = movieDetails.id,
                    title = movieDetails.title,
                    overview = movieDetails.overview,
                    poster_path = movieDetails.poster_path,
                    release_date = movieDetails.release_date,
                    vote_average = movieDetails.vote_average,
                    genre_ids = emptyList(),
                    original_language = movieDetails.original_language ?: "ar",
                    backdrop_path = movieDetails.backdrop_path
                )

                // Load trailer
                val videos = apiService.getMovieVideos(movieId)
                val trailerKey = videos.results.find { 
                    it.site == "YouTube" && it.type == "Trailer" 
                }?.key

                // Load cast
                val credits = apiService.getMovieCredits(movieId)
                val castList = credits.cast.take(10)

                // Load user's rating for this movie
                val existingRating = ratingRepository.getRating(movieId.toString())
                val userRating = existingRating?.rating ?: 0f

                // Check if in favorites
                val favorites = favoritesRepository.getFavorites(currentUserId)
                val isFavorite = favorites.any { it.movieId == movieId.toString() }

                // Check if watched
                val watched = watchedRepository.getWatched(currentUserId)
                val isWatched = watched.any { it.movieId == movieId.toString() }

                // Check if in watchlist
                val watchlistSnapshot = db.collection("users")
                    .document(currentUserId)
                    .collection("watchlist")
                    .document(movieId.toString())
                    .get()
                    .await()
                val isInWatchlist = watchlistSnapshot.exists()

                _uiState.value = _uiState.value.copy(
                    movie = movie,
                    trailerKey = trailerKey,
                    castList = castList,
                    isLoading = false,
                    isFavorite = isFavorite,
                    isWatched = isWatched,
                    isInWatchlist = isInWatchlist,
                    userRating = userRating,
                    posterVisible = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load movie details. Please try again."
                )
            }
        }
    }

    fun toggleFavorite() {
        val movie = _uiState.value.movie ?: return
        val isFavorite = !_uiState.value.isFavorite
        
        viewModelScope.launch {
            try {
                val favItem = FavoritesItem(
                    movieId = movie.id.toString(),
                    title = movie.title,
                    poster = "https://image.tmdb.org/t/p/w500${movie.poster_path}"
                )
                
                if (isFavorite) {
                    favoritesRepository.addFavorite(favItem, currentUserId)
                } else {
                    favoritesRepository.removeFavorite(movie.id.toString(), currentUserId)
                }
                
                _uiState.value = _uiState.value.copy(isFavorite = isFavorite)
            } catch (e: Exception) {
                // Revert on error
            }
        }
    }

    fun toggleWatched() {
        val movie = _uiState.value.movie ?: return
        val isWatched = !_uiState.value.isWatched
        
        viewModelScope.launch {
            try {
                if (isWatched) {
                    watchedRepository.addWatched(
                        WatchedItem(
                            movieId = movie.id.toString(),
                            title = movie.title,
                            poster = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                            rating = 0,
                            vote_average = movie.vote_average.toInt()
                        )
                    )
                } else {
                    watchedRepository.removeWatched(movie.id.toString())
                }
                
                _uiState.value = _uiState.value.copy(isWatched = isWatched)
            } catch (e: Exception) {
                // Revert on error
            }
        }
    }

    fun toggleWatchlist() {
        val movie = _uiState.value.movie ?: return
        val isInWatchlist = !_uiState.value.isInWatchlist
        
        viewModelScope.launch {
            try {
                if (isInWatchlist) {
                    db.collection("users")
                        .document(currentUserId)
                        .collection("watchlist")
                        .document(movie.id.toString())
                        .set(
                            WatchlistItem(
                                movieId = movie.id.toString(),
                                title = movie.title,
                                poster = "https://image.tmdb.org/t/p/w500${movie.poster_path}"
                            )
                        )
                        .await()
                } else {
                    db.collection("users")
                        .document(currentUserId)
                        .collection("watchlist")
                        .document(movie.id.toString())
                        .delete()
                        .await()
                }
                
                _uiState.value = _uiState.value.copy(isInWatchlist = isInWatchlist)
            } catch (e: Exception) {
                // Revert on error
            }
        }
    }

    fun setRating(rating: Float) {
        val movie = _uiState.value.movie ?: return
        
        _uiState.value = _uiState.value.copy(userRating = rating)
        
        if (rating > 0f) {
            viewModelScope.launch {
                try {
                    ratingRepository.addRating(
                        RatingItem(
                            movieId = movie.id.toString(),
                            title = movie.title,
                            poster = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                            rating = rating,
                            review = "",
                            vote_average = movie.vote_average
                        )
                    )

                    // Add to watched automatically if not already
                    if (!_uiState.value.isWatched) {
                        watchedRepository.addWatched(
                            WatchedItem(
                                movieId = movie.id.toString(),
                                title = movie.title,
                                poster = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                                rating = 0,
                                vote_average = movie.vote_average.toInt()
                            )
                        )
                        _uiState.value = _uiState.value.copy(isWatched = true)
                    }

                    // Remove from watchlist if exists
                    if (_uiState.value.isInWatchlist) {
                        db.collection("users")
                            .document(currentUserId)
                            .collection("watchlist")
                            .document(movie.id.toString())
                            .delete()
                            .await()
                        _uiState.value = _uiState.value.copy(isInWatchlist = false)
                    }
                } catch (e: Exception) {
                    // Silent fail
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun retry() {
        loadMovieDetails()
    }
}

class MovieDetailsScreenViewModelFactory(
    private val movieId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieDetailsScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovieDetailsScreenViewModel(movieId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

