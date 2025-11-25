package com.example.myapplication.data

import com.example.myapplication.ui.screens.favorites.FavoritesItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FavoritesRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ✅ دالة مساعدة للحصول على الـ Collection الخاصة باليوزر
    private fun getFavoritesCollection(userId: String?) =
        (userId ?: auth.currentUser?.uid)?.let { uid ->
            db.collection("users").document(uid).collection("favorites")
        }

    // ✅ جلب أول 5 Favorites (للـ Home Screen مثلاً)
    suspend fun getFirst5Favorites(userId: String?): List<FavoritesItem> {
        val collection = getFavoritesCollection(userId) ?: return emptyList()
        return try {
            val snapshot = collection.limit(5).get().await()
            snapshot.documents.mapNotNull { it.toObject(FavoritesItem::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ✅ جلب كل الـ Favorites الخاصة باليوزر
    suspend fun getFavorites(userId: String?): List<FavoritesItem> {
        val collection = getFavoritesCollection(userId) ?: return emptyList()
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { it.toObject(FavoritesItem::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ✅ إضافة فيلم للـ Favorites
    suspend fun addFavorite(item: FavoritesItem, userId: String?) {
        val collection = getFavoritesCollection(userId) ?: return
        collection.document(item.movieId).set(item).await()
    }

    // ✅ حذف فيلم من الـ Favorites
    suspend fun removeFavorite(movieId: String, userId: String?) {
        val collection = getFavoritesCollection(userId) ?: return
        collection.document(movieId).delete().await()
    }
}