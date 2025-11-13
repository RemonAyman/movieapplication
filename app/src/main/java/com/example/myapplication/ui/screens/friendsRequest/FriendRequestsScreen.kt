package com.example.myapplication.ui.screens.friendsRequest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.AppColors
import com.example.myapplication.data.remote.firebase.models.UserDataModel
import com.example.myapplication.ui.screens.friends.FriendsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestsScreen(
    viewModel: FriendsViewModel,
    onBack: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val requests by viewModel.friendRequests.collectAsState()
    val loading by viewModel.loadingState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFriendRequests()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Friend Requests",
                        color = AppColors.TextColor,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.headlineSmall
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
                            viewModel.loadFriendRequests()
                            snackbarHostState.showSnackbar("✅ Requests refreshed")
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
                if (requests.isEmpty()) {
                    Text(
                        text = "No friend requests available.",
                        color = AppColors.TextColor.copy(alpha = 0.8f),
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 16.sp
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(requests) { friend ->
                            FriendRequestCard(
                                friend = friend,
                                onAccept = {
                                    scope.launch {
                                        viewModel.acceptFriend(friend.uid)
                                        snackbarHostState.showSnackbar("✅ ${friend.username} accepted")
                                    }
                                },
                                onDecline = {
                                    scope.launch {
                                        viewModel.declineFriendRequest(friend.uid)
                                        snackbarHostState.showSnackbar("❌ ${friend.username} declined")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendRequestCard(
    friend: UserDataModel,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape = MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = AppColors.DarkBg.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape)
                    .background(AppColors.NeonGlow.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                if (friend.avatarBase64.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(friend.avatarBase64),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = friend.username.firstOrNull()?.uppercase() ?: "?",
                        color = AppColors.NeonGlow,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.username,
                    color = AppColors.TextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = friend.email,
                    color = AppColors.TextColor.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.NeonGlow)
                ) {
                    Text("Accept", color = AppColors.TextColor)
                }
                OutlinedButton(
                    onClick = onDecline,
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.5.dp,
                        brush = SolidColor(AppColors.NeonGlow)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.NeonGlow
                    )
                ) {
                    Text("Decline", color = AppColors.NeonGlow)
                }
            }
        }
    }
}
