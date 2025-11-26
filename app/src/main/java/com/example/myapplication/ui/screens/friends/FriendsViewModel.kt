package com.example.myapplication.ui.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.firebase.models.UserDataModel
import com.example.myapplication.data.remote.firebase.repository.FriendsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FriendsViewModel : ViewModel() {

    private val repository = FriendsRepository()

    private val _friendsList = MutableStateFlow<List<UserDataModel>>(emptyList())
    val friendsList: StateFlow<List<UserDataModel>> = _friendsList

    private val _friendDetail = MutableStateFlow<UserDataModel?>(null)
    val friendDetail: StateFlow<UserDataModel?> = _friendDetail

    private val _friendRequests = MutableStateFlow<List<UserDataModel>>(emptyList())
    val friendRequests: StateFlow<List<UserDataModel>> = _friendRequests

    private val _sentFriendRequests = MutableStateFlow<List<UserDataModel>>(emptyList())
    val sentFriendRequests: StateFlow<List<UserDataModel>> = _sentFriendRequests

    private val _allUsers = MutableStateFlow<List<UserDataModel>>(emptyList())
    val allUsers: StateFlow<List<UserDataModel>> = _allUsers

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    // ===================== Loaders =====================
    fun loadFriendsList(userId: String = currentUserId()) = viewModelScope.launch {
        _loadingState.value = true
        try {
            _friendsList.value = repository.getFriendsList(userId)
            _errorState.value = null
        } catch(e: Exception) {
            _errorState.value = e.message
        } finally {
            _loadingState.value = false
        }
    }

    fun loadAllUsers(userId: String = currentUserId()) = viewModelScope.launch {
        _loadingState.value = true
        try {
            _allUsers.value = repository.getAllUsers(userId)
            _errorState.value = null
        } catch(e: Exception) {
            _errorState.value = e.message
        } finally {
            _loadingState.value = false
        }
    }

    fun loadFriendRequests(userId: String = currentUserId()) = viewModelScope.launch {
        _loadingState.value = true
        try {
            _friendRequests.value = repository.getFriendRequests(userId)
            _sentFriendRequests.value = repository.getSentFriendRequests(userId)
            _errorState.value = null
        } catch(e: Exception) {
            _errorState.value = e.message
        } finally {
            _loadingState.value = false
        }
    }

    fun loadFriendDetail(friendId: String) = viewModelScope.launch {
        _loadingState.value = true
        try {
            _friendDetail.value = repository.getUserById(friendId)
            _errorState.value = null
        } catch(e: Exception) {
            _errorState.value = e.message
        } finally {
            _loadingState.value = false
        }
    }

    // ===================== Actions =====================
    fun sendFriendRequest(friendId: String) = viewModelScope.launch {
        _loadingState.value = true
        try {
            repository.sendFriendRequest(friendId)
            _errorState.value = null
            loadFriendRequests()
            loadAllUsers()
        } catch(e: Exception) { _errorState.value = e.message }
        finally { _loadingState.value = false }
    }

    fun cancelFriendRequest(friendId: String) = viewModelScope.launch {
        _loadingState.value = true
        try {
            repository.cancelFriendRequest(friendId)
            _errorState.value = null
            loadFriendRequests()
            loadAllUsers()
        } catch(e: Exception) { _errorState.value = e.message }
        finally { _loadingState.value = false }
    }

    fun acceptFriendRequest(friendId: String) = viewModelScope.launch {
        _loadingState.value = true
        try {
            repository.acceptFriendRequest(friendId)
            _errorState.value = null
            loadFriendsList()
            loadFriendRequests()
            loadAllUsers()
        } catch(e: Exception) { _errorState.value = e.message }
        finally { _loadingState.value = false }
    }

    fun declineFriendRequest(friendId: String) = viewModelScope.launch {
        _loadingState.value = true
        try {
            repository.declineFriendRequest(friendId)
            _errorState.value = null
            loadFriendRequests()
            loadAllUsers()
        } catch(e: Exception) { _errorState.value = e.message }
        finally { _loadingState.value = false }
    }

    fun removeFriend(friendId: String) = viewModelScope.launch {
        _loadingState.value = true
        try {
            repository.removeFriend(friendId)
            _errorState.value = null
            loadFriendsList()
            loadFriendRequests()
            loadAllUsers()
        } catch(e: Exception) { _errorState.value = e.message }
        finally { _loadingState.value = false }
    }

    // ===================== Helpers for UI =====================
    fun computeRequestStatusFor(userId: String): String {
        if (_friendsList.value.any { it.uid == userId }) return "friend"
        if (_sentFriendRequests.value.any { it.uid == userId }) return "sent"
        if (_friendRequests.value.any { it.uid == userId }) return "incoming"
        return ""
    }

    private fun currentUserId(): String {
        return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
}

