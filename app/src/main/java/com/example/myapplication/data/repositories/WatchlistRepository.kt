package com.example.myapplication.data

import com.example.myapplication.ui.watchlist.WatchlistItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WatchlistRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private fun currentUserId(userId: String? = null): String? = userId ?: auth.currentUser?.uid

    // جلب Watchlist كـ Flow
    fun getWatchlistFlow(userId: String? = null): Flow<List<WatchlistItem>> {
        val uid = currentUserId(userId) ?: throw IllegalArgumentException("User not logged in")

        return callbackFlow {
            val listener = db.collection("users")
                .document(uid)
                .collection("watchlist")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val list = snapshot?.documents?.mapNotNull {
                        it.toObject(WatchlistItem::class.java)
                    } ?: emptyList()
                    trySend(list).isSuccess
                }

            awaitClose { listener.remove() }
        }
    }

    // إضافة فيلم
    suspend fun addToWatchlist(movie: WatchlistItem, userId: String? = null) {
        val uid = currentUserId(userId) ?: return
        db.collection("users")
            .document(uid)
            .collection("watchlist")
            .document(movie.movieId)
            .set(movie)
            .await()
    }

    // إزالة فيلم
    suspend fun removeFromWatchlist(movieId: String, userId: String? = null) {
        val uid = currentUserId(userId) ?: return
        db.collection("users")
            .document(uid)
            .collection("watchlist")
            .document(movieId)
            .delete()
            .await()
    }
}
