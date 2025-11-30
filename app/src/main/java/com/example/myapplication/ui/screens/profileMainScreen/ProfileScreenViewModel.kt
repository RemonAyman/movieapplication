package com.example.myapplication.ui.screens.profileMainScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FavoritesRepository
import com.example.myapplication.data.WatchlistRepository
import com.example.myapplication.data.remote.firebase.repository.FriendsRepository
import com.example.myapplication.data.repositories.RatingRepository
import com.example.myapplication.data.repositories.WatchedRepository
import com.example.myapplication.ui.screens.favorites.FavoritesItem
import com.example.myapplication.ui.screens.friends.FriendsViewModel
import com.example.myapplication.ui.screens.ratings.RatingItem
import com.example.myapplication.ui.screens.watched.WatchedItem
import com.example.myapplication.ui.watchlist.WatchlistItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val username: String = "Username",
    val avatarBase64: String = "",
    val joinDate: String = "Jun 5, 2025",
    val friendsCount: Int = 0,
    val totalWatchTime: String = "0d 0h 0m",
    val favoriteMovies: List<FavoritesItem> = emptyList(),
    val watchlistMovies: List<WatchlistItem> = emptyList(),
    val watchedMovies: List<WatchedItem> = emptyList(),
    val ratingsMovies: List<RatingItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ProfileScreenViewModel(
    val userId: String?,
    private val watchedRepository: WatchedRepository = WatchedRepository(),
    private val favoritesRepository: FavoritesRepository = FavoritesRepository(),
    private val watchlistRepository: WatchlistRepository = WatchlistRepository(),
    private val ratingsRepository: RatingRepository = RatingRepository(),
    private val friendsRepository: FriendsRepository = FriendsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        fetchProfileData()
    }

    private fun fetchProfileData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val uid = userId ?: run {
                    Log.e("ProfileViewModel", "userId is null!")
                    return@launch
                }

                // ---------------------------
                // 🔥 Fetch username + avatar from Firestore only
                // ---------------------------
                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        val username = doc.getString("username") ?: "Username"
                        val avatarBase64 = doc.getString("avatarBase64") ?: ""
                        _uiState.value = _uiState.value.copy(
                            username = username,
                            avatarBase64 = avatarBase64
                        )
                        Log.d("ProfileViewModel", "✅ Fetched username & avatar successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProfileViewModel", "Firestore error: ${e.message}", e)
                    }

                // باقي اللوجيك زي ما هو بدون أي تعديل
                val watchTime = watchedRepository.getTotalWatchedTime(userId)
                _uiState.value = _uiState.value.copy(totalWatchTime = watchTime)

                val frindsCount = friendsRepository.getFriendsList().size
                _uiState.value =_uiState.value.copy(friendsCount = frindsCount)

                val favorites = favoritesRepository.getFirst5Favorites(userId)
                _uiState.value = _uiState.value.copy(favoriteMovies = favorites)

                val watched = watchedRepository.getWatched(userId)
                _uiState.value = _uiState.value.copy(watchedMovies = watched)

                val ratings = ratingsRepository.getRatings(userId)
                _uiState.value = _uiState.value.copy(ratingsMovies = ratings)

                watchlistRepository.getWatchlistFlow(userId).collect { list ->
                    _uiState.value = _uiState.value.copy(
                        watchlistMovies = list,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun refresh() {
        fetchProfileData()
    }
}

class ProfileScreenViewModelFactory(
    private val userId: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileScreenViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
