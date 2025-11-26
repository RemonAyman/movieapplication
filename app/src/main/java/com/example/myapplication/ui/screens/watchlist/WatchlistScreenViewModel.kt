package com.example.myapplication.ui.screens.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.watchlist.WatchlistItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class WatchlistScreenState(
    val watchlistItems: List<WatchlistItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class WatchlistScreenViewModel(
    private val userId: String? = null
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(WatchlistScreenState())
    val uiState: StateFlow<WatchlistScreenState> = _uiState.asStateFlow()

    private fun currentUserId(): String? = userId ?: auth.currentUser?.uid

    init {
        loadWatchlist()
    }

    private fun loadWatchlist() {
        val uid = currentUserId() ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            db.collection("users")
                .document(uid)
                .collection("watchlist")
                .snapshotsFlow()
                .collect { snapshot ->
                    val list = snapshot.documents.mapNotNull { 
                        it.toObject(WatchlistItem::class.java) 
                    }
                    _uiState.value = _uiState.value.copy(
                        watchlistItems = list,
                        isLoading = false
                    )
                }
        }
    }

    fun addToWatchlist(movie: WatchlistItem) {
        val uid = currentUserId() ?: return
        
        viewModelScope.launch {
            try {
                db.collection("users")
                    .document(uid)
                    .collection("watchlist")
                    .document(movie.movieId)
                    .set(movie)
                    .await()
                    
                _uiState.value = _uiState.value.copy(
                    watchlistItems = _uiState.value.watchlistItems + movie
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to add to watchlist"
                )
            }
        }
    }

    fun removeFromWatchlist(movieId: String) {
        val uid = currentUserId() ?: return
        
        viewModelScope.launch {
            try {
                db.collection("users")
                    .document(uid)
                    .collection("watchlist")
                    .document(movieId)
                    .delete()
                    .await()
                    
                _uiState.value = _uiState.value.copy(
                    watchlistItems = _uiState.value.watchlistItems.filter { it.movieId != movieId }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to remove from watchlist"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun Query.snapshotsFlow(): Flow<QuerySnapshot> = callbackFlow {
        val listener = addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            snapshot?.let { trySend(it).isSuccess }
        }
        awaitClose { listener.remove() }
    }
}

class WatchlistScreenViewModelFactory(
    private val userId: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WatchlistScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WatchlistScreenViewModel(userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

