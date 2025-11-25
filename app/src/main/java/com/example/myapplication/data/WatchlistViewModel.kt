package com.example.myapplication.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
// ===================== Extension Function =====================
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


class WatchlistViewModel(userID: String?=null) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _watchlist = MutableStateFlow<List<WatchlistItem>>(emptyList())
    val watchlist: StateFlow<List<WatchlistItem>> = _watchlist

    private fun currentUserId(): String? = auth.currentUser?.uid

    init {
        loadWatchlist(userID)
    }

    private fun loadWatchlist(userId: String? = null) {

        // تحويل الـ snapshot إلى Flow ليبقى Coroutine-friendly
        viewModelScope.launch {
            db.collection("users")
                .document(userId!!)
                .collection("watchlist")
                .snapshotsFlow()
                .collect { snapshot ->
                    val list = snapshot.documents.mapNotNull { it.toObject(WatchlistItem::class.java) }
                    _watchlist.value = list
                }
        }
    }

    fun addToWatchlist(movie: WatchlistItem) {
        val userId = currentUserId() ?: return
        viewModelScope.launch {
            db.collection("users")
                .document(userId)
                .collection("watchlist")
                .document(movie.movieId)
                .set(movie)
                .await()  // استخدام await بدل addOnSuccessListener
            _watchlist.value = _watchlist.value + movie
        }
    }

    fun removeFromWatchlist(movieId: String) {
        val userId = currentUserId() ?: return
        viewModelScope.launch {
            db.collection("users")
                .document(userId)
                .collection("watchlist")
                .document(movieId)
                .delete()
                .await()  // استخدام await بدل addOnSuccessListener
            _watchlist.value = _watchlist.value.filter { it.movieId != movieId }
        }
    }
}
class WatchlistViewModelFactory(
    private val userId: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WatchlistViewModel::class.java)) {
            return WatchlistViewModel(userID = userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}




fun Query.snapshotsFlow(): Flow<QuerySnapshot> = callbackFlow {
    val listener = addSnapshotListener { snapshot, error ->
        if (error != null) return@addSnapshotListener
        snapshot?.let { trySend(it).isSuccess }
    }
    awaitClose { listener.remove() }
}