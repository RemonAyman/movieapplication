package com.example.myapplication.utils

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * ✅ نظام الإشعارات المبسط - يعمل بدون Server Key
 * يستخدم Firestore Triggers لإرسال الإشعارات
 */
object NotificationHelper {

    private const val TAG = "NotificationHelper"
    private val db = FirebaseFirestore.getInstance()

    /**
     * إرسال إشعار لشخص واحد
     */
    suspend fun sendNotificationToUser(
        userId: String,
        title: String,
        body: String,
        chatId: String,
        isGroup: Boolean,
        senderAvatar: String = ""
    ) {
        withContext(Dispatchers.IO) {
            try {
                // جلب FCM Token للمستخدم
                val userDoc = db.collection("users").document(userId).get().await()
                val fcmToken = userDoc.getString("fcmToken")

                if (fcmToken.isNullOrEmpty()) {
                    Log.w(TAG, "⚠️ No FCM token found for user: $userId")
                    return@withContext
                }

                // ✅ حفظ الإشعار في Firestore
                // سيتم إرساله تلقائياً عبر Cloud Function (إذا كانت مفعّلة)
                // أو يمكن قراءته من التطبيق
                val notificationData = hashMapOf(
                    "to" to fcmToken,
                    "userId" to userId,
                    "title" to title,
                    "body" to body,
                    "chatId" to chatId,
                    "isGroup" to isGroup,
                    "senderAvatar" to senderAvatar,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "read" to false
                )

                db.collection("notifications")
                    .add(notificationData)
                    .await()

                Log.d(TAG, "✅ Notification saved for user: $userId")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending notification to user: $userId", e)
            }
        }
    }

    /**
     * إرسال إشعار لمجموعة من المستخدمين (للـ Group Chat)
     */
    suspend fun sendNotificationToMultipleUsers(
        userIds: List<String>,
        currentUserId: String,
        title: String,
        body: String,
        chatId: String,
        isGroup: Boolean,
        senderAvatar: String = ""
    ) {
        withContext(Dispatchers.IO) {
            try {
                // استبعاد المستخدم الحالي من قائمة المستلمين
                val recipients = userIds.filter { it != currentUserId }

                Log.d(TAG, "📤 Sending notifications to ${recipients.size} users")

                recipients.forEach { userId ->
                    sendNotificationToUser(userId, title, body, chatId, isGroup, senderAvatar)
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending notifications to multiple users", e)
            }
        }
    }

    /**
     * ✅ جلب الإشعارات الغير مقروءة للمستخدم الحالي
     */
    suspend fun getUnreadNotifications(userId: String): List<Map<String, Any>> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = db.collection("notifications")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("read", false)
                    .get()
                    .await()

                snapshot.documents.mapNotNull { it.data }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error fetching unread notifications", e)
                emptyList()
            }
        }
    }

    /**
     * ✅ تحديد إشعار كمقروء
     */
    suspend fun markNotificationAsRead(notificationId: String) {
        withContext(Dispatchers.IO) {
            try {
                db.collection("notifications")
                    .document(notificationId)
                    .update("read", true)
                    .await()

                Log.d(TAG, "✅ Notification marked as read: $notificationId")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error marking notification as read", e)
            }
        }
    }

    /**
     * ✅ حذف الإشعارات القديمة (أكثر من 7 أيام)
     */
    suspend fun deleteOldNotifications() {
        withContext(Dispatchers.IO) {
            try {
                val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
                val snapshot = db.collection("notifications")
                    .whereLessThan("timestamp", weekAgo)
                    .get()
                    .await()

                snapshot.documents.forEach { it.reference.delete() }
                Log.d(TAG, "✅ Deleted ${snapshot.size()} old notifications")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error deleting old notifications", e)
            }
        }
    }
}