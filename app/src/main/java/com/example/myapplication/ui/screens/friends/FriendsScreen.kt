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

    var selectedTabIndex by remember { mutableStateOf(0) }

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
        // ===== Top Bar =====
        TopAppBar(
            title = {
                Text(
                    text = "Friends",
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

        // ===== Tabs: Friends | Requests =====
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
            val tabs = listOf("Friends", "Requests")
            val friendsCount = friends.size
            val requestsCount = friendRequests.size + sentRequests.size

            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                title,
                                fontSize = 16.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )

                            val count = if (index == 0) friendsCount else requestsCount
                            if (count > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == 1 && friendRequests.isNotEmpty())
                                                Color(0xFFFF5252)
                                            else
                                                Color(0xFF9B5DE5)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = count.toString(),
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

        Spacer(modifier = Modifier.height(16.dp))

        // ===== Content Based on Selected Tab =====
        when (selectedTabIndex) {
            0 -> FriendsListTab(friends, navController, onFriendClick)
            1 -> RequestsTab(
                friendRequests = friendRequests,
                sentRequests = sentRequests,
                navController = navController,
                onFriendClick = onFriendClick,
                onAcceptRequest = { scope.launch { viewModel.acceptFriendRequest(it) } },
                onDeclineRequest = { scope.launch { viewModel.declineFriendRequest(it) } },
                onCancelRequest = { scope.launch { viewModel.cancelFriendRequest(it) } }
            )
        }
    }
}

// ===== Friends List Tab =====
@Composable
fun FriendsListTab(
    friends: List<UserDataModel>,
    navController: NavController,
    onFriendClick: (String) -> Unit
) {
    if (friends.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "No friends yet",
                    color = Color(0xFFBDBDBD),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Start adding friends!",
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
            items(friends) { friend ->
                FriendCard(friend, onFriendClick)
            }
        }
    }
}

// ===== Requests Tab =====
@Composable
fun RequestsTab(
    friendRequests: List<UserDataModel>,
    sentRequests: List<UserDataModel>,
    navController: NavController,
    onFriendClick: (String) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onDeclineRequest: (String) -> Unit,
    onCancelRequest: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ===== Incoming Requests Section =====
        item {
            Text(
                text = "Incoming Requests",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (friendRequests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No incoming requests",
                            color = Color(0xFFBDBDBD),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(friendRequests) { friend ->
                RequestCard(
                    friend = friend,
                    primaryActionText = "Accept",
                    primaryColor = Color(0xFF48C774),
                    secondaryActionText = "Decline",
                    onPrimaryAction = { onAcceptRequest(friend.uid) },
                    onSecondaryAction = { onDeclineRequest(friend.uid) },
                    onCardClick = { onFriendClick(friend.uid) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        // ===== Sent Requests Section =====
        item {
            Text(
                text = "Sent Requests",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (sentRequests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No sent requests",
                            color = Color(0xFFBDBDBD),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(sentRequests) { friend ->
                RequestCard(
                    friend = friend,
                    primaryActionText = "Cancel",
                    primaryColor = Color(0xFFEF5350),
                    secondaryActionText = null,
                    onPrimaryAction = { onCancelRequest(friend.uid) },
                    onSecondaryAction = null,
                    onCardClick = { onFriendClick(friend.uid) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ===== Friend Card (للـ Friends List) =====
@Composable
fun FriendCard(friend: UserDataModel, onFriendClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape = RoundedCornerShape(16.dp))
            .clickable { onFriendClick(friend.uid) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarImage(friend)
            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.username,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = friend.email,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                "Friend",
                color = Color(0xFF48C774),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ===== Request Card (للـ Requests Tab) =====
@Composable
fun RequestCard(
    friend: UserDataModel,
    primaryActionText: String,
    primaryColor: Color,
    secondaryActionText: String?,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: (() -> Unit)?,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape = RoundedCornerShape(16.dp))
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarImage(friend)
            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.username,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = friend.email,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Button(
                    onClick = onPrimaryAction,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Text(
                        primaryActionText,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (secondaryActionText != null && onSecondaryAction != null) {
                    OutlinedButton(
                        onClick = onSecondaryAction,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9B5DE5)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text(
                            secondaryActionText,
                            color = Color(0xFFEF5350),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ===== Avatar Image Component =====
@Composable
fun AvatarImage(user: UserDataModel) {
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
}