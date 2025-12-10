package com.example.myapplication.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun editProfileScreen(
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

    var showLogoutDialog by remember { mutableStateOf(false) }

    val currentUser = auth.currentUser
    val uid = currentUser?.uid

    val purple = Color(0xFF9B5FFF)
    val bg = Color(0xFF0B0B23)

    fun encodeImageToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            val outputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, outputStream)
            val bytes = outputStream.toByteArray()
            android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }

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
                        db.collection("users").document(uid)
                            .update("avatarBase64", base64)
                            .await()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = purple
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopBarNeon(
                    borderColor = purple,
                    onBackClick = { onBack?.invoke() ?: navController.navigateUp() }
                )

                Spacer(modifier = Modifier.height(30.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Avatar Circle
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .shadow(
                                    elevation = 40.dp,
                                    shape = CircleShape,
                                    ambientColor = purple,
                                    spotColor = purple
                                )
                                .clip(CircleShape)
                                .background(
                                    if (avatarBase64 != null) Color.Transparent else Color.DarkGray
                                )
                                .clickable { launcher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarBase64 != null) {
                                val decodedBytes = android.util.Base64.decode(avatarBase64, android.util.Base64.DEFAULT)
                                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Text(
                                    text = if (username.text.isNotEmpty()) username.text.first().uppercase() else "?",
                                    color = purple,
                                    fontSize = 52.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // Camera Icon Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-4).dp, y = (-4).dp)
                                .size(38.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(purple)
                                .clickable { launcher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Change Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Username Field
                    OutlinedPurpleField(
                        value = username.text,
                        onValueChange = { username = TextFieldValue(it) },
                        placeholder = "Username",
                        icon = R.drawable.ic_user,
                        borderColor = purple
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Email Field (Read-only)
                    OutlinedPurpleField(
                        value = email.text,
                        onValueChange = {},
                        placeholder = "Email",
                        icon = R.drawable.ic_email,
                        borderColor = purple,
                        keyboard = KeyboardOptions(keyboardType = KeyboardType.Email),
                        readOnly = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Phone Field (Read-only)
                    OutlinedPurpleField(
                        value = phone,
                        onValueChange = {},
                        placeholder = "Phone Number",
                        icon = R.drawable.ic_phone,
                        borderColor = purple,
                        keyboard = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        readOnly = true
                    )

                    Spacer(modifier = Modifier.height(30.dp))

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
                        enabled = !saving && !uploading,
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(25.dp),
                                ambientColor = purple,
                                spotColor = purple
                            ),
                        colors = ButtonDefaults.buttonColors(containerColor = purple)
                    ) {
                        if (saving || uploading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Save Changes",
                                fontSize = 18.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = {
                            showLogoutDialog = true
                        },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .shadow(
                                elevation = 15.dp,
                                shape = RoundedCornerShape(25.dp),
                                ambientColor = purple.copy(alpha = 0.6f),
                                spotColor = purple.copy(alpha = 0.6f)
                            ),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = purple),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                    ) {
                        Text(
                            text = "Logout",
                            fontSize = 18.sp,
                            color = purple,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = {
                    showLogoutDialog = false
                    scope.launch {
                        val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        sharedPref.edit().putBoolean("isLoggedIn", false).apply()
                        auth.signOut()
                        navController.navigate("login") {
                            popUpTo("HomeScreen") { inclusive = true }
                        }
                    }
                },
                onDismiss = {
                    showLogoutDialog = false
                },
                purple = purple,
                bg = bg
            )
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    purple: Color,
    bg: Color
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = bg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = CircleShape,
                            ambientColor = purple,
                            spotColor = purple
                        )
                        .clip(CircleShape)
                        .background(purple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👋",
                        fontSize = 40.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "ليه بس خليك شوية؟ 🥺",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "متأكد إنك عايز تطلع من الابليكيشن؟",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = purple),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                    ) {
                        Text(
                            text = "No",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = purple
                        )
                    }

                    // Yes Button
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = purple,
                                spotColor = purple
                            ),
                        colors = ButtonDefaults.buttonColors(containerColor = purple)
                    ) {
                        Text(
                            text = "Yes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopBarNeon(
    borderColor: Color,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        contentAlignment = Alignment.Center
    ) {
        // Back Icon on the left
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    tint = borderColor
                )
            }
        }

        // Center Title with glow
        Text(
            text = "Profile",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.shadow(
                elevation = 25.dp,
                ambientColor = borderColor,
                spotColor = borderColor
            )
        )
    }
}

@Composable
fun OutlinedPurpleField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: Int,
    borderColor: Color,
    keyboard: KeyboardOptions = KeyboardOptions.Default,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        readOnly = readOnly,
        leadingIcon = {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = placeholder,
                tint = borderColor
            )
        },
        placeholder = {
            Text(text = placeholder, color = Color.Gray)
        },
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = keyboard,
        textStyle = LocalTextStyle.current.copy(color = Color.White),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = borderColor,
            disabledBorderColor = borderColor.copy(alpha = 0.5f),
            disabledTextColor = Color.White.copy(alpha = 0.7f)
        )
    )
}