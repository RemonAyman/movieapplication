package com.example.myapplication.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.data.UserPreferences
import com.example.myapplication.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    onBack: (() -> Unit)? = null
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ✅ ViewModel مربوط بـ UserPreferences
    val userPrefs = remember { UserPreferences(context) }
    val viewModel = remember { ProfileViewModel(userPrefs) }

    val snackbarHostState = remember { SnackbarHostState() }

    var username by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var phone by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }

    val currentUser = auth.currentUser
    val uid = currentUser?.uid

    // ✅ تحميل البيانات من Firestore و DataStore
    suspend fun loadUserData() {
        if (uid != null) {
            try {
                val snapshot = db.collection("users").document(uid).get().await()
                if (snapshot.exists()) {
                    username = TextFieldValue(snapshot.getString("username") ?: "")
                    email = TextFieldValue(snapshot.getString("email") ?: "")
                    phone = snapshot.getString("phone") ?: ""
                    // حفظ محلي
                    viewModel.saveProfile(username.text, email.text, "")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("فشل تحميل البيانات ❌")
            } finally {
                loading = false
            }
        } else {
            loading = false
        }
    }

    // ✅ أول تحميل
    LaunchedEffect(uid) {
        loadUserData()
    }

    // 🧠 Auto Save بعد آخر تعديل بـ 2 ثانية
    LaunchedEffect(username.text) {
        if (!loading && uid != null && username.text.isNotBlank()) {
            delay(2000)
            if (username.text.matches("^[A-Za-zأ-ي\\s]{3,}$".toRegex())) {
                try {
                    db.collection("users").document(uid)
                        .update("username", username.text.trim())
                        .await()

                    // حفظ محلي كمان
                    viewModel.updateProfile(name = username.text.trim())
                    snackbarHostState.showSnackbar("تم الحفظ التلقائي ✅")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("حدث خطأ أثناء الحفظ ❌")
                }
            } else {
                snackbarHostState.showSnackbar("الاسم لازم يكون 3 حروف على الأقل بدون رموز ⚠️")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    onBack?.let {
                        IconButton(onClick = it) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            loading = true
                            loadUserData()
                            snackbarHostState.showSnackbar("تم تحديث البيانات 🔄")
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 🟣 Avatar أول حرف من الاسم + ظل خفيف
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (username.text.isNotEmpty()) username.text.first().uppercase() else "?",
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {},
                    label = { Text("Email") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = TextFieldValue(phone),
                    onValueChange = {},
                    label = { Text("Phone") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (uid == null) return@Button

                        if (!username.text.matches("^[A-Za-zأ-ي\\s]{3,}$".toRegex())) {
                            scope.launch {
                                snackbarHostState.showSnackbar("الاسم لازم يكون 3 حروف على الأقل بدون رموز ⚠️")
                            }
                            return@Button
                        }

                        scope.launch {
                            saving = true
                            try {
                                db.collection("users").document(uid)
                                    .update("username", username.text.trim())
                                    .await()
                                viewModel.updateProfile(name = username.text.trim())
                                snackbarHostState.showSnackbar("تم حفظ التغييرات ✅")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("فشل الحفظ ❌")
                            } finally {
                                saving = false
                            }
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (saving) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                    else Text("Save Changes")
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            viewModel.clearUserData()
                            auth.signOut()
                            navController.navigate("login") {
                                popUpTo("movies") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
            }
        }
    }
}
