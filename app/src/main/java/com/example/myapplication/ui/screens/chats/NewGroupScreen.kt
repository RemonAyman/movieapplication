package com.example.myapplication.ui.screens.chats

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGroupScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

    var groupName by remember { mutableStateOf("") }
    var friends by remember { mutableStateOf<List<EnhancedFriendItem>>(emptyList()) }
    var selectedFriends by remember { mutableStateOf(mutableSetOf<String>()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                friends = snapshot.documents
                    .filter { it.id != currentUserId }
                    .map { doc ->
                        EnhancedFriendItem(
                            id = doc.id,
                            name = doc.getString("username") ?: "Unknown",
                            avatarBase64 = doc.getString("avatarBase64") ?: ""
                        )
                    }
            }
    }

    val filteredFriends = friends.filter {
        it.name.contains(searchText, ignoreCase = true)
    }

    // ✅ Get selected friends details
    val selectedFriendsList = friends.filter { selectedFriends.contains(it.id) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create New Group",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1330))
            )
        },
        containerColor = Color(0xFF0F0820),
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F0820),
                                Color(0xFF1B1330)
                            )
                        )
                    )
                    .padding(padding)
                    .padding(16.dp)
            ) {

                // ✅ Group Avatar & Name Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp)
                                .shadow(8.dp, CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF9B5DE5),
                                            Color(0xFF6930C3)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = "Group",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        OutlinedTextField(
                            value = groupName,
                            onValueChange = { groupName = it },
                            placeholder = { Text("Enter group name...", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF2A1B3D),
                                unfocusedContainerColor = Color(0xFF2A1B3D),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF9B5DE5),
                                focusedBorderColor = Color(0xFF9B5DE5),
                                unfocusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ✅ Selected Members Row (Horizontal Scroll)
                if (selectedFriendsList.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Selected Members",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF9B5DE5).copy(alpha = 0.2f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "${selectedFriendsList.size}",
                                        color = Color(0xFF9B5DE5),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(selectedFriendsList) { friend ->
                                    SelectedMemberChip(
                                        friend = friend,
                                        onRemove = { selectedFriends.remove(friend.id) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ✅ Search Bar
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search friends...", color = Color.Gray) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF9B5DE5))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1B1330),
                        unfocusedContainerColor = Color(0xFF1B1330),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF9B5DE5),
                        focusedBorderColor = Color(0xFF9B5DE5),
                        unfocusedBorderColor = Color(0xFF2A1B3D)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ Select Members Title
                Text(
                    text = "All Friends",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredFriends.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "No friends found",
                                color = Color(0xFFBDBDBD),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Try a different search",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredFriends) { friend ->
                            val selected = selectedFriends.contains(friend.id)
                            FriendCardItem(
                                friend = friend,
                                isSelected = selected,
                                onClick = {
                                    if (selected) selectedFriends.remove(friend.id)
                                    else selectedFriends.add(friend.id)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ Create Button
                Button(
                    onClick = {
                        if (groupName.isNotBlank() && selectedFriends.isNotEmpty()) {
                            isLoading = true
                            val members = (selectedFriends + currentUserId).toList()
                            val groupData = hashMapOf(
                                "name" to groupName,
                                "isGroup" to true,
                                "members" to members,
                                "lastMessage" to "",
                                "lastMessageTime" to Timestamp.now()
                            )
                            db.collection("chats")
                                .add(groupData)
                                .addOnSuccessListener { docRef ->
                                    isLoading = false
                                    navController.navigate("chatDetail/${docRef.id}") {
                                        popUpTo("chats") { inclusive = false }
                                    }
                                }
                                .addOnFailureListener {
                                    isLoading = false
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9B5DE5),
                        disabledContainerColor = Color(0xFF9B5DE5).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isLoading && groupName.isNotBlank() && selectedFriends.isNotEmpty()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = "Group",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Create Group",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    )
}

// ✅ Selected Member Chip (في الصف الأفقي)
@Composable
fun SelectedMemberChip(
    friend: EnhancedFriendItem,
    onRemove: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(70.dp)
    ) {
        Box {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
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
                if (friend.avatarBase64.isNotEmpty()) {
                    val bytes = Base64.decode(friend.avatarBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = friend.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = friend.name.firstOrNull()?.uppercase() ?: "?",
                        color = Color(0xFF9B5DE5),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Remove Button (X)
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .align(Alignment.TopEnd)
                    .shadow(4.dp, CircleShape)
                    .background(Color(0xFFFF4757), CircleShape)
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = friend.name,
            color = Color.White,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FriendCardItem(
    friend: EnhancedFriendItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isSelected) 12.dp else 6.dp, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2A1B3D) else Color(0xFF1B1330)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
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
                if (friend.avatarBase64.isNotEmpty()) {
                    val bytes = Base64.decode(friend.avatarBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = friend.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = friend.name.firstOrNull()?.uppercase() ?: "?",
                        color = Color(0xFF9B5DE5),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = friend.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Color(0xFF9B5DE5),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Color(0xFF2A1B3D),
                            CircleShape
                        )
                )
            }
        }
    }
}

data class EnhancedFriendItem(
    val id: String,
    val name: String,
    val avatarBase64: String = ""
)