package com.example.myapplication

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    onLoginSuccess: () -> Unit // ✅ Callback بدل navigate مباشرة
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                        identifier.isEmpty() || password.isEmpty() -> {
                            error = "All fields are required"
                            showSnackbar = true
                        }
                        else -> {
                            loading = true
                            val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
                            val phonePattern = Regex("^01[0-9]{9}$")

                            fun handleLoginSuccess() {
                                sharedPref.edit().putBoolean("isLoggedIn", true).apply()
                                onLoginSuccess() // ✅ استخدم callback
                            }

                            when {
                                emailPattern.matches(identifier) -> {
                                    auth.signInWithEmailAndPassword(identifier, password)
                                        .addOnCompleteListener { task ->
                                            loading = false
                                            if (task.isSuccessful) handleLoginSuccess()
                                            else {
                                                error = task.exception?.localizedMessage ?: "Login failed"
                                                showSnackbar = true
                                            }
                                        }
                                }
                                phonePattern.matches(identifier) -> {
                                    db.collection("users").whereEqualTo("phone", identifier)
                                        .get().addOnSuccessListener { result ->
                                            loading = false
                                            if (!result.isEmpty) {
                                                val email = result.documents[0].getString("email") ?: ""
                                                auth.signInWithEmailAndPassword(email, password)
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) handleLoginSuccess()
                                                        else {
                                                            error = "Invalid password or user"
                                                            showSnackbar = true
                                                        }
                                                    }
                                            } else {
                                                error = "Phone not found"
                                                showSnackbar = true
                                            }
                                        }.addOnFailureListener {
                                            loading = false
                                            error = "Error checking phone"
                                            showSnackbar = true
                                        }
                                }
                                else -> {
                                    db.collection("users").whereEqualTo("username", identifier)
                                        .get().addOnSuccessListener { result ->
                                            loading = false
                                            if (!result.isEmpty) {
                                                val email = result.documents[0].getString("email") ?: ""
                                                auth.signInWithEmailAndPassword(email, password)
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) handleLoginSuccess()
                                                        else {
                                                            error = "Invalid password or user"
                                                            showSnackbar = true
                                                        }
                                                    }
                                            } else {
                                                error = "Username not found"
                                                showSnackbar = true
                                            }
                                        }.addOnFailureListener {
                                            loading = false
                                            error = "Error checking username"
                                            showSnackbar = true
                                        }
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
                    Text("Don’t have an account? Sign up", color = TextColor)
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
