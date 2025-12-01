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