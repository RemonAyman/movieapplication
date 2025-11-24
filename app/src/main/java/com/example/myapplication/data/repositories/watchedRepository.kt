package com.example.myapplication.data.repositories

import com.example.myapplication.ui.screens.watched.WatchedItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

class WatchedRepository {
    private val database = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser?.uid

    private fun getWatchedCollection(userId: String? = null) =
        (userId ?: auth.currentUser?.uid)?.let { uid ->
            database.collection("users").document(uid).collection("watched")
        }
    suspend fun getWatched(userId: String? = null): List<WatchedItem> {
        val collection = getWatchedCollection(userId)?:return emptyList()


        return try {
            val snapshot =collection.get().await()
            snapshot.mapNotNull {
                it.toObject(WatchedItem::class.java)
            }
        }catch (e: Exception){
            emptyList()
        }

    }
    suspend fun addWatched(item: WatchedItem){

        val collection =getWatchedCollection(currentUser)?:return
        collection.document(item.movieId).set(item).await()

    }
    suspend fun removeWatched(movieId : String){
        val collection=getWatchedCollection(currentUser)?:return
        collection.document(movieId).delete().await()
    }
}