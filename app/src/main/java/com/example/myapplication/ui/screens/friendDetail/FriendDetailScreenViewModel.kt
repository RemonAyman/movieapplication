package com.example.myapplication.ui.screens.friendDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.firebase.models.UserDataModel
import com.example.myapplication.data.remote.firebase.repository.FriendsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendDetailScreenState(
    val friend: UserDataModel? = null,
    val friends: List<UserDataModel> = emptyList(),
    val friendRequests: List<UserDataModel> = emptyList(),
    val sentRequests: List<UserDataModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null
) {
    val relationshipStatus: String
        get() = when {
            friends.any { it.uid == friend?.uid } -> "friend"
            sentRequests.any { it.uid == friend?.uid } -> "sent"
            friendRequests.any { it.uid == friend?.uid } -> "incoming"
            else -> ""
        }
}

class FriendDetailScreenViewModel(
    private val friendId: String,
    private val repository: FriendsRepository = FriendsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendDetailScreenState())
    val uiState: StateFlow<FriendDetailScreenState> = _uiState.asStateFlow()

    private fun currentUserId(): String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        loadFriendDetail()
    }

    fun loadFriendDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val userId = currentUserId()
                val friend = repository.getUserById(friendId)
                val friends = repository.getFriendsList(userId)
                val friendRequests = repository.getFriendRequests(userId)
                val sentRequests = repository.getSentFriendRequests(userId)
                
                _uiState.value = _uiState.value.copy(
                    friend = friend,
                    friends = friends,
                    friendRequests = friendRequests,
                    sentRequests = sentRequests,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load friend details"
                )
            }
        }
    }

    fun sendFriendRequest() {
        viewModelScope.launch {
            try {
                repository.sendFriendRequest(friendId)
                _uiState.value = _uiState.value.copy(snackbarMessage = "Friend request sent.")
                loadFriendDetail()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun cancelFriendRequest() {
        viewModelScope.launch {
            try {
                repository.cancelFriendRequest(friendId)
                _uiState.value = _uiState.value.copy(snackbarMessage = "Friend request canceled.")
                loadFriendDetail()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun acceptFriendRequest() {
        viewModelScope.launch {
            try {
                repository.acceptFriendRequest(friendId)
                _uiState.value = _uiState.value.copy(snackbarMessage = "Friend request accepted.")
                loadFriendDetail()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun declineFriendRequest() {
        viewModelScope.launch {
            try {
                repository.declineFriendRequest(friendId)
                _uiState.value = _uiState.value.copy(snackbarMessage = "Friend request declined.")
                loadFriendDetail()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun removeFriend() {
        viewModelScope.launch {
            try {
                repository.removeFriend(friendId)
                _uiState.value = _uiState.value.copy(snackbarMessage = "Removed friend.")
                loadFriendDetail()
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
}

class FriendDetailScreenViewModelFactory(
    private val friendId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendDetailScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FriendDetailScreenViewModel(friendId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

