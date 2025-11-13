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

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    // 🔹 تحميل الأصدقاء الحاليين
    fun loadFriendsList() {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                _friendsList.value = repository.getFriendsList()
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }

    // 🔹 تحميل كل المستخدمين للبحث (isSearchMode)
    fun loadAllUsers() {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                _friendsList.value = repository.getAllUsers()
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }

    // 🔹 تحميل طلبات الصداقة
    fun loadFriendRequests() {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                _friendRequests.value = repository.getFriendRequests()
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }

    // 🔹 تحميل بيانات صديق محدد
    fun loadFriendDetail(friendId: String) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                _friendDetail.value = repository.getUserById(friendId)
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }

    // 🔹 قبول طلب صداقة
    fun acceptFriend(friendId: String) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                val success = repository.acceptFriendRequest(friendId)
                if (success) loadFriendsList()
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }

    // 🔹 إرسال طلب صداقة
    fun sendFriendRequest(friendId: String) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                repository.sendFriendRequest(friendId)
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }

    // 🔹 رفض طلب صداقة
    fun declineFriendRequest(friendId: String) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                repository.declineFriendRequest(friendId)
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }

    // 🔹 إزالة صديق
    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                val success = repository.removeFriend(friendId)
                if (success) loadFriendsList()
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }
}
