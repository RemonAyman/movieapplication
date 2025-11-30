package com.example.myapplication.ui.screens.profileMainScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FavoritesRepository
import com.example.myapplication.data.WatchlistRepository
import com.example.myapplication.data.repositories.RatingRepository
import com.example.myapplication.data.repositories.WatchedRepository
import com.example.myapplication.ui.screens.favorites.FavoritesItem
import com.example.myapplication.ui.screens.ratings.RatingItem
import com.example.myapplication.ui.screens.watched.WatchedItem
import com.example.myapplication.ui.watchlist.WatchlistItem
import com.google.firebase.database.FirebaseDatabase
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
    private val ratingsRepository: RatingRepository = RatingRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        fetchProfileData()
    }

    private fun fetchProfileData() {
        viewModelScope.launch {
            try {
                android.util.Log.d("ProfileViewModel", "fetchProfileData started for userId: $userId")
                _uiState.value = _uiState.value.copy(isLoading = true)

                // جلب بيانات المستخدم الكاملة من Firebase
                userId?.let { uid ->
                    android.util.Log.d("ProfileViewModel", "Fetching user data from Firebase for uid: $uid")

                    FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(uid)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            android.util.Log.d("ProfileViewModel", "✅ Firebase Success! Snapshot exists: ${snapshot.exists()}")
                            android.util.Log.d("ProfileViewModel", "Snapshot children count: ${snapshot.childrenCount}")

                            // Print all keys
                            snapshot.children.forEach { child ->
                                android.util.Log.d("ProfileViewModel", "Key: ${child.key}, Value: ${child.value}")
                            }

                            // Try different possible field names
                            val username = snapshot.child("username").getValue(String::class.java)
                                ?: snapshot.child("name").getValue(String::class.java)
                                ?: snapshot.child("displayName").getValue(String::class.java)
                                ?: "Username"

                            val avatarBase64 = snapshot.child("avatarBase64").getValue(String::class.java)
                                ?: snapshot.child("avatar").getValue(String::class.java)
                                ?: snapshot.child("profilePicture").getValue(String::class.java)
                                ?: ""

                            android.util.Log.d("ProfileViewModel", "Username found: $username")
                            android.util.Log.d("ProfileViewModel", "Avatar length: ${avatarBase64.length}")
                            android.util.Log.d("ProfileViewModel", "Avatar first 50 chars: ${avatarBase64.take(50)}")

                            _uiState.value = _uiState.value.copy(
                                username = username,
                                avatarBase64 = avatarBase64
                            )

                            android.util.Log.d("ProfileViewModel", "✅ UI State updated successfully")
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("ProfileViewModel", "❌ Firebase Error: ${e.message}", e)
                        }
                } ?: run {
                    android.util.Log.e("ProfileViewModel", "❌ userId is NULL!")
                }

                // حساب الوقت الكلي
                val watchTime = watchedRepository.getTotalWatchedTime(userId)
                _uiState.value = _uiState.value.copy(totalWatchTime = watchTime)

                // جلب القوائم
                val favorites = favoritesRepository.getFirst5Favorites(userId)
                _uiState.value = _uiState.value.copy(favoriteMovies = favorites)

                val watched = watchedRepository.getWatched(userId)
                _uiState.value = _uiState.value.copy(watchedMovies = watched)

                val ratings = ratingsRepository.getRatings(userId)
                _uiState.value = _uiState.value.copy(ratingsMovies = ratings)

                // جمع Watchlist من الـ Flow
                watchlistRepository.getWatchlistFlow(userId).collect { list ->
                    _uiState.value = _uiState.value.copy(
                        watchlistMovies = list,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "❌ Exception in fetchProfileData: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Unknown error",
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