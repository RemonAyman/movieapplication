package com.example.myapplication.ui.screens.friends

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.remote.firebase.models.UserDataModel
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import com.google.accompanist.flowlayout.FlowRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel,
    navController: NavController,
    onFriendClick: (String) -> Unit,
    isSearchMode: Boolean = false,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val allUsers by viewModel.allUsers.collectAsState()
    val friends by viewModel.friendsList.collectAsState()
    val friendRequests by viewModel.friendRequests.collectAsState()
    val sentRequests by viewModel.sentFriendRequests.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val displayList by remember(allUsers, friends, friendRequests, sentRequests, searchQuery, isSearchMode) {
        derivedStateOf {
            if (isSearchMode) {
                if (searchQuery.isBlank()) emptyList()
                else allUsers.filter { it.username.contains(searchQuery, ignoreCase = true) }
            } else {
                friends
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadFriendsList()
        viewModel.loadAllUsers()
        viewModel.loadFriendRequests()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0820))
            .padding(16.dp)
    ) {
        // ======= Top Bar =======
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = if (isSearchMode) "Find Users" else "Your Friends",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isSearchMode) {
            // ======= Search Bar =======
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by username", color = Color.LightGray) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFF1B1330),
                    unfocusedContainerColor = Color(0xFF1B1330),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))

            // ======= Search Button (optional but consistent) =======
            Button(
                onClick = { /* Already searching live */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // ======= Results / Empty State =======
        when {
            displayList.isEmpty() && searchQuery.isNotEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No users match your search.",
                        color = Color(0xFFBDBDBD),
                        fontSize = 16.sp
                    )
                }
            }

            displayList.isEmpty() && !isSearchMode -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "You have no friends yet.",
                        color = Color(0xFFBDBDBD),
                        fontSize = 16.sp
                    )
                }
            }

            displayList.isNotEmpty() -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(displayList) { user ->
                        val status = viewModel.computeRequestStatusFor(user.uid)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, shape = RoundedCornerShape(14.dp))
                                .clickable { onFriendClick(user.uid) },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarSmall(user)
                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.username, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(user.email, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }

                                when (status) {
                                    "friend" -> Text("Friend", color = Color(0xFFBDBDBD), fontSize = 13.sp)
                                    "sent" -> OutlinedButton(
                                        onClick = { scope.launch { viewModel.cancelFriendRequest(user.uid) } },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9B5DE5))
                                    ) { Text("Cancel", color = Color.White, fontSize = 12.sp) }

                                    "incoming" -> Text("Incoming", color = Color(0xFFBDBDBD), fontSize = 13.sp)
                                    else -> Button(
                                        onClick = { scope.launch { viewModel.sendFriendRequest(user.uid) } },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5))
                                    ) { Text("Add", color = Color.White, fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarSmall(user: UserDataModel, sizeDp: Int = 52) {
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .shadow(6.dp, CircleShape)
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
            AsyncImage(
                model = user.avatarBase64,
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
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}