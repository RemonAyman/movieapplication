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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.AppColors

@Composable
fun FriendsScreen(
    viewModel: FriendsViewModel,
    onFriendClick: (String) -> Unit, // اضغط على صديق → اعرض بروفايله
    isSearchMode: Boolean = false // لو true يظهر شريط البحث
) {
    val friends by viewModel.friendsList.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }

    // فلترة الأصدقاء لو في بحث
    val filteredFriends = if (searchQuery.isEmpty()) friends else friends.filter {
        it.username.contains(searchQuery, ignoreCase = true)
    }

    LaunchedEffect(Unit) {
        if (isSearchMode) {
            viewModel.loadAllUsers() // جلب كل المستخدمين للبحث
        } else {
            viewModel.loadFriendsList() // جلب الأصدقاء الحاليين
        }
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

        // Search Bar يظهر لو الصفحة addFriend
        if (isSearchMode) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("ابحث عن صديق باليوزر") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFF2A1B3D),
                    unfocusedContainerColor = Color(0xFF2A1B3D),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (filteredFriends.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (isSearchMode) "لا يوجد نتائج للبحث." else "لا يوجد أصدقاء حتى الآن.",
                    color = AppColors.TextColor,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredFriends) { friend ->
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
