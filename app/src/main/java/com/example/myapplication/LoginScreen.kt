package com.example.myapplication

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var error by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ دالة مساعدة للتحقق من نوع المدخل
    fun isEmail(input: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
    }

    fun isPhone(input: String): Boolean {
        return input.matches(Regex("^01[0-9]{9}$"))
    }

    // ✅ دالة موحدة للـ Login
    suspend fun performLogin(emailToUse: String, passwordToUse: String): Boolean {
        return try {
            Log.d("LoginScreen", "Attempting login with email: $emailToUse")
            auth.signInWithEmailAndPassword(emailToUse, passwordToUse).await()
            Log.d("LoginScreen", "Login successful")
            true
        } catch (e: Exception) {
            Log.e("LoginScreen", "Login failed: ${e.message}", e)
            error = when {
                e.message?.contains("password") == true -> "Wrong password"
                e.message?.contains("user") == true -> "User not found"
                e.message?.contains("network") == true -> "Network error"
                else -> e.localizedMessage ?: "Login failed"
            }
            showSnackbar = true
            false
        }
    }

    // ✅ دالة البحث عن Email بناءً على Username أو Phone
    suspend fun findEmailByIdentifier(field: String, value: String): String? {
        return try {
            Log.d("LoginScreen", "Searching for $field: $value")

            val result = db.collection("users")
                .whereEqualTo(field, value)
                .get()
                .await()

            Log.d("LoginScreen", "Search result size: ${result.size()}")

            if (!result.isEmpty) {
                val email = result.documents[0].getString("email")
                Log.d("LoginScreen", "Found email: $email")
                email
            } else {
                error = when (field) {
                    "username" -> "Username not found"
                    "phone" -> "Phone number not found"
                    else -> "User not found"
                }
                showSnackbar = true
                Log.w("LoginScreen", "No user found with $field: $value")
                null
            }
        } catch (e: Exception) {
            Log.e("LoginScreen", "Database error: ${e.message}", e)
            error = when {
                e.message?.contains("PERMISSION_DENIED") == true ->
                    "Database access denied. Please contact support."
                e.message?.contains("network") == true ->
                    "Network error. Check your connection."
                else -> "Database error: ${e.message}"
            }
            showSnackbar = true
            null
        }
    }

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
                NeonTitle("Login")
                Spacer(modifier = Modifier.height(24.dp))

                NeonInput(
                    value = identifier,
                    onValueChange = { identifier = it },
                    placeholder = "Email, Username or Phone",
                    icon = Icons.Default.Person
                )
                Spacer(modifier = Modifier.height(16.dp))

                NeonPassword(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Password"
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { navController.navigate("resetPassword") },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Forgot Password?", color = TextColor)
                }

                Spacer(modifier = Modifier.height(24.dp))

                val onLoginClick: () -> Unit = {
                    when {
                        identifier.trim().isEmpty() || password.isEmpty() -> {
                            error = "All fields are required"
                            showSnackbar = true
                        }
                        else -> {
                            loading = true
                            Log.d("LoginScreen", "Login button clicked with identifier: ${identifier.trim()}")

                            scope.launch {
                                try {
                                    val emailToLogin = when {
                                        // ✅ إذا كان المدخل Email
                                        isEmail(identifier.trim()) -> {
                                            Log.d("LoginScreen", "Detected as email")
                                            identifier.trim()
                                        }

                                        // ✅ إذا كان المدخل Phone
                                        isPhone(identifier.trim()) -> {
                                            Log.d("LoginScreen", "Detected as phone")
                                            findEmailByIdentifier("phone", identifier.trim())
                                        }

                                        // ✅ إذا كان المدخل Username
                                        else -> {
                                            Log.d("LoginScreen", "Detected as username")
                                            findEmailByIdentifier("username", identifier.trim())
                                        }
                                    }

                                    // ✅ محاولة تسجيل الدخول
                                    if (emailToLogin != null) {
                                        val success = performLogin(emailToLogin, password)
                                        if (success) {
                                            sharedPref.edit().putBoolean("isLoggedIn", true).apply()
                                            onLoginSuccess()
                                        }
                                    } else {
                                        Log.w("LoginScreen", "Email lookup returned null")
                                    }
                                } catch (e: Exception) {
                                    Log.e("LoginScreen", "Unexpected error: ${e.message}", e)
                                    error = "Unexpected error: ${e.message}"
                                    showSnackbar = true
                                } finally {
                                    loading = false
                                }
                            }
                        }
                    }
                }

                NeonButton(
                    text = "Login",
                    onClick = onLoginClick,
                    loading = loading,
                    enabled = !loading
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { navController.navigate("signup") }) {
                    Text("Don't have an account? Sign up", color = TextColor)
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
