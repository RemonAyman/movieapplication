package com.example.myapplication.ui.screens

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.ui.theme.MovitoBackground
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    val snackbarHostState = remember { SnackbarHostState() }

    var username by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var phone by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }

    val currentUser = auth.currentUser
    val uid = currentUser?.uid

    // ✅ ألوان الـ TextField المعدلة لتناسق واضح مع الثيم
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color(0xFF2C1E4A),
        unfocusedContainerColor = Color(0xFF3B2A5E),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White.copy(alpha = 0.9f),
        cursorColor = Color(0xFF9B5DE5),
        focusedIndicatorColor = Color(0xFF9B5DE5),
        unfocusedIndicatorColor = Color.Gray,
        focusedLabelColor = Color(0xFF9B5DE5),
        unfocusedLabelColor = Color.LightGray
    )

    // ✅ تحميل بيانات المستخدم
    suspend fun loadUserData() {
        if (uid != null) {
            try {
                val snapshot = db.collection("users").document(uid).get().await()
                if (snapshot.exists()) {
                    username = TextFieldValue(snapshot.getString("username") ?: "")
                    email = TextFieldValue(snapshot.getString("email") ?: "")
                    phone = snapshot.getString("phone") ?: ""
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("❌ فشل تحميل البيانات")
            } finally {
                loading = false
            }
        } else {
            loading = false
        }
    }

    // ✅ تحميل البيانات عند فتح الشاشة
    LaunchedEffect(uid) {
        loadUserData()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    onBack?.let {
                        IconButton(onClick = it) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            loading = true
                            loadUserData()
                            snackbarHostState.showSnackbar("✅ تم تحديث البيانات")
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2C1E4A)
                )
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MovitoBackground)
                .padding(padding)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ✅ Avatar
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFF4A3A64)),
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

                    // ✅ Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ✅ Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {},
                        label = { Text("Email") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ✅ Phone
                    OutlinedTextField(
                        value = TextFieldValue(phone),
                        onValueChange = {},
                        label = { Text("Phone") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // ✅ زر الحفظ
                    Button(
                        onClick = {
                            if (uid == null) return@Button

                            if (!username.text.matches("^[A-Za-zأ-ي\\s]{3,}$".toRegex())) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("⚠️ الاسم لازم يكون 3 حروف على الأقل")
                                }
                                return@Button
                            }

                            scope.launch {
                                saving = true
                                try {
                                    db.collection("users").document(uid)
                                        .update("username", username.text.trim())
                                        .await()
                                    snackbarHostState.showSnackbar("✅ تم حفظ التغييرات")
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("❌ فشل حفظ التغييرات")
                                } finally {
                                    saving = false
                                }
                            }
                        },
                        enabled = !saving,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9B5DE5)
                        )
                    ) {
                        if (saving)
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                        else
                            Text("Save Changes", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ✅ زر تسجيل الخروج
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                                sharedPref.edit()
                                    .putBoolean("isLoggedIn", false)
                                    .apply()

                                auth.signOut()

                                navController.navigate("login") {
                                    popUpTo("HomeScreen") { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp, brush = SolidColor(Color.White))
                    ) {
                        Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
