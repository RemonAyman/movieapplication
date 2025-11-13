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

    fun loadFriendsList() {
        viewModelScope.launch {
            _friendsList.value = repository.getFriendsList()
        }
    }

    fun loadFriendRequests() {
        viewModelScope.launch {
            _friendRequests.value = repository.getFriendRequests()
        }
    }


    fun loadFriendDetail(friendId: String) {
        viewModelScope.launch {
            _friendDetail.value = repository.getUserById(friendId)
        }
    }

    fun acceptFriend(friendId: String) {
        viewModelScope.launch {
            val success = repository.acceptFriendRequest(friendId)
            if (success) loadFriendsList()
        }
    }

    fun sendFriendRequest(friendId: String) {
        viewModelScope.launch {
            repository.sendFriendRequest(friendId)
        }
    }

    fun declineFriendRequest(friendId: String) {
        viewModelScope.launch {
            repository.declineFriendRequest(friendId)
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            val success = repository.removeFriend(friendId)
            if (success) loadFriendsList()
        }
    }
}
