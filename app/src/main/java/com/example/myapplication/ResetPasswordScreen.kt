package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(navController: NavHostController) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    var email by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.DarkBg)
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                NeonTitle("Reset Password")
                Spacer(modifier = Modifier.height(24.dp))

                NeonInput(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Enter your email",
                    icon = Icons.Default.Email
                )
                Spacer(modifier = Modifier.height(24.dp))

                val onResetClick: () -> Unit = {
                    when {
                        email.isEmpty() -> {
                            error = "Please enter your email"
                            showSnackbar = true
                        }
                        else -> {
                            loading = true
                            auth.sendPasswordResetEmail(email)
                                .addOnCompleteListener { task ->
                                    loading = false
                                    if (task.isSuccessful) {
                                        scope.launch {
                                            error = "✅ Password reset email sent!"
                                            showSnackbar = true
                                            delay(2000)
                                            navController.navigate("login") {
                                                popUpTo("resetPassword") { inclusive = true }
                                            }
                                        }
                                    } else {
                                        error = task.exception?.localizedMessage ?: "Failed to send email"
                                        showSnackbar = true
                                    }
                                }
                        }
                    }
                }

                NeonButton(
                    text = "Send Reset Link",
                    onClick = onResetClick,
                    loading = loading,
                    enabled = !loading
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = {
                    navController.navigate("login") {
                        popUpTo("resetPassword") { inclusive = true }
                    }
                }) {
                    Text("Back to Login", color = AppColors.TextColor)
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

