package com.example.myapplication.ui.screens.friendDetail

import android.app.Activity
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.screens.friends.FriendsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    UserId: String,
    viewModel: FriendsViewModel,
    onBack: (() -> Unit)? = null,
    onLikesClick: (String) -> Unit,
    onWatchedClick: (String) -> Unit,
    onWatchListClick: (String) -> Unit,
    onRatingsClick: (String) -> Unit,
) {
    val context = LocalContext.current
    val friend by viewModel.friendDetail.collectAsState()
    val friends by viewModel.friendsList.collectAsState()
    val friendRequests by viewModel.friendRequests.collectAsState()
    val sentRequests by viewModel.sentFriendRequests.collectAsState()
    val loading by viewModel.loadingState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load friend detail and lists
    LaunchedEffect(UserId) {
        viewModel.loadFriendDetail(UserId)
        viewModel.loadFriendsList()
        viewModel.loadFriendRequests()
        viewModel.loadAllUsers()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        onBack?.invoke() ?: (context as? Activity)?.onBackPressed()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1330))
            )
        },
        containerColor = Color(0xFF0F0820)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0820))
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (friend == null || loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                friend?.let { f ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .shadow(8.dp, shape = CircleShape)
                                .background(Color(0xFF2A1B3D)),
                            contentAlignment = Alignment.Center
                        ) {
                            val bmp = remember(f.avatarBase64) {
                                try {
                                    if (f.avatarBase64.isNotEmpty()) {
                                        val pureBase64 = f.avatarBase64.substringAfter(",")
                                        val bytes = Base64.decode(pureBase64, Base64.DEFAULT)
                                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        bitmap?.asImageBitmap()
                                    } else null
                                } catch (e: Exception) { null }
                            }

                            if (bmp != null) {
                                Image(
                                    painter = BitmapPainter(bmp),
                                    contentDescription = "avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = f.username.firstOrNull()?.uppercase() ?: "?",
                                    color = Color(0xFF9B5DE5),
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(f.username, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                        Text(f.email, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Text(f.phone, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Compute relationship status
                        val status = remember(friends, friendRequests, sentRequests) {
                            when {
                                friends.any { it.uid == f.uid } -> "friend"
                                sentRequests.any { it.uid == f.uid } -> "sent"
                                friendRequests.any { it.uid == f.uid } -> "incoming"
                                else -> ""
                            }
                        }

                        // Show status in English
                        Text(
                            text = when (status) {
                                "friend" -> "Friend"
                                "sent" -> "Request Sent"
                                "incoming" -> "Incoming Request"
                                else -> "Not Friends"
                            },
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Buttons based on status
                        when (status) {
                            "friend" -> {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            viewModel.removeFriend(f.uid)
                                            snackbarHostState.showSnackbar("Removed friend.")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                ) { Text("Remove Friend", color = Color.White) }
                            }
                            "sent" -> {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            viewModel.cancelFriendRequest(f.uid)
                                            snackbarHostState.showSnackbar("Friend request canceled.")
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9B5DE5)),
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                ) { Text("Cancel Request", color = Color.White) }
                            }
                            "incoming" -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(0.8f),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                viewModel.acceptFriendRequest(f.uid)
                                                snackbarHostState.showSnackbar("Friend request accepted.")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF48C774)),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Accept", color = Color.White) }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                viewModel.declineFriendRequest(f.uid)
                                                snackbarHostState.showSnackbar("Friend request declined.")
                                            }
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9B5DE5)),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Decline", color = Color.White) }
                                }
                            }
                            else -> {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            viewModel.sendFriendRequest(f.uid)
                                            snackbarHostState.showSnackbar("Friend request sent.")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5)),
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                ) { Text("Add Friend", color = Color.White) }
                            }
                        }
                        // =======================
                        // Extra Profile Actions
                        // =======================
                        Spacer(modifier = Modifier.height(28.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(0.85f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Favorites
                            Button(
                                onClick = { onLikesClick(UserId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E60CE)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Favorites", color = Color.White)
                            }

                            // Watchlist
                            Button(
                                onClick = { onWatchListClick(UserId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6930C3)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Watchlist", color = Color.White)
                            }

                            // Ratings
                            Button(
                                onClick = { onRatingsClick(UserId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7400B8)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ratings", color = Color.White)
                            }

                            // Watched
                            Button(
                                onClick = { onWatchedClick(UserId) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A0CA3)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Watched", color = Color.White)
                            }
                        }

                    }
                } ?: run {
                    Text("Friend not found", color = Color.White, fontSize = 18.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
