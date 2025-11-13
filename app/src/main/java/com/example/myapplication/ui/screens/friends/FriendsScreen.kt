package com.example.myapplication.ui.screens.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.remote.firebase.models.UserDataModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel,
    onFriendClick: (String) -> Unit,
    isSearchMode: Boolean = false
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
        Text(
            text = if (isSearchMode) "Find Users" else "Your Friends",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isSearchMode) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by username", color = Color.LightGray) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1B1330),
                    unfocusedContainerColor = Color(0xFF1B1330),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (displayList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (isSearchMode) "No users match your search." else "You have no friends yet.",
                    color = Color(0xFFBDBDBD),
                    fontSize = 16.sp
                )
            }
        } else {
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
                                Text(user.username, color = Color.White, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(user.email, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }

                            when (status) {
                                "friend" -> Text("Friend", color = Color(0xFFBDBDBD), fontSize = 13.sp)
                                "sent" -> OutlinedButton(
                                    onClick = { scope.launch { viewModel.cancelFriendRequest(user.uid) } },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9B5DE5))
                                ) { Text("Cancel", color = Color.White) }

                                "incoming" -> Text("Incoming", color = Color(0xFFBDBDBD), fontSize = 13.sp)
                                else -> Button(
                                    onClick = { scope.launch { viewModel.sendFriendRequest(user.uid) } },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5))
                                ) { Text("Add Friend", color = Color.White) }
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
            .clip(CircleShape)
            .background(Color(0xFF2A1B3D).copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        if (user.avatarBase64.isNotEmpty()) {
            AsyncImage(
                model = user.avatarBase64,
                contentDescription = "avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(user.username.firstOrNull()?.uppercase() ?: "?", color = Color(0xFF9B5DE5), fontSize = 18.sp)
        }
    }
}
