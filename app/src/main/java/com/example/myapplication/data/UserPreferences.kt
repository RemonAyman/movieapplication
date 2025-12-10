package com.example.myapplication.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_IMAGE = stringPreferencesKey("user_image")
    }

    suspend fun saveUserData(name: String, email: String, image: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_NAME] = name
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_USER_IMAGE] = image
        }
    }

    suspend fun updateProfile(
        name: String? = null,
        email: String? = null,
        image: String? = null
    ): Boolean {
        return try {
            context.dataStore.edit { prefs ->
                name?.let { prefs[KEY_USER_NAME] = it }
                email?.let { prefs[KEY_USER_EMAIL] = it }
                image?.let { prefs[KEY_USER_IMAGE] = it }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: ""
    }

    val userEmail: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL] ?: ""
    }

    val userImage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_IMAGE] ?: ""
    }

    suspend fun getUserData(): Triple<String, String, String> {
        val prefs = context.dataStore.data.first()
        val name = prefs[KEY_USER_NAME] ?: ""
        val email = prefs[KEY_USER_EMAIL] ?: ""
        val image = prefs[KEY_USER_IMAGE] ?: ""
        return Triple(name, email, image)
    }

    suspend fun refreshFromFirebase(userId: String, db: FirebaseFirestore) {
        try {
            val snapshot = db.collection("users").document(userId).get().await()
            if (snapshot.exists()) {
                val name = snapshot.getString("username") ?: ""
                val email = snapshot.getString("email") ?: ""
                val image = snapshot.getString("image") ?: ""
                saveUserData(name, email, image)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun autoSaveToFirebase(userId: String, db: FirebaseFirestore, name: String): Boolean {
        return try {
            db.collection("users").document(userId)
                .update("username", name.trim())
                .await()

            updateProfile(name = name)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun clearData() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun isProfileComplete(): Boolean {
        val prefs = context.dataStore.data.first()
        val name = prefs[KEY_USER_NAME]
        val email = prefs[KEY_USER_EMAIL]
        return !name.isNullOrEmpty() && !email.isNullOrEmpty()
    }
}
