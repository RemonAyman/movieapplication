package com.example.myapplication.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

    var chats by remember { mutableStateOf<List<ChatItem>>(emptyList()) }
    var searchText by remember { mutableStateOf("") }

    // ✅ تحميل كل الشاتات الخاصة بالمستخدم
    LaunchedEffect(Unit) {
        db.collection("chats")
            .whereArrayContains("members", currentUserId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    chats = snapshot.documents.map { doc ->
                        ChatItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "Unknown Chat",
                            isGroup = doc.getBoolean("isGroup") ?: false,
                            lastMessage = doc.getString("lastMessage") ?: "",
                            lastMessageTime = doc.getTimestamp("lastMessageTime")
                        )
                    }
                }
            }
    }

    val filteredChats = chats.filter {
        it.name.contains(searchText, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate("newPrivateChat") },
                    containerColor = Color(0xFF9B5DE5)
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Private Chat", tint = Color.White)
                }
                FloatingActionButton(
                    onClick = { navController.navigate("newGroup") },
                    containerColor = Color(0xFF9B5DE5)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Group", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Chats",
                fontSize = 26.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Search
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search chats...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredChats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No chats found", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(filteredChats) { chat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("chatDetail/${chat.id}")
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = chat.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (chat.isGroup) {
                                Text(
                                    text = "Group",
                                    color = Color(0xFF9B5DE5),
                                    fontSize = 14.sp
                                )
                            } else {
                                Text(
                                    text = "Private",
                                    color = Color(0xFF5DE59B),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Divider(color = Color.DarkGray, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
