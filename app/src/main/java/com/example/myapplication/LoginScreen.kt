package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Login", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = identifier,
                            onValueChange = { identifier = it },
                            label = { Text("Email, Username or Phone") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { navController.navigate("resetPassword") },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Forgot Password?")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                when {
                                    identifier.isEmpty() || password.isEmpty() -> {
                                        error = "All fields are required"
                                        showSnackbar = true
                                    }
                                    else -> {
                                        loading = true
                                        val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
                                        val phonePattern = Regex("^01[0-9]{9}$")

                                        fun onLoginSuccess() {
                                            // هنا نحدث SharedPreferences بعد نجاح الدخول
                                            sharedPref.edit().putBoolean("isLoggedIn", true).apply()
                                            navController.navigate("HomeScreen") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }

                                        when {
                                            emailPattern.matches(identifier) -> {
                                                auth.signInWithEmailAndPassword(identifier, password)
                                                    .addOnCompleteListener { task ->
                                                        loading = false
                                                        if (task.isSuccessful) {
                                                            onLoginSuccess()
                                                        } else {
                                                            error = task.exception?.localizedMessage ?: "Login failed"
                                                            showSnackbar = true
                                                        }
                                                    }
                                            }

                                            phonePattern.matches(identifier) -> {
                                                db.collection("users")
                                                    .whereEqualTo("phone", identifier)
                                                    .get()
                                                    .addOnSuccessListener { result ->
                                                        loading = false
                                                        if (!result.isEmpty) {
                                                            val email = result.documents[0].getString("email") ?: ""
                                                            auth.signInWithEmailAndPassword(email, password)
                                                                .addOnCompleteListener { task ->
                                                                    if (task.isSuccessful) {
                                                                        onLoginSuccess()
                                                                    } else {
                                                                        error = "Invalid password or user"
                                                                        showSnackbar = true
                                                                    }
                                                                }
                                                        } else {
                                                            error = "Phone not found"
                                                            showSnackbar = true
                                                        }
                                                    }
                                                    .addOnFailureListener {
                                                        loading = false
                                                        error = "Error checking phone"
                                                        showSnackbar = true
                                                    }
                                            }

                                            else -> {
                                                db.collection("users")
                                                    .whereEqualTo("username", identifier)
                                                    .get()
                                                    .addOnSuccessListener { result ->
                                                        loading = false
                                                        if (!result.isEmpty) {
                                                            val email = result.documents[0].getString("email") ?: ""
                                                            auth.signInWithEmailAndPassword(email, password)
                                                                .addOnCompleteListener { task ->
                                                                    if (task.isSuccessful) {
                                                                        onLoginSuccess()
                                                                    } else {
                                                                        error = "Invalid password or user"
                                                                        showSnackbar = true
                                                                    }
                                                                }
                                                        } else {
                                                            error = "Username not found"
                                                            showSnackbar = true
                                                        }
                                                    }
                                                    .addOnFailureListener {
                                                        loading = false
                                                        error = "Error checking username"
                                                        showSnackbar = true
                                                    }
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !loading
                        ) {
                            if (loading)
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            else
                                Text("Login")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(onClick = { navController.navigate("signup") }) {
                            Text("Don’t have an account? Sign up")
                        }
                    }
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
