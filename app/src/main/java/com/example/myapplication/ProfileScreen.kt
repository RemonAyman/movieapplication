package com.example.myapplication.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.AppColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Base64

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
    var avatarBase64 by remember { mutableStateOf<String?>(null) }

    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf(false) }

    val currentUser = auth.currentUser
    val uid = currentUser?.uid

    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = AppColors.DarkBg.copy(alpha = 0.9f),
        unfocusedContainerColor = AppColors.DarkBg.copy(alpha = 0.8f),
        focusedTextColor = AppColors.TextColor,
        unfocusedTextColor = AppColors.TextColor.copy(alpha = 0.8f),
        cursorColor = AppColors.NeonGlow,
        focusedIndicatorColor = AppColors.NeonGlow,
        unfocusedIndicatorColor = AppColors.TextColor.copy(alpha = 0.4f),
        focusedLabelColor = AppColors.NeonGlow,
        unfocusedLabelColor = AppColors.TextColor.copy(alpha = 0.7f)
    )

    // ✅ Function لتحويل الصورة إلى Base64
    fun encodeImageToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            val outputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            val bytes = outputStream.toByteArray()
            android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }

    // ✅ اختيار صورة + تحويلها Base64 + حفظها
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && uid != null) {
            scope.launch {
                uploading = true
                try {
                    val base64 = encodeImageToBase64(context, uri)
                    if (base64 != null) {
                        avatarBase64 = base64

                        // حفظ في Firestore
                        db.collection("users").document(uid)
                            .update("avatarBase64", base64)
                            .await()

                        // حفظ محلي
                        val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        sharedPref.edit().putString("avatarBase64", base64).apply()

                        snackbarHostState.showSnackbar("✅ الصورة اتسجلت")
                    } else {
                        snackbarHostState.showSnackbar("❌ فشل تحويل الصورة")
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("❌ فشل حفظ الصورة")
                } finally {
                    uploading = false
                }
            }
        }
    }

    // ✅ تحميل بيانات المستخدم
    suspend fun loadUserData() {
        if (uid != null) {
            try {
                val snapshot = db.collection("users").document(uid).get().await()
                if (snapshot.exists()) {
                    username = TextFieldValue(snapshot.getString("username") ?: "")
                    email = TextFieldValue(snapshot.getString("email") ?: "")
                    phone = snapshot.getString("phone") ?: ""

                    avatarBase64 = snapshot.getString("avatarBase64")
                        ?: context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                            .getString("avatarBase64", null)
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("❌ فشل تحميل البيانات")
            } finally {
                loading = false
            }
        } else loading = false
    }

    LaunchedEffect(uid) { loadUserData() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        color = AppColors.TextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    onBack?.let {
                        IconButton(onClick = it) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.TextColor)
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
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AppColors.NeonGlow)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.DarkBg
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.DarkBg)
                .padding(padding)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AppColors.NeonGlow
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ✅ Avatar Base64
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(AppColors.NeonGlow.copy(alpha = 0.2f))
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarBase64 != null) {
                            val decodedBytes = android.util.Base64.decode(avatarBase64, android.util.Base64.DEFAULT)
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = if (username.text.isNotEmpty()) username.text.first().uppercase() else "?",
                                color = AppColors.NeonGlow,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {},
                        label = { Text("Email") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = TextFieldValue(phone),
                        onValueChange = {},
                        label = { Text("Phone") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save button
                    Button(
                        onClick = {
                            if (uid == null) return@Button

                            if (!username.text.matches("^[A-Za-zأ-ي\\s]{3,}$".toRegex())) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("⚠️ الاسم لازم يكون 3 حروف")
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
                            containerColor = AppColors.NeonGlow
                        )
                    ) {
                        if (saving || uploading)
                            CircularProgressIndicator(color = AppColors.TextColor, strokeWidth = 2.dp)
                        else
                            Text("Save Changes", color = AppColors.TextColor)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Logout button
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
                            contentColor = AppColors.TextColor
                        )
                    ) {
                        Text("Logout", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
