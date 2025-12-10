package com.example.myapplication.ui.screens.addFriend

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.data.remote.firebase.models.UserDataModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendScreen(
    viewModel: AddFriendViewModel,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    val filteredUsers = remember(searchQuery, uiState.allUsers) {
        if (searchQuery.isEmpty()) {
            emptyList()
        } else {
            uiState.allUsers.filter {
                it.username.contains(searchQuery, ignoreCase = true) ||
                        it.email.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0820))
    ) {
        // ===== Top Bar =====
        TopAppBar(
            title = {
                Text(
                    text = "Find Users",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1330))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ===== Search Bar =====
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                isSearching = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search by username", color = Color.Gray) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF9B5DE5))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF9B5DE5),
                unfocusedBorderColor = Color(0xFF1B1330),
                focusedContainerColor = Color(0xFF1B1330),
                unfocusedContainerColor = Color(0xFF1B1330),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF9B5DE5)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ===== Search Button =====
        Button(
            onClick = {
                isSearching = true
                scope.launch {
                    viewModel.loadInitialData()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Search",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ===== Users List =====
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF9B5DE5))
            }
        } else if (searchQuery.isEmpty() && !isSearching) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Search for users",
                        color = Color(0xFFBDBDBD),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Enter a username to find friends",
                        color = Color(0xFF9B5DE5).copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }
        } else if (filteredUsers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No users found",
                        color = Color(0xFFBDBDBD),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Try a different search",
                        color = Color(0xFF9B5DE5).copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredUsers) { user ->
                    val status = uiState.getUserStatus(user.uid)
                    UserCard(
                        user = user,
                        status = status,
                        onUserClick = { navController.navigate("profileMainScreen/${user.uid}") },
                        onAddFriend = {
                            scope.launch {
                                viewModel.sendFriendRequest(user.uid)
                            }
                        },
                        onCancelRequest = {
                            scope.launch {
                                viewModel.cancelFriendRequest(user.uid)
                            }
                        }
                    )
                }
            }
        }
    }
}

// ===== User Card =====
@Composable
fun UserCard(
    user: UserDataModel,
    status: String,
    onUserClick: () -> Unit,
    onAddFriend: () -> Unit,
    onCancelRequest: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape = RoundedCornerShape(16.dp))
            .clickable { onUserClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF9B5DE5).copy(alpha = 0.3f),
                                Color(0xFF2A1B3D)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (user.avatarBase64.isNotEmpty()) {
                    val bitmap = remember(user.avatarBase64) {
                        try {
                            val bytes = Base64.decode(user.avatarBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = user.username.firstOrNull()?.uppercase() ?: "?",
                            color = Color(0xFF9B5DE5),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    Text(
                        text = user.username.firstOrNull()?.uppercase() ?: "?",
                        color = Color(0xFF9B5DE5),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            when (status) {
                "friend" -> {
                    Text(
                        "Friend",
                        color = Color(0xFF48C774),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                "sent" -> {
                    OutlinedButton(
                        onClick = onCancelRequest,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text(
                            "Cancel",
                            color = Color(0xFFEF5350),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                else -> {
                    Button(
                        onClick = onAddFriend,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text(
                            "Add Friend",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}