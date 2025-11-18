package com.example.myapplication.data

import com.example.myapplication.ui.screens.favorites.FavoritesItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FavoritesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getFavoritesCollection() =
        auth.currentUser?.uid?.let { userId ->
            db.collection("users").document(userId).collection("favorites")
        }

    suspend fun getFavorites(): List<FavoritesItem> {
        val collection = getFavoritesCollection() ?: return emptyList()
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { it.toObject(FavoritesItem::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addFavorite(item: FavoritesItem) {
        val collection = getFavoritesCollection() ?: return
        collection.document(item.movieId).set(item).await()
    }

    suspend fun removeFavorite(movieId: String) {
        val collection = getFavoritesCollection() ?: return
        collection.document(movieId).delete().await()
    }
}