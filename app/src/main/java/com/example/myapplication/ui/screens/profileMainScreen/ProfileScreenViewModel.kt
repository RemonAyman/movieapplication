package com.example.myapplication.ui.screens.profileMainScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FavoritesRepository
import com.example.myapplication.data.WatchlistRepository
import com.example.myapplication.data.remote.firebase.models.UserDataModel
import com.example.myapplication.data.remote.firebase.repository.FriendsRepository
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

    // Added from FriendDetailScreenState
    val friend: UserDataModel? = null,
    val friends: List<UserDataModel> = emptyList(),
    val friendRequests: List<UserDataModel> = emptyList(),
    val sentRequests: List<UserDataModel> = emptyList(),
    val snackbarMessage: String? = null,

    val isLoading: Boolean = true,
    val error: String? = null
) {
    // Added relationship status calculation
    val relationshipStatus: String
        get() = when {
            friends.any { it.uid == friend?.uid } -> "friend"
            sentRequests.any { it.uid == friend?.uid } -> "sent"
            friendRequests.any { it.uid == friend?.uid } -> "incoming"
            else -> "notFriend"
        }
}

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

    //  Added helper function
    private fun currentUserId(): String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

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

                val watchTime = watchedRepository.getTotalWatchedTime(userId)
                _uiState.value = _uiState.value.copy(totalWatchTime = watchTime)

                val frindsCount = friendsRepository.getFriendsList(userId).size
                _uiState.value = _uiState.value.copy(friendsCount = frindsCount)

                val favorites = favoritesRepository.getFirst5Favorites(userId)
                _uiState.value = _uiState.value.copy(favoriteMovies = favorites)

                val watched = watchedRepository.getWatched(userId)
                _uiState.value = _uiState.value.copy(watchedMovies = watched)

                val ratings = ratingsRepository.getRatings(userId)
                _uiState.value = _uiState.value.copy(ratingsMovies = ratings)

                // Added friend detail loading
                loadFriendDetailData()

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

    // Added from FriendDetailScreenViewModel
    private suspend fun loadFriendDetailData() {
        _uiState.value=_uiState.value.copy(isLoading = true)
        try {
            val currentUid = currentUserId()
            val friend = userId?.let { friendsRepository.getUserById(it) }
            val friends = friendsRepository.getFriendsList(currentUid)
            val friendRequests = friendsRepository.getFriendRequests(currentUid)
            val sentRequests = friendsRepository.getSentFriendRequests(currentUid)

            _uiState.value = _uiState.value.copy(
                friend = friend,
                friends = friends,
                friendRequests = friendRequests,
                sentRequests = sentRequests,
                isLoading = false
            )
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error loading friend details: ${e.message}", e)
        }
    }

    // Added all friend management functions
    fun sendFriendRequest() {
        viewModelScope.launch {
            try {
                userId?.let { friendId ->
                    friendsRepository.sendFriendRequest(friendId)
                    _uiState.value = _uiState.value.copy(snackbarMessage = "Friend request sent.")
                    loadFriendDetailData()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun cancelFriendRequest() {
        viewModelScope.launch {
            try {
                userId?.let { friendId ->
                    friendsRepository.cancelFriendRequest(friendId)
                    _uiState.value = _uiState.value.copy(snackbarMessage = "Friend request canceled.")
                    loadFriendDetailData()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun acceptFriendRequest() {
        viewModelScope.launch {
            try {
                userId?.let { friendId ->
                    friendsRepository.acceptFriendRequest(friendId)
                    _uiState.value = _uiState.value.copy(snackbarMessage = "Friend request accepted.")
                    loadFriendDetailData()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun declineFriendRequest() {
        viewModelScope.launch {
            try {
                userId?.let { friendId ->
                    friendsRepository.declineFriendRequest(friendId)
                    _uiState.value = _uiState.value.copy(snackbarMessage = "Friend request declined.")
                    loadFriendDetailData()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun removeFriend() {
        viewModelScope.launch {
            try {
                userId?.let { friendId ->
                    friendsRepository.removeFriend(friendId)
                    _uiState.value = _uiState.value.copy(snackbarMessage = "Removed friend.")
                    loadFriendDetailData()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
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