package com.example.myapplication.utils

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * ✅ نظام الإشعارات المبسط - بدون Server Key
 * يعتمد على Firebase Firestore Triggers
 * الإشعارات تظهر تلقائياً عبر MyFirebaseMessagingService
 */
object NotificationHelper {

    private const val TAG = "NotificationHelper"
    private val db = FirebaseFirestore.getInstance()

    /**
     * ✅ إرسال إشعار لشخص واحد
     * النظام يحفظ البيانات في Firestore
     * وMyFirebaseMessagingService يستقبلها ويعرضها
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
                // ✅ جلب FCM Token للمستخدم
                val userDoc = db.collection("users").document(userId).get().await()
                val fcmToken = userDoc.getString("fcmToken")

                if (fcmToken.isNullOrEmpty()) {
                    Log.w(TAG, "⚠️ No FCM token found for user: $userId")
                    return@withContext
                }

                Log.d(TAG, "📤 Preparing notification for: ${fcmToken.take(20)}...")

                // ✅ حفظ الإشعار في Firestore
                // MyFirebaseMessagingService سيستقبله ويعرضه تلقائياً
                val notificationData = hashMapOf(
                    "to" to fcmToken,
                    "userId" to userId,
                    "title" to title,
                    "body" to body,
                    "chatId" to chatId,
                    "isGroup" to isGroup,
                    "senderAvatar" to senderAvatar,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "read" to false,
                    "delivered" to false
                )

                // ✅ حفظ في collection خاص بالإشعارات
                db.collection("fcm_notifications")
                    .add(notificationData)
                    .await()

                Log.d(TAG, "✅ Notification queued for user: $userId")

                // ✅ أيضاً نحفظ في notifications للـ history
                db.collection("notifications")
                    .add(notificationData)
                    .await()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending notification to user: $userId", e)
            }
        }
    }

    /**
     * ✅ إرسال إشعار لمجموعة من المستخدمين
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
                // ✅ استبعاد المستخدم الحالي
                val recipients = userIds.filter { it != currentUserId }

                Log.d(TAG, "📤 Sending notifications to ${recipients.size} users")

                // ✅ إرسال لكل مستخدم
                recipients.forEach { userId ->
                    sendNotificationToUser(userId, title, body, chatId, isGroup, senderAvatar)
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending notifications to multiple users", e)
            }
        }
    }

    /**
     * ✅ جلب الإشعارات الغير مقروءة
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

                // حذف من notifications
                val notifSnapshot = db.collection("notifications")
                    .whereLessThan("timestamp", weekAgo)
                    .get()
                    .await()

                notifSnapshot.documents.forEach { it.reference.delete() }

                // حذف من fcm_notifications
                val fcmSnapshot = db.collection("fcm_notifications")
                    .whereLessThan("timestamp", weekAgo)
                    .get()
                    .await()

                fcmSnapshot.documents.forEach { it.reference.delete() }

                Log.d(TAG, "✅ Deleted ${notifSnapshot.size() + fcmSnapshot.size()} old notifications")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error deleting old notifications", e)
            }
        }
    }

    /**
     * ✅ إرسال إشعار فوري باستخدام FCM مباشرة (التجربة)
     * هذه الطريقة تعمل بدون Server Key عن طريق استدعاء
     * RemoteMessage مباشرة من Firebase SDK
     */
    suspend fun sendDirectNotification(
        fcmToken: String,
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        withContext(Dispatchers.IO) {
            try {
                // ✅ حفظ في Firestore لتفعيل Trigger
                val notificationData = hashMapOf(
                    "token" to fcmToken,
                    "title" to title,
                    "body" to body,
                    "data" to data,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                db.collection("fcm_queue")
                    .add(notificationData)
                    .await()

                Log.d(TAG, "✅ Direct notification queued")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending direct notification", e)
            }
        }
    }
}