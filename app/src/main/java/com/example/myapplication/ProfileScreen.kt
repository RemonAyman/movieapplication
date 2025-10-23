package com.example.myapplication.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.data.UserPreferences
import com.example.myapplication.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navToLogin: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userPrefs = remember { UserPreferences(context) }
    val snackbarHostState = remember { SnackbarHostState() }

    val viewModel: ProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(userPrefs) as T
            }
        }
    )

    val userName by viewModel.userName.collectAsStateWithLifecycle(initialValue = "")
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle(initialValue = "")
    val userImage by viewModel.userImage.collectAsStateWithLifecycle(initialValue = "")

    var nameField by remember { mutableStateOf(TextFieldValue("")) }
    var emailField by remember { mutableStateOf(TextFieldValue("")) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val defaultImageUrl = "https://cdn-icons-png.flaticon.com/512/149/149071.png"

    LaunchedEffect(userName, userEmail, userImage) {
        nameField = TextFieldValue(userName)
        emailField = TextFieldValue(userEmail)
    }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) selectedImageUri = uri
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
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .clickable { pickImage.launch("image/*") }
            ) {
                val painter = rememberAsyncImagePainter(
                    selectedImageUri?.toString()
                        ?: if (userImage.isNotEmpty()) userImage else defaultImageUrl
                )
                Image(
                    painter = painter,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = nameField,
                onValueChange = { nameField = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = emailField,
                onValueChange = { emailField = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (nameField.text.isBlank() || emailField.text.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("يرجى ملء كل الحقول ⚠️")
                        }
                        return@Button
                    }

                    val imageToSave = selectedImageUri?.toString() ?: userImage
                    scope.launch {
                        viewModel.saveProfile(
                            name = nameField.text.trim(),
                            email = emailField.text.trim(),
                            image = imageToSave
                        )
                        snackbarHostState.showSnackbar("تم حفظ التغييرات بنجاح ✅")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                elevation = ButtonDefaults.buttonElevation(6.dp)
            ) {
                Text("Save Changes")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        viewModel.clearUserData()
                        snackbarHostState.showSnackbar("تم تسجيل الخروج بنجاح 👋")
                        navToLogin()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}
