package com.example.myapplication.ui.screens.addFriend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.firebase.models.UserDataModel
import com.example.myapplication.data.remote.firebase.repository.FriendsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddFriendUiState(
    val allUsers: List<UserDataModel> = emptyList(),
    val friendsList: List<UserDataModel> = emptyList(),
    val friendRequests: List<UserDataModel> = emptyList(),
    val sentFriendRequests: List<UserDataModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    fun getUserStatus(userId: String): String {
        return when {
            friendsList.any { it.uid == userId } -> "friend"
            sentFriendRequests.any { it.uid == userId } -> "sent"
            friendRequests.any { it.uid == userId } -> "incoming"
            else -> ""
        }
    }
}

class AddFriendViewModel : ViewModel() {

    private val repository = FriendsRepository()

    private val _uiState = MutableStateFlow(AddFriendUiState())
    val uiState: StateFlow<AddFriendUiState> = _uiState.asStateFlow()

    private fun currentUserId(): String {
        return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    fun loadInitialData() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        try {
            val userId = currentUserId()

            // تحميل كل البيانات بشكل متزامن
            val allUsers = repository.getAllUsers(userId)
            val friends = repository.getFriendsList(userId)
            val requests = repository.getFriendRequests(userId)
            val sent = repository.getSentFriendRequests(userId)

            _uiState.value = _uiState.value.copy(
                allUsers = allUsers,
                friendsList = friends,
                friendRequests = requests,
                sentFriendRequests = sent,
                isLoading = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message
            )
        }
    }

    suspend fun sendFriendRequest(friendId: String) {
        try {
            repository.sendFriendRequest(friendId)

            // تحديث فوري للـ UI بإضافة المستخدم للـ sentRequests
            val currentState = _uiState.value
            val userToAdd = currentState.allUsers.find { it.uid == friendId }

            if (userToAdd != null) {
                _uiState.value = currentState.copy(
                    sentFriendRequests = currentState.sentFriendRequests + userToAdd
                )
            }

            // تحديث كامل من الـ Backend
            loadInitialData()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }

    suspend fun cancelFriendRequest(friendId: String) {
        try {
            repository.cancelFriendRequest(friendId)

            // تحديث فوري للـ UI بإزالة المستخدم من sentRequests
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                sentFriendRequests = currentState.sentFriendRequests.filter { it.uid != friendId }
            )

            // تحديث كامل من الـ Backend
            loadInitialData()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(error = e.message)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}