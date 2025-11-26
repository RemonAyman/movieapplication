package com.example.myapplication.ui.screens.profileMainScreen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FavoritesRepository
import com.example.myapplication.ui.screens.favorites.FavoritesItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileScreenState(
    val username: String = "",
    val avatarBitmap: Bitmap? = null,
    val favorites: List<FavoritesItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileScreenViewModel(
    private val userId: String,
    private val favoritesRepository: FavoritesRepository = FavoritesRepository()
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(ProfileScreenState())
    val uiState: StateFlow<ProfileScreenState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    fun loadProfileData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Load user data
                val snapshot = db.collection("users").document(userId).get().await()
                var username = ""
                var avatarBitmap: Bitmap? = null
                
                if (snapshot.exists()) {
                    username = snapshot.getString("username") ?: "No Name"
                    val avatarBase64 = snapshot.getString("avatarBase64")
                    if (!avatarBase64.isNullOrEmpty()) {
                        val decodedBytes = Base64.decode(avatarBase64, Base64.DEFAULT)
                        avatarBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    }
                }

                // Load favorites
                val favorites = favoritesRepository.getFirst5Favorites(userId)

                _uiState.value = _uiState.value.copy(
                    username = username,
                    avatarBitmap = avatarBitmap,
                    favorites = favorites,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load profile"
                )
            }
        }
    }

    fun refreshFavorites() {
        viewModelScope.launch {
            try {
                val favorites = favoritesRepository.getFirst5Favorites(userId)
                _uiState.value = _uiState.value.copy(favorites = favorites)
            } catch (e: Exception) {
                // Silent fail for refresh
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

class ProfileScreenViewModelFactory(
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileScreenViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

