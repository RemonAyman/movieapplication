package com.example.myapplication.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// ✅ تعريف الـ DataStore بشكل Singleton
val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_IMAGE = stringPreferencesKey("user_image")
    }

    // ✅ حفظ كل بيانات المستخدم (اسم - إيميل - صورة)
    suspend fun saveUserData(name: String, email: String, image: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_USER_IMAGE] = image
        }
    }

    // ✅ تحديث جزئي (مثلاً الاسم أو الصورة فقط)
    suspend fun updateProfile(name: String? = null, email: String? = null, image: String? = null) {
        context.dataStore.edit { prefs ->
            name?.let { prefs[KEY_USER_NAME] = it }
            email?.let { prefs[KEY_USER_EMAIL] = it }
            image?.let { prefs[KEY_USER_IMAGE] = it }
        }
    }

    // ✅ قراءة البيانات بشكل Reactive عبر Flow
    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: ""
    }

    val userEmail: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL] ?: ""
    }

    val userImage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_IMAGE] ?: ""
    }

    // ✅ دالة إضافية: ترجع القيم الحالية بشكل مباشر (في كوروترين)
    suspend fun getUserData(): Triple<String, String, String> {
        val prefs = context.dataStore.data.first()
        val name = prefs[KEY_USER_NAME] ?: ""
        val email = prefs[KEY_USER_EMAIL] ?: ""
        val image = prefs[KEY_USER_IMAGE] ?: ""
        return Triple(name, email, image)
    }

    // ✅ مسح بيانات المستخدم (تستخدم في Logout)
    suspend fun clearData() {
        context.dataStore.edit { it.clear() }
    }

    // ✅ فحص إذا كان المستخدم مكمل بياناته أو لأ
    suspend fun isProfileComplete(): Boolean {
        val prefs = context.dataStore.data.first()
        val name = prefs[KEY_USER_NAME]
        val email = prefs[KEY_USER_EMAIL]
        return !name.isNullOrEmpty() && !email.isNullOrEmpty()
    }
}
