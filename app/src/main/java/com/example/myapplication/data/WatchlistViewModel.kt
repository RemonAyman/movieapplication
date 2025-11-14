package com.example.myapplication.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class WatchlistItem(
    val movieId: String = "",
    val title: String = "",
    val poster: String = ""
)

class WatchlistViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _watchlist = MutableStateFlow<List<WatchlistItem>>(emptyList())
    val watchlist: StateFlow<List<WatchlistItem>> = _watchlist

    init {
        loadWatchlist()
    }

    private fun loadWatchlist() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("watchlist")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error if needed
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toObject(WatchlistItem::class.java) }
                    _watchlist.value = list
                }
            }
    }

    fun addToWatchlist(movie: WatchlistItem) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            db.collection("users")
                .document(userId)
                .collection("watchlist")
                .document(movie.movieId)
                .set(movie)
                .addOnSuccessListener {
                    // Automatically update local state
                    _watchlist.value = _watchlist.value + movie
                }
        }
    }

    fun removeFromWatchlist(movieId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            db.collection("users")
                .document(userId)
                .collection("watchlist")
                .document(movieId)
                .delete()
                .addOnSuccessListener {
                    // Automatically update local state
                    _watchlist.value = _watchlist.value.filter { it.movieId != movieId }
                }
        }
    }
}
