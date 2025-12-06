package com.example.myapplication.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * ✅ خدمة Firebase Cloud Messaging لاستقبال الإشعارات
 * تعمل حتى لو التطبيق مغلق أو في الخلفية
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCMService"
    private val CHANNEL_ID = "chat_notifications"
    private val CHANNEL_NAME = "Chat Messages"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "✅ FCM Service Created")
    }

    /**
     * ✅ يتم استدعاؤها عند استلام رسالة جديدة
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "📩 Message received from: ${remoteMessage.from}")

        // ✅ استخراج البيانات من الرسالة
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "New Message"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: ""
        val chatId = remoteMessage.data["chatId"] ?: ""
        val isGroup = remoteMessage.data["isGroup"]?.toBoolean() ?: false
        val senderAvatar = remoteMessage.data["senderAvatar"] ?: ""

        Log.d(TAG, "📬 Title: $title")
        Log.d(TAG, "📝 Body: $body")
        Log.d(TAG, "💬 Chat ID: $chatId")
        Log.d(TAG, "👥 Is Group: $isGroup")

        // ✅ عرض الإشعار
        if (chatId.isNotEmpty()) {
            showNotification(title, body, chatId, isGroup, senderAvatar)
        }
    }

    /**
     * ✅ يتم استدعاؤها عند تحديث FCM Token
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔑 New FCM Token: ${token.take(20)}...")

        // ✅ حفظ الـ Token في Firestore
        updateFCMTokenInFirestore(token)
    }

    /**
     * ✅ تحديث FCM Token في Firestore
     */
    private fun updateFCMTokenInFirestore(token: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d(TAG, "✅ FCM Token updated in Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "❌ Failed to update FCM Token", e)
                }
        } else {
            Log.w(TAG, "⚠️ No user logged in, cannot update FCM Token")
        }
    }

    /**
     * ✅ عرض الإشعار مع صورة المرسل
     */
    private fun showNotification(
        title: String,
        body: String,
        chatId: String,
        isGroup: Boolean,
        senderAvatar: String
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // ✅ Intent للانتقال إلى الشات عند الضغط على الإشعار
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("openChat", true)
            putExtra("chatId", chatId)
            putExtra("isGroup", isGroup)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            chatId.hashCode(), // ID فريد لكل شات
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ تحويل الصورة من Base64 لـ Bitmap
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
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // ✅ تأكد من وجود الأيقونة
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setColor(0xFF9B5DE5.toInt()) // لون الإشعار (بنفسجي)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // ✅ إضافة الصورة إذا كانت متوفرة
        if (largeIcon != null) {
            notificationBuilder.setLargeIcon(largeIcon)
        }

        // ✅ عرض الإشعار
        notificationManager.notify(chatId.hashCode(), notificationBuilder.build())
        Log.d(TAG, "✅ Notification displayed successfully")
    }

    /**
     * ✅ إنشاء قناة الإشعارات (مطلوب لـ Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for chat messages"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "✅ Notification channel created")
        }
    }
}