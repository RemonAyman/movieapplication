package com.example.myapplication.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var chats by remember { mutableStateOf<List<ChatItem>>(emptyList()) }
    var usersMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) } // id -> username

    // ✅ تحميل جميع المستخدمين مسبقًا عشان نجيب اسم العضو
    LaunchedEffect(Unit) {
        db.collection("users").get().addOnSuccessListener { snapshot ->
            usersMap = snapshot.documents.associate { doc ->
                val id = doc.id
                val username = doc.getString("username") ?: "Unknown"
                id to username
            }
        }
    }

    // ✅ تحميل الشاتات
    LaunchedEffect(Unit) {
        db.collection("chats")
            .whereArrayContains("members", currentUserId)
            .orderBy("lastMessageTime")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    chats = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val lastMessage = doc.getString("lastMessage") ?: ""
                        val lastTime = doc.getTimestamp("lastMessageTime") ?: Timestamp.now()
                        val isGroup = doc.getBoolean("isGroup") ?: false

                        // ✅ اسم الشات: لو جروب استخدم الاسم، لو فردي استخدم اسم العضو الآخر
                        val name = if (isGroup) {
                            doc.getString("name") ?: "Group"
                        } else {
                            val members = doc.get("members") as? List<*> ?: emptyList<Any>()
                            val otherUserId = members.firstOrNull { it != currentUserId } as? String
                            usersMap[otherUserId] ?: "Unknown"
                        }

                        ChatItem(
                            id = id,
                            name = name,
                            lastMessage = lastMessage,
                            lastMessageTime = lastTime,
                            isGroup = isGroup
                        )
                    }.reversed()
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text(
            text = "Chats",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (chats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No chats yet", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(chats) { chat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (chat.isGroup) {
                                    navController.navigate("chatDetail/${chat.id}")
                                } else {
                                    navController.navigate("privateChatDetail/${chat.id}")
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (chat.isGroup) Icons.Default.Group else Icons.Default.Person,
                            contentDescription = "Chat",
                            tint = Color(0xFF9B5DE5),
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = chat.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = chat.lastMessage,
                                color = Color.Gray,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                        }

                        Text(
                            text = chat.lastMessageTime?.let { formatTimestamp(it) } ?: "",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Divider(color = Color.DarkGray, thickness = 0.6.dp)
                }
            }
        }
    }
}



// ✅ دالة لتحويل Timestamp لوقت قابل للقراءة
fun formatTimestamp(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}
