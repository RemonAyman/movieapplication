package com.example.myapplication.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
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

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "chat_notifications"
        private const val CHANNEL_NAME = "Chat Messages"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")

        // حفظ الـ Token في Firestore
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d(TAG, "FCM Token saved successfully")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to save FCM Token", e)
                }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")

        // استخراج البيانات من الـ Notification
        val data = message.data
        val title = data["title"] ?: "New Message"
        val body = data["body"] ?: ""
        val chatId = data["chatId"] ?: ""
        val isGroup = data["isGroup"]?.toBoolean() ?: false
        val senderAvatar = data["senderAvatar"] ?: ""

        // عرض الإشعار
        showNotification(title, body, chatId, isGroup, senderAvatar)
    }

    private fun showNotification(
        title: String,
        body: String,
        chatId: String,
        isGroup: Boolean,
        senderAvatar: String
    ) {
        // إنشاء قناة الإشعارات (Android 8.0+)
        createNotificationChannel()

        // Intent للانتقال إلى الشات عند الضغط على الإشعار
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("chatId", chatId)
            putExtra("isGroup", isGroup)
            putExtra("openChat", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            chatId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // تحويل الـ Avatar من Base64 إلى Bitmap (إن وُجد)
        val largeIcon = try {
            if (senderAvatar.isNotEmpty()) {
                val bytes = Base64.decode(senderAvatar, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding avatar", e)
            null
        }

        // بناء الإشعار
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.movito_logo) // تأكد من إضافة الأيقونة
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // إضافة صورة المرسل إذا كانت موجودة
        if (largeIcon != null) {
            notificationBuilder.setLargeIcon(largeIcon)
        }

        // إضافة الصوت والاهتزاز
        notificationBuilder.setDefaults(NotificationCompat.DEFAULT_ALL)

        // عرض الإشعار
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(chatId.hashCode(), notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new chat messages"
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}