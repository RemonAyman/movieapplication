package com.example.myapplication.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val CHANNEL_ID = "chat_notifications"
        private const val CHANNEL_NAME = "Chat Messages"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // لو في Data payload
        message.data.isNotEmpty().let {
            val title = message.data["title"] ?: "New Message"
            val body = message.data["body"] ?: "You have a new message"
            val senderId = message.data["senderId"]

            showNotification(title, body, senderId)
        }

        // لو في Notification payload
        message.notification?.let {
            showNotification(
                it.title ?: "New Message",
                it.body ?: "You have a new message",
                null
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // ✅ حفظ الـ FCM Token في Firestore
        FirebaseAuth.getInstance().currentUser?.let { user ->
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    Log.d("FCM", "Token saved successfully for user: ${user.uid}")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Failed to save token: ${e.message}")
                }
        }
    }

    private fun showNotification(title: String, body: String, senderId: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // إنشاء Notification Channel (Android 8.0+)
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
            notificationManager.createNotificationChannel(channel)
        }

        // Intent لفتح التطبيق
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            senderId?.let { putExtra("senderId", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // بناء الإشعار
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.movito_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(Random.nextInt(), notification)
    }
}