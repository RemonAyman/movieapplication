package com.example.myapplication.ui.screens.friends

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
import com.example.myapplication.data.remote.firebase.models.UserDataModel
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

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
    var selectedTabIndex by remember { mutableStateOf(0) }

    val displayList by remember(allUsers, friends, friendRequests, sentRequests, searchQuery, isSearchMode, selectedTabIndex) {
        derivedStateOf {
            if (isSearchMode) {
                if (searchQuery.isBlank()) emptyList()
                else allUsers.filter { it.username.contains(searchQuery, ignoreCase = true) }
            } else {
                when (selectedTabIndex) {
                    0 -> friends
                    1 -> friendRequests
                    2 -> sentRequests
                    else -> friends
                }
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
    ) {
        TopAppBar(
            title = {
                Text(
                    text = if (isSearchMode) "Find Users" else "Friends",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1330))
        )

        if (!isSearchMode) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF1B1330),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .height(3.dp)
                            .background(Color(0xFF9B5DE5))
                    )
                }
            ) {
                val tabs = listOf("Friends", "Incoming", "Sent")
                val counts = listOf(friends.size, friendRequests.size, sentRequests.size)

                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    title,
                                    fontSize = 15.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )

                                if (counts[index] > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (index == 1) Color(0xFFFF5252) else Color(0xFF9B5DE5)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = counts[index].toString(),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            if (isSearchMode) {
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
            }

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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when (selectedTabIndex) {
                                    0 -> "No friends yet"
                                    1 -> "No incoming requests"
                                    2 -> "No sent requests"
                                    else -> "Nothing here"
                                },
                                color = Color(0xFFBDBDBD),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (selectedTabIndex == 0) "Start adding friends!" else "You're all caught up!",
                                color = Color(0xFF9B5DE5).copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                        }
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
                                    // ✅ Avatar with Base64 decode (same as chat screens)
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
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
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = user.username.firstOrNull()?.uppercase() ?: "?",
                                                color = Color(0xFF9B5DE5),
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            user.username,
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            user.email,
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    when {
                                        selectedTabIndex == 0 && status == "friend" -> {
                                            Text(
                                                "Friend",
                                                color = Color(0xFF48C774),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        selectedTabIndex == 1 && status == "incoming" -> {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                                horizontalAlignment = Alignment.End
                                            ) {
                                                Button(
                                                    onClick = { scope.launch { viewModel.acceptFriendRequest(user.uid) } },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF48C774)),
                                                    modifier = Modifier.height(32.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                ) {
                                                    Text("Accept", color = Color.White, fontSize = 12.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = { scope.launch { viewModel.declineFriendRequest(user.uid) } },
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                                                    modifier = Modifier.height(32.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                ) {
                                                    Text("Decline", color = Color(0xFFEF5350), fontSize = 12.sp)
                                                }
                                            }
                                        }

                                        selectedTabIndex == 2 && status == "sent" -> {
                                            OutlinedButton(
                                                onClick = { scope.launch { viewModel.cancelFriendRequest(user.uid) } },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9B5DE5)),
                                                modifier = Modifier.height(32.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                            ) {
                                                Text("Cancel", color = Color.White, fontSize = 12.sp)
                                            }
                                        }

                                        isSearchMode -> {
                                            when (status) {
                                                "friend" -> Text("Friend", color = Color(0xFFBDBDBD), fontSize = 13.sp)
                                                "sent" -> OutlinedButton(
                                                    onClick = { scope.launch { viewModel.cancelFriendRequest(user.uid) } },
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9B5DE5)),
                                                    modifier = Modifier.height(32.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                ) {
                                                    Text("Cancel", color = Color.White, fontSize = 12.sp)
                                                }

                                                "incoming" -> Text("Incoming", color = Color(0xFFBDBDBD), fontSize = 13.sp)
                                                else -> Button(
                                                    onClick = { scope.launch { viewModel.sendFriendRequest(user.uid) } },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5)),
                                                    modifier = Modifier.height(32.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                ) {
                                                    Text("Add", color = Color.White, fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}