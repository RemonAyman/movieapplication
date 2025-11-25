package com.example.myapplication.data.repositories

import com.example.myapplication.ui.screens.ratings.RatingItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RatingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    suspend fun getRatings(userId: String? = null): List<RatingItem> {
        return try {
            val userIdToUse = userId ?: currentUserId
            val snapshot = db.collection("users")
                .document(userIdToUse)
                .collection("ratings")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                RatingItem(
                    movieId = doc.getString("movieId") ?: "",
                    title = doc.getString("title") ?: "",
                    poster = doc.getString("poster") ?: "",
                    rating = doc.getDouble("rating")?.toFloat() ?: 0f,
                    review = doc.getString("review") ?: "",
                    ratedDate = doc.getLong("ratedDate") ?: System.currentTimeMillis(),
                    vote_average = doc.getDouble("vote_average") ?: 0.0
                )
            }.sortedByDescending { it.ratedDate }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addRating(item: RatingItem) {
        try {
            db.collection("users")
                .document(currentUserId)
                .collection("ratings")
                .document(item.movieId)
                .set(
                    hashMapOf(
                        "movieId" to item.movieId,
                        "title" to item.title,
                        "poster" to item.poster,
                        "rating" to item.rating,
                        "review" to item.review,
                        "ratedDate" to item.ratedDate,
                        "vote_average" to item.vote_average
                    )
                )
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun updateRating(movieId: String, rating: Float, review: String) {
        try {
            db.collection("users")
                .document(currentUserId)
                .collection("ratings")
                .document(movieId)
                .update(
                    mapOf(
                        "rating" to rating,
                        "review" to review,
                        "ratedDate" to System.currentTimeMillis()
                    )
                )
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun removeRating(movieId: String) {
        try {
            db.collection("users")
                .document(currentUserId)
                .collection("ratings")
                .document(movieId)
                .delete()
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getRating(movieId: String): RatingItem? {
        return try {
            val doc = db.collection("users")
                .document(currentUserId)
                .collection("ratings")
                .document(movieId)
                .get()
                .await()

            if (doc.exists()) {
                RatingItem(
                    movieId = doc.getString("movieId") ?: "",
                    title = doc.getString("title") ?: "",
                    poster = doc.getString("poster") ?: "",
                    rating = doc.getDouble("rating")?.toFloat() ?: 0f,
                    review = doc.getString("review") ?: "",
                    ratedDate = doc.getLong("ratedDate") ?: System.currentTimeMillis(),
                    vote_average = doc.getDouble("vote_average") ?: 0.0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}