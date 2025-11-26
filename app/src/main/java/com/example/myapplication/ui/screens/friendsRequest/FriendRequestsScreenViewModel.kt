package com.example.myapplication.ui.screens.friendsRequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.firebase.models.UserDataModel
import com.example.myapplication.data.remote.firebase.repository.FriendsRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendRequestsScreenState(
    val incomingRequests: List<UserDataModel> = emptyList(),
    val sentRequests: List<UserDataModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FriendRequestsScreenViewModel(
    private val repository: FriendsRepository = FriendsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendRequestsScreenState())
    val uiState: StateFlow<FriendRequestsScreenState> = _uiState.asStateFlow()

    private fun currentUserId(): String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    init {
        loadFriendRequests()
    }

    fun loadFriendRequests() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val userId = currentUserId()
                val incoming = repository.getFriendRequests(userId)
                val sent = repository.getSentFriendRequests(userId)
                
                _uiState.value = _uiState.value.copy(
                    incomingRequests = incoming,
                    sentRequests = sent,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load friend requests"
                )
            }
        }
    }

    fun acceptFriendRequest(friendId: String) {
        viewModelScope.launch {
            try {
                repository.acceptFriendRequest(friendId)
                loadFriendRequests()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun declineFriendRequest(friendId: String) {
        viewModelScope.launch {
            try {
                repository.declineFriendRequest(friendId)
                loadFriendRequests()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun cancelFriendRequest(friendId: String) {
        viewModelScope.launch {
            try {
                repository.cancelFriendRequest(friendId)
                loadFriendRequests()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

