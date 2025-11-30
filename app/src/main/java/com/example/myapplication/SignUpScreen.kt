package com.example.myapplication

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// 🎨 Colors
val DarkBg = Color(0xFF0C0F2A)
val NeonGlow = Color(0xFF885CFF)
val TextColor = Color(0xFFEDE6FF)

// -------------------- Neon UI Components --------------------

// 🟣 Title
@Composable
fun NeonTitle(title: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = title,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            color = TextColor,
            style = TextStyle(
                shadow = Shadow(
                    color = NeonGlow.copy(alpha = 0.9f),
                    blurRadius = 45f
                )
            ),
            textAlign = TextAlign.Center
        )
    }
}

// 🔥 Neon Box - container with neon border + glow shadow
@Composable
fun NeonBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .border(2.dp, NeonGlow, RoundedCornerShape(22.dp))
            .shadow(
                elevation = 18.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = NeonGlow.copy(0.6f),
                spotColor = NeonGlow.copy(0.6f)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        content()
    }
}

// 📝 Neon Input (uses ImageVector leading icon)
@Composable
fun NeonInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector
) {
    NeonBox(
        Modifier
            .fillMaxWidth()
            .height(65.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeonGlow
                )
            },
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFFBCAFFF),
                    fontSize = 17.sp
                )
            },
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 17.sp
            ),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = NeonGlow,
                focusedLabelColor = NeonGlow,
                unfocusedLabelColor = Color(0xFFBCAFFF),
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
        )
    }
}

// 🔑 Neon Password Input (with eye toggle)
@Composable
fun NeonPassword(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector = Icons.Default.Lock
) {
    var show by remember { mutableStateOf(false) }

    NeonBox(
        Modifier
            .fillMaxWidth()
            .height(65.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = {
                Icon(imageVector = leadingIcon, contentDescription = null, tint = NeonGlow)
            },
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFFBCAFFF),
                    fontSize = 17.sp
                )
            },
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 17.sp
            ),
            singleLine = true,
            visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { show = !show }) {
                    Icon(
                        imageVector = if (show) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = NeonGlow
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = NeonGlow,
                focusedLabelColor = NeonGlow,
                unfocusedLabelColor = Color(0xFFBCAFFF),
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
        )
    }
}

// 🟣 Neon Button - supports loading & enabled
@Composable
fun NeonButton(text: String, onClick: () -> Unit, loading: Boolean = false, enabled: Boolean = true) {
    NeonBox(
        Modifier
            .fillMaxWidth()
            .height(65.dp)
            .clickable(enabled = enabled && !loading) { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = TextColor
                )
            } else {
                Text(text, color = TextColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------- SignUp Screen --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var error by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                NeonTitle("Sign Up")
                Spacer(modifier = Modifier.height(24.dp))

                NeonInput(username, { username = it }, "Username", Icons.Default.Person)
                Spacer(modifier = Modifier.height(16.dp))

                NeonInput(email, { email = it }, "Email", Icons.Default.Email)
                Spacer(modifier = Modifier.height(16.dp))

                NeonInput(phone, { phone = it }, "Phone Number", Icons.Default.Phone)
                Spacer(modifier = Modifier.height(16.dp))

                NeonPassword(password, { password = it }, "Password")
                Spacer(modifier = Modifier.height(16.dp))

                NeonPassword(confirmPassword, { confirmPassword = it }, "Confirm Password")
                Spacer(modifier = Modifier.height(24.dp))

                val onSignUpClick: () -> Unit = {
                when {
                        username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() -> {
                            error = "All fields are required"
                            showSnackbar = true
                        }

                        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                            error = "Invalid email address"
                            showSnackbar = true
                        }

                        !phone.matches(Regex("^01[0-9]{9}$")) -> {
                            error = "Phone must start with 01 and be 11 digits"
                            showSnackbar = true
                        }

                        password != confirmPassword -> {
                            error = "Passwords do not match"
                            showSnackbar = true
                        }

                        else -> {
                            loading = true
                            fun onSignUpSuccess() {
                                sharedPref.edit().putBoolean("isLoggedIn", true).apply()
                                navController.navigate("HomeScreen") {
                                    popUpTo("signup") { inclusive = true }
                                }
                            }

                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    loading = false
                                    if (task.isSuccessful) {
                                        val uid = auth.currentUser?.uid ?: ""
                                        val user = hashMapOf(
                                            "username" to username,
                                            "email" to email,
                                            "phone" to phone,
                                            "password" to password,
                                            "uid" to uid
                                        )

                                        db.collection("users").document(uid)
                                            .set(user)
                                            .addOnSuccessListener {
                                                onSignUpSuccess()
                                            }
                                            .addOnFailureListener {
                                                error = "Failed to save user data"
                                                showSnackbar = true
                                            }
                                    } else {
                                        error = task.exception?.localizedMessage ?: "Signup failed"
                                        showSnackbar = true
                                    }
                                }
                        }
                    }
                }

                NeonButton(text = "Sign Up", onClick = onSignUpClick, loading = loading, enabled = !loading)
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { navController.navigate("login") }) {
                    Text("Already have an account? Log in", color = TextColor)
                }
            }

            if (showSnackbar && error.isNotEmpty()) {
                LaunchedEffect(error) {
                    snackbarHostState.showSnackbar(error)
                    showSnackbar = false
                }
            }
        }
    }
}

