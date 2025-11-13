package com.example.myapplication.ui.screens.friendDetail

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.AppColors
import com.example.myapplication.ui.screens.friends.FriendsViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.painter.BitmapPainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    friendId: String,
    viewModel: FriendsViewModel,
    isSearchMode: Boolean = false, // لو جايه من Search/Add Friend
    onBack: (() -> Unit)? = null
) {
    val friend by viewModel.friendDetail.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(friendId) {
        loading = true
        viewModel.loadFriendDetail(friendId)
        loading = false
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isSearchMode) "Add Friend" else "Friend Details",
                        color = AppColors.TextColor,
                        style = MaterialTheme.typography.titleLarge
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
                            viewModel.loadFriendDetail(friendId)
                            loading = false
                            snackbarHostState.showSnackbar("✅ Data refreshed")
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AppColors.NeonGlow)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.DarkBg)
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
                friend?.let { f ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(AppColors.NeonGlow.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (f.avatarBase64.isNotEmpty()) {
                                val imageBitmap: ImageBitmap? = try {
                                    val bytes = Base64.decode(f.avatarBase64, Base64.DEFAULT)
                                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    bitmap?.asImageBitmap()
                                } catch (e: Exception) {
                                    null
                                }

                                if (imageBitmap != null) {
                                    Image(
                                        painter = BitmapPainter(imageBitmap),
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = f.username.firstOrNull()?.uppercase() ?: "?",
                                        color = AppColors.NeonGlow,
                                        fontSize = 48.sp
                                    )
                                }
                            } else {
                                Text(
                                    text = f.username.firstOrNull()?.uppercase() ?: "?",
                                    color = AppColors.NeonGlow,
                                    fontSize = 48.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = f.username,
                            color = AppColors.TextColor,
                            fontSize = 26.sp,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "📧 Email: ${f.email}",
                            color = AppColors.TextColor,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "📱 Phone: ${f.phone}",
                            color = AppColors.TextColor,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (isSearchMode) {
                                // زر Add Friend
                                Button(
                                    onClick = {
                                        scope.launch {
                                            viewModel.sendFriendRequest(f.uid)
                                            snackbarHostState.showSnackbar("✅ Friend request sent to ${f.username}")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.NeonGlow)
                                ) {
                                    Text("Add Friend", color = AppColors.TextColor)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            viewModel.acceptFriend(f.uid)
                                            snackbarHostState.showSnackbar("✅ ${f.username} accepted")
                                            viewModel.loadFriendsList()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.NeonGlow)
                                ) {
                                    Text("Accept", color = AppColors.TextColor)
                                }

                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            viewModel.removeFriend(f.uid)
                                            snackbarHostState.showSnackbar("❌ ${f.username} removed")
                                            viewModel.loadFriendsList()
                                        }
                                    },
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        width = 1.5.dp,
                                        brush = SolidColor(AppColors.NeonGlow)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.NeonGlow)
                                ) {
                                    Text("Remove", color = AppColors.NeonGlow)
                                }
                            }
                        }
                    }
                } ?: run {
                    Text(
                        text = "❌ Friend not found.",
                        color = AppColors.TextColor,
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
