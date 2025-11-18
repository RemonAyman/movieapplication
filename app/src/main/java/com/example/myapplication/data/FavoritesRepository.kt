package com.example.myapplication.data

import com.example.myapplication.ui.screens.favorites.FavoritesItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FavoritesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getFavoritesCollection(userId: String? = null) =
        (userId ?: auth.currentUser?.uid)?.let { uid ->
            db.collection("users").document(uid).collection("favorites")
        }

    suspend fun getFavorites(userId: String? = null): List<FavoritesItem> {
        val collection = getFavoritesCollection(userId) ?: return emptyList()
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { it.toObject(FavoritesItem::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addFavorite(item: FavoritesItem, userId: String? = null) {
        val collection = getFavoritesCollection(userId) ?: return
        collection.document(item.movieId).set(item).await()
    }

    suspend fun removeFavorite(movieId: String, userId: String? = null) {
        val collection = getFavoritesCollection(userId) ?: return
        collection.document(movieId).delete().await()
    }
}
