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

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "chat_notifications"
        private const val CHANNEL_NAME = "Chat Messages"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "✅ FCM Service Created")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "📩 Message received from: ${remoteMessage.from}")
        Log.d(TAG, "📬 Data payload: ${remoteMessage.data}")
        Log.d(TAG, "📨 Notification: ${remoteMessage.notification}")

        val data = remoteMessage.data
        val title = data["title"] ?: remoteMessage.notification?.title ?: "New Message"
        val body = data["body"] ?: remoteMessage.notification?.body ?: ""
        val chatId = data["chatId"] ?: ""
        val isGroup = data["isGroup"]?.toBoolean() ?: false
        val senderAvatar = data["senderAvatar"] ?: ""

        Log.d(TAG, "📬 Title: $title")
        Log.d(TAG, "📝 Body: $body")
        Log.d(TAG, "💬 Chat ID: $chatId")
        Log.d(TAG, "👥 Is Group: $isGroup")

        if (chatId.isNotEmpty()) {
            showNotification(title, body, chatId, isGroup, senderAvatar)
        } else {
            Log.w(TAG, "⚠️ No chatId found in notification")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "🔑 New FCM Token: ${token.take(20)}...")

        updateFCMTokenInFirestore(token)
    }

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

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUserId)
                        .set(
                            hashMapOf("fcmToken" to token),
                            com.google.firebase.firestore.SetOptions.merge()
                        )
                        .addOnSuccessListener {
                            Log.d(TAG, "✅ FCM Token created in Firestore")
                        }
                        .addOnFailureListener { e2 ->
                            Log.e(TAG, "❌ Failed to create FCM Token field", e2)
                        }
                }
        } else {
            Log.w(TAG, "⚠️ No user logged in, cannot update FCM Token")
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        chatId: String,
        isGroup: Boolean,
        senderAvatar: String
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("openChat", true)
            putExtra("chatId", chatId)
            putExtra("isGroup", isGroup)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            chatId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = try {
            if (senderAvatar.isNotEmpty()) {
                val bytes = Base64.decode(senderAvatar, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to decode avatar", e)
            null
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
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

        if (largeIcon != null) {
            notificationBuilder.setLargeIcon(largeIcon)
        }

        notificationManager.notify(chatId.hashCode(), notificationBuilder.build())
        Log.d(TAG, "✅ Notification displayed successfully")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for chat messages"
                enableLights(true)
                lightColor = 0xFF9B5DE5.toInt()
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
            Log.d(TAG, "✅ Notification channel created")
        }
    }
}