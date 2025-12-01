package com.example.myapplication.appConstant

import com.google.firebase.auth.FirebaseAuth

object AppConstants {
    val CURRENT_USER_ID: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
}
