package com.example.myapplication.data.remote.firebase.models

import com.example.myapplication.ui.screens.favorites.FavoritesItem

data class UserDataModel(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val avatarBase64: String = "",
    val requestStatus: String = "", // "pending", "sent", "" للعرض في UI
    val favoriest: List<FavoritesItem> = emptyList(),
    val fcmToken: String = "" // ✅ FCM Token للإشعارات
)

/**
 * ✅ Extension function لتحديث FCM Token بسهولة
 */
fun UserDataModel.withFcmToken(token: String): UserDataModel {
    return this.copy(fcmToken = token)
}

/**
 * ✅ Extension function للتحقق من صلاحية FCM Token
 */
fun UserDataModel.hasFcmToken(): Boolean {
    return fcmToken.isNotEmpty()
}

/**
 * ✅ Data class لإرسال الإشعارات
 */
data class NotificationData(
    val to: String, // FCM Token
    val priority: String = "high",
    val notification: NotificationPayload,
    val data: Map<String, String> = emptyMap()
)

data class NotificationPayload(
    val title: String,
    val body: String,
    val sound: String = "default",
    val badge: String = "1"
)