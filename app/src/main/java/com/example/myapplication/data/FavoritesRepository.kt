package com.example.myapplication.data

import android.util.Log
import com.example.myapplication.ui.screens.favorites.FavoritesItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FavoritesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "FavoritesRepository"

    private fun getFavoritesCollection(userId: String?) =
        (userId ?: auth.currentUser?.uid)?.let { uid ->
            Log.d(TAG, "Getting favorites collection for user: $uid")
            db.collection("users").document(uid).collection("favorites")
        }

    suspend fun getFirst5Favorites(userId: String?): List<FavoritesItem> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getFirst5Favorites called with userId: $userId")

        val collection = getFavoritesCollection(userId)
        if (collection == null) {
            Log.e(TAG, "Collection is null! auth.currentUser?.uid = ${auth.currentUser?.uid}")
            return@withContext emptyList()
        }

        return@withContext try {
            Log.d(TAG, "Fetching first 5 favorites from Firestore...")
            val snapshot = collection.limit(5).get().await()
            Log.d(TAG, "Snapshot received. Document count: ${snapshot.documents.size}")

            snapshot.documents.forEachIndexed { index, doc ->
                Log.d(TAG, "Document #$index - ID: ${doc.id}, Data: ${doc.data}")
            }

            val result = snapshot.documents.mapNotNull { it.toObject(FavoritesItem::class.java) }
            Log.d(TAG, "Successfully mapped ${result.size} favorites")
            result

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR in getFirst5Favorites: ${e.message}", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getFavorites(userId: String?): List<FavoritesItem> = withContext(Dispatchers.IO) {
        Log.d(TAG, "getFavorites called with userId: $userId")

        val collection = getFavoritesCollection(userId)
        if (collection == null) {
            Log.e(TAG, "Collection is null! auth.currentUser?.uid = ${auth.currentUser?.uid}")
            return@withContext emptyList()
        }

        return@withContext try {
            Log.d(TAG, "Fetching all favorites from Firestore...")
            val snapshot = collection.get().await()
            Log.d(TAG, "Snapshot received. Document count: ${snapshot.documents.size}")

            snapshot.documents.forEachIndexed { index, doc ->
                Log.d(TAG, "Document #$index - ID: ${doc.id}, Data: ${doc.data}")
            }

            val result = snapshot.documents.mapNotNull { it.toObject(FavoritesItem::class.java) }
            Log.d(TAG, "Successfully mapped ${result.size} favorites")
            result

        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR in getFavorites: ${e.message}", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addFavorite(item: FavoritesItem, userId: String?) = withContext(Dispatchers.IO) {
        Log.d(TAG, "addFavorite called - movieId: ${item.movieId}, title: ${item.title}")

        val collection = getFavoritesCollection(userId)
        if (collection == null) {
            Log.e(TAG, "Cannot add favorite - collection is null!")
            return@withContext
        }

        try {
            collection.document(item.movieId).set(item).await()
            Log.d(TAG, "✅ Successfully added favorite: ${item.title}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR adding favorite: ${e.message}", e)
            e.printStackTrace()
        }
    }

    suspend fun removeFavorite(movieId: String, userId: String?) = withContext(Dispatchers.IO) {
        Log.d(TAG, "removeFavorite called for movieId: $movieId")

        val collection = getFavoritesCollection(userId)
        if (collection == null) {
            Log.e(TAG, "Cannot remove favorite - collection is null!")
            return@withContext
        }

        try {
            collection.document(movieId).delete().await()
            Log.d(TAG, "✅ Successfully removed favorite: $movieId")
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR removing favorite: ${e.message}", e)
            e.printStackTrace()
        }
    }
}