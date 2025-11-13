package com.example.myapplication.data.remote.firebase.repository

import com.example.myapplication.data.remote.firebase.models.UserDataModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FriendsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // 🔹 جلب قائمة الأصدقاء
    suspend fun getFriendsList(): List< UserDataModel> {
        if (currentUserId.isEmpty()) return emptyList()

        return try {
            val result = db.collection("users")
                .document(currentUserId)
                .collection("friends")
                .get()
                .await()

            val friendsIds = result.map { it.id }
            if (friendsIds.isEmpty()) return emptyList()

            val usersResult = db.collection("users")
                .whereIn("uid", friendsIds)
                .get()
                .await()

            usersResult.map { doc ->
                UserDataModel(
                    uid = doc.getString("uid") ?: "",
                    username = doc.getString("username") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    avatarBase64 = doc.getString("avatarBase64") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    // 🔹 جلب قائمة طلبات الصداقة الواردة
    suspend fun getFriendRequests(): List<UserDataModel> {
        if (currentUserId.isEmpty()) return emptyList()

        return try {
            // جلب الـ uids لكل طلبات الصداقة عندي
            val requestsSnapshot = db.collection("users")
                .document(currentUserId)
                .collection("friendRequests")
                .get()
                .await()

            val requestIds = requestsSnapshot.map { it.id }
            if (requestIds.isEmpty()) return emptyList()

            // جلب بيانات المستخدمين اللي طلبوا الصداقة
            val usersSnapshot = db.collection("users")
                .whereIn("uid", requestIds)
                .get()
                .await()

            usersSnapshot.map { doc ->
                UserDataModel(
                    uid = doc.getString("uid") ?: "",
                    username = doc.getString("username") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    avatarBase64 = doc.getString("avatarBase64") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }



    // 🔹 جلب بيانات صديق محدد
    suspend fun getUserById(friendId: String): UserDataModel? {
        return try {
            val doc = db.collection("users").document(friendId).get().await()
            if (doc.exists()) {
                UserDataModel(
                    uid = doc.getString("uid") ?: "",
                    username = doc.getString("username") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    avatarBase64 = doc.getString("avatarBase64") ?: ""
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // 🔹 إرسال طلب صداقة
    suspend fun sendFriendRequest(targetId: String): Boolean {
        if (currentUserId.isEmpty() || targetId == currentUserId) return false

        return try {
            val requestData = hashMapOf(
                "from" to currentUserId,
                "status" to "pending",
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("users")
                .document(targetId)
                .collection("friendRequests")
                .document(currentUserId)
                .set(requestData)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    // 🔹 قبول طلب صداقة
    suspend fun acceptFriendRequest(friendId: String): Boolean {
        if (currentUserId.isEmpty()) return false

        return try {
            val friendData = hashMapOf(
                "status" to "accepted",
                "since" to FieldValue.serverTimestamp()
            )

            val batch = db.batch()

            val myRef = db.collection("users").document(currentUserId)
                .collection("friends").document(friendId)

            val friendRef = db.collection("users").document(friendId)
                .collection("friends").document(currentUserId)

            val myRequestRef = db.collection("users").document(currentUserId)
                .collection("friendRequests").document(friendId)

            val theirRequestRef = db.collection("users").document(friendId)
                .collection("friendRequests").document(currentUserId)

            batch.set(myRef, friendData)
            batch.set(friendRef, friendData)
            batch.delete(myRequestRef)
            batch.delete(theirRequestRef)

            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // 🔹 رفض طلب صداقة
    suspend fun declineFriendRequest(fromId: String): Boolean {
        if (currentUserId.isEmpty()) return false

        return try {
            val myRef = db.collection("users").document(currentUserId)
                .collection("friendRequests").document(fromId)

            val theirRef = db.collection("users").document(fromId)
                .collection("friendRequests").document(currentUserId)

            val batch = db.batch()
            batch.delete(myRef)
            batch.delete(theirRef)
            batch.commit().await()

            true
        } catch (e: Exception) {
            false
        }
    }

    // 🔹 حذف صديق من الطرفين
    suspend fun removeFriend(friendId: String): Boolean {
        if (currentUserId.isEmpty()) return false

        return try {
            val batch = db.batch()

            val myFriendRef = db.collection("users").document(currentUserId)
                .collection("friends").document(friendId)

            val theirFriendRef = db.collection("users").document(friendId)
                .collection("friends").document(currentUserId)

            batch.delete(myFriendRef)
            batch.delete(theirFriendRef)

            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}