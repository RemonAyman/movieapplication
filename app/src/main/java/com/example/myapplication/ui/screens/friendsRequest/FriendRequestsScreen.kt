package com.example.myapplication.ui.screens.friendsRequest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.remote.firebase.models.UserDataModel
import com.example.myapplication.ui.screens.friends.FriendsViewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestsScreen(
    viewModel: FriendsViewModel,
    navController: NavController
) {
    val incoming by viewModel.friendRequests.collectAsState()
    val outgoing by viewModel.sentFriendRequests.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadFriendRequests() }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friend Requests", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("profileMainScreen/$currentUserId") {
                            popUpTo("profileMainScreen/$currentUserId") { inclusive = false }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1330))
            )
        },
        containerColor = Color(0xFF0F0820)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0820))
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Section("Incoming", incoming) { friend ->
                FriendRequestCard(
                    friend,
                    primaryActionText = "Accept",
                    secondaryActionText = "Decline",
                    onPrimaryAction = { scope.launch { viewModel.acceptFriendRequest(friend.uid) } },
                    onSecondaryAction = { scope.launch { viewModel.declineFriendRequest(friend.uid) } }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Section("Outgoing", outgoing) { friend ->
                FriendRequestCard(
                    friend,
                    primaryActionText = "Cancel",
                    secondaryActionText = null,
                    onPrimaryAction = { scope.launch { viewModel.cancelFriendRequest(friend.uid) } },
                    onSecondaryAction = null
                )
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    list: List<UserDataModel>,
    itemContent: @Composable (UserDataModel) -> Unit
) {
    Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    Spacer(modifier = Modifier.height(8.dp))
    if (list.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.CenterStart) {
            Text("No $title requests", color = Color(0xFFBDBDBD))
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(list) { itemContent(it) }
        }
    }
}

@Composable
fun FriendRequestCard(
    friend: UserDataModel,
    primaryActionText: String,
    secondaryActionText: String?,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A1B3D).copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (friend.avatarBase64.isNotEmpty()) {
                        AsyncImage(
                            model = friend.avatarBase64,
                            contentDescription = "avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(friend.username.firstOrNull()?.uppercase() ?: "?", color = Color(0xFF9B5DE5), fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(friend.username, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(friend.email, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onPrimaryAction,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5))
                ) {
                    Text(primaryActionText, color = Color.White)
                }

                if (secondaryActionText != null && onSecondaryAction != null) {
                    OutlinedButton(
                        onClick = onSecondaryAction,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9B5DE5))
                    ) {
                        Text(secondaryActionText, color = Color(0xFF9B5DE5))
                    }
                }
            }
        }
    }
}