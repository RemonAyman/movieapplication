package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.UserPreferences
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ProfileViewModel(private val userPrefs: UserPreferences) : ViewModel() {

    // ✅ روابط مباشرة للبيانات من الـ DataStore
    val userName: Flow<String> = userPrefs.userName
    val userEmail: Flow<String> = userPrefs.userEmail
    val userImage: Flow<String> = userPrefs.userImage

    // ✅ حفظ كامل للبيانات (اسم - إيميل - صورة)
    fun saveProfile(name: String, email: String, image: String) {
        if (name.isBlank() || email.isBlank()) return
        viewModelScope.launch {
            userPrefs.saveUserData(name, email, image)
        }
    }

    // ✅ تحديث جزئي (لو المستخدم غيّر اسمه أو صورته بس)
    fun updateProfile(name: String? = null, email: String? = null, image: String? = null) {
        viewModelScope.launch {
            val (currentName, currentEmail, currentImage) = userPrefs.getUserData()

            val finalName = name ?: currentName
            val finalEmail = email ?: currentEmail
            val finalImage = image ?: currentImage

            userPrefs.updateProfile(finalName, finalEmail, finalImage)
        }
    }

    // ✅ حفظ تلقائي للـ Username في Firebase + DataStore
    fun autoSaveUsername(userId: String, db: FirebaseFirestore, newName: String) {
        // نمنع الرموز أو الاسم الفاضي
        if (newName.isBlank() || !newName.matches(Regex("^[a-zA-Z0-9أ-ي ]+$"))) return

        viewModelScope.launch {
            userPrefs.autoSaveToFirebase(userId, db, newName)
        }
    }

    // ✅ تحديث البيانات من Firebase (زر Refresh)
    fun refreshProfileFromFirebase(userId: String, db: FirebaseFirestore) {
        viewModelScope.launch {
            userPrefs.refreshFromFirebase(userId, db)
        }
    }

    // ✅ مسح بيانات المستخدم (تستخدم في تسجيل الخروج)
    fun clearUserData() {
        viewModelScope.launch {
            userPrefs.clearData()
        }
    }

    // ✅ فحص هل المستخدم مكمل بياناته ولا لأ
    suspend fun isProfileComplete(): Boolean {
        return userPrefs.isProfileComplete()
    }
}
