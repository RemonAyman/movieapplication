package com.example.myapplication.data.remote.firebase.repository

import com.example.myapplication.data.remote.firebase.models.UserDataModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FriendsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // ===================== Fetch Lists =====================

    suspend fun getFriendsList(): List<UserDataModel> {
        if (currentUserId.isEmpty()) return emptyList()
        return try {
            val friendsSnapshot = db.collection("users")
                .document(currentUserId)
                .collection("friends")
                .get()
                .await()

            val ids = friendsSnapshot.map { it.id }
            if (ids.isEmpty()) return emptyList()

            val usersSnapshot = db.collection("users")
                .whereIn("uid", ids)
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
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getAllUsers(): List<UserDataModel> {
        if (currentUserId.isEmpty()) return emptyList()
        return try {
            val snapshot = db.collection("users").get().await()
            snapshot.mapNotNull { doc ->
                val uid = doc.getString("uid") ?: return@mapNotNull null
                if (uid == currentUserId) return@mapNotNull null
                UserDataModel(
                    uid = uid,
                    username = doc.getString("username") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    avatarBase64 = doc.getString("avatarBase64") ?: ""
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    // ===================== Friend Requests =====================

    // Incoming requests (الطلبات اللي جت للمستخدم الحالي)
    suspend fun getFriendRequests(): List<UserDataModel> {
        if (currentUserId.isEmpty()) return emptyList()
        return try {
            val snapshot = db.collection("users")
                .document(currentUserId)
                .collection("friendRequests")
                .whereEqualTo("status", "pending")
                .get()
                .await()

            val ids = snapshot.map { it.id }
            if (ids.isEmpty()) return emptyList()

            val usersSnapshot = db.collection("users")
                .whereIn("uid", ids)
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
        } catch (e: Exception) { emptyList() }
    }

    // Outgoing requests (الطلبات اللي أرسلها المستخدم)
    suspend fun getSentFriendRequests(): List<UserDataModel> {
        if (currentUserId.isEmpty()) return emptyList()
        return try {
            val snapshot = db.collection("users")
                .document(currentUserId)
                .collection("friendRequests")
                .whereEqualTo("status", "sent")
                .get()
                .await()

            val ids = snapshot.map { it.id }
            if (ids.isEmpty()) return emptyList()

            val usersSnapshot = db.collection("users")
                .whereIn("uid", ids)
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
        } catch (e: Exception) { emptyList() }
    }



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
        } catch (e: Exception) { null }
    }

    // ===================== Actions =====================

    // إرسال طلب صداقة
    suspend fun sendFriendRequest(targetId: String): Boolean {
        if (currentUserId.isEmpty() || targetId == currentUserId) return false
        return try {
            val batch = db.batch()

            val senderRef = db.collection("users").document(currentUserId)
                .collection("friendRequests").document(targetId)
            val receiverRef = db.collection("users").document(targetId)
                .collection("friendRequests").document(currentUserId)

            batch.set(senderRef, mapOf(
                "from" to currentUserId,
                "to" to targetId,
                "status" to "sent",
                "timestamp" to FieldValue.serverTimestamp()
            ))
            batch.set(receiverRef, mapOf(
                "from" to currentUserId,
                "to" to targetId,
                "status" to "pending",
                "timestamp" to FieldValue.serverTimestamp()
            ))

            batch.commit().await()
            true
        } catch (e: Exception) { false }
    }

    // إلغاء طلب الصداقة
    suspend fun cancelFriendRequest(targetId: String): Boolean {
        if (currentUserId.isEmpty()) return false
        return try {
            val batch = db.batch()
            val myReq = db.collection("users").document(targetId)
                .collection("friendRequests").document(currentUserId)
            val theirReq = db.collection("users").document(currentUserId)
                .collection("friendRequests").document(targetId)

            batch.delete(myReq)
            batch.delete(theirReq)
            batch.commit().await()
            true
        } catch (e: Exception) { false }
    }

    // قبول الطلب → ينضاف في friends ويُحذف من friendRequests
    suspend fun acceptFriendRequest(friendId: String): Boolean {
        if (currentUserId.isEmpty()) return false
        return try {
            val batch = db.batch()

            // حذف الريكويست
            val myReq = db.collection("users").document(currentUserId)
                .collection("friendRequests").document(friendId)
            val theirReq = db.collection("users").document(friendId)
                .collection("friendRequests").document(currentUserId)
            batch.delete(myReq)
            batch.delete(theirReq)

            // إضافة الأصدقاء
            val myFriend = db.collection("users").document(currentUserId)
                .collection("friends").document(friendId)
            val theirFriend = db.collection("users").document(friendId)
                .collection("friends").document(currentUserId)
            val friendData = mapOf("since" to FieldValue.serverTimestamp())
            batch.set(myFriend, friendData)
            batch.set(theirFriend, friendData)

            batch.commit().await()
            true
        } catch (e: Exception) { false }
    }

    // رفض الطلب / إزالة
    suspend fun declineFriendRequest(friendId: String): Boolean = cancelFriendRequest(friendId)

    // إزالة صديق
    suspend fun removeFriend(friendId: String): Boolean {
        if (currentUserId.isEmpty()) return false
        return try {
            val batch = db.batch()
            val myFriend = db.collection("users").document(currentUserId)
                .collection("friends").document(friendId)
            val theirFriend = db.collection("users").document(friendId)
                .collection("friends").document(currentUserId)

            batch.delete(myFriend)
            batch.delete(theirFriend)
            batch.commit().await()
            true
        } catch (e: Exception) { false }
    }
}