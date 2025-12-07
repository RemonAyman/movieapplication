package com.example.myapplication.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * ✅ نظام الإشعارات المجاني 100%
 * بدون Cloud Functions - بدون فلوس!
 * يعمل عن طريق Firestore Listener
 */
object NotificationHelper {

    private const val TAG = "NotificationHelper"
    private val db = FirebaseFirestore.getInstance()
    private var notificationListener: ListenerRegistration? = null

    /**
     * ✅ إرسال إشعار لشخص واحد (مجاني 100%)
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
                Log.d(TAG, "📤 Sending notification to user: $userId")

                // ✅ حفظ الإشعار في Firestore
                // التطبيق نفسه هيقراه ويعرضه!
                val notificationData = hashMapOf(
                    "userId" to userId,
                    "title" to title,
                    "body" to body,
                    "chatId" to chatId,
                    "isGroup" to isGroup,
                    "senderAvatar" to senderAvatar,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "read" to false
                )

                // ✅ حفظ في collection خاص بالإشعارات
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

                Log.d(TAG, "✅ All notifications sent successfully")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sending notifications to multiple users", e)
            }
        }
    }

    /**
     * ✅ بدء الاستماع للإشعارات (يتم استدعاؤها في MainActivity)
     */
    fun startListeningForNotifications(context: Context) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        Log.d(TAG, "🔔 Starting notification listener for user: $currentUserId")

        // ✅ إلغاء الـ Listener القديم (لو موجود)
        notificationListener?.remove()

        // ✅ إنشاء Listener جديد
        notificationListener = db.collection("notifications")
            .whereEqualTo("userId", currentUserId)
            .whereEqualTo("read", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Notification listener error", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    for (doc in snapshot.documentChanges) {
                        if (doc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                            val data = doc.document.data
                            val title = data["title"] as? String ?: "New Message"
                            val body = data["body"] as? String ?: ""
                            val chatId = data["chatId"] as? String ?: ""
                            val isGroup = data["isGroup"] as? Boolean ?: false
                            val senderAvatar = data["senderAvatar"] as? String ?: ""

                            Log.d(TAG, "📬 New notification: $title - $body")

                            // ✅ عرض الإشعار
                            showLocalNotification(
                                context,
                                title,
                                body,
                                chatId,
                                isGroup,
                                senderAvatar,
                                doc.document.id
                            )

                            // ✅ تحديد الإشعار كمقروء
                            doc.document.reference.update("read", true)
                        }
                    }
                }
            }
    }

    /**
     * ✅ إيقاف الاستماع للإشعارات
     */
    fun stopListeningForNotifications() {
        notificationListener?.remove()
        notificationListener = null
        Log.d(TAG, "🔕 Notification listener stopped")
    }

    /**
     * ✅ عرض الإشعار المحلي
     */
    private fun showLocalNotification(
        context: Context,
        title: String,
        body: String,
        chatId: String,
        isGroup: Boolean,
        senderAvatar: String,
        notificationId: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // ✅ إنشاء القناة (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "chat_notifications",
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for chat messages"
                enableLights(true)
                lightColor = 0xFF9B5DE5.toInt()
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // ✅ Intent للانتقال إلى الشات
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("openChat", true)
            putExtra("chatId", chatId)
            putExtra("isGroup", isGroup)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            chatId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ تحويل الصورة من Base64
        val largeIcon = try {
            if (senderAvatar.isNotEmpty()) {
                val bytes = Base64.decode(senderAvatar, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to decode avatar", e)
            null
        }

        // ✅ صوت الإشعار
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // ✅ بناء الإشعار
        val notificationBuilder = NotificationCompat.Builder(context, "chat_notifications")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setColor(0xFF9B5DE5.toInt())
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        // ✅ إضافة الصورة
        if (largeIcon != null) {
            notificationBuilder.setLargeIcon(largeIcon)
        }

        // ✅ عرض الإشعار
        notificationManager.notify(notificationId.hashCode(), notificationBuilder.build())
        Log.d(TAG, "✅ Notification displayed: $title")
    }

    /**
     * ✅ حذف الإشعارات القديمة (أكثر من 7 أيام)
     */
    suspend fun deleteOldNotifications() {
        withContext(Dispatchers.IO) {
            try {
                val weekAgo = com.google.firebase.Timestamp(
                    System.currentTimeMillis() / 1000 - (7 * 24 * 60 * 60),
                    0
                )

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