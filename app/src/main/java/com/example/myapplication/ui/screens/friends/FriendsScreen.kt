package com.example.myapplication.ui.screens.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.AppColors
import com.example.myapplication.ui.screens.friends.FriendsViewModel

@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel,
    onFriendClick: (String) -> Unit
) {
    // استخدام collectAsState بدل observeAsState
    val friends by viewModel.friendsList.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        viewModel.loadFriendsList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBg)
            .padding(16.dp)
    ) {
        Text(
            "أصدقائك",
            style = MaterialTheme.typography.headlineMedium,
            color = AppColors.TextColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (friends.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "لا يوجد أصدقاء حتى الآن.",
                    color = AppColors.TextColor,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(friends) { friend ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFriendClick(friend.uid) },
                        colors = CardDefaults.cardColors(containerColor = AppColors.DarkBg.copy(alpha = 0.85f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.NeonGlow.copy(alpha = 0.2f)),
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

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = friend.username,
                                color = AppColors.TextColor,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}