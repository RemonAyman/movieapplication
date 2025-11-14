package com.example.myapplication.ui.screens.chats

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.text.SimpleDateFormat
import java.util.*

// موديل الشات
data class ChatItem(
    val id: String,
    val name: String,
    val isGroup: Boolean,
    val lastMessage: String,
    val lastMessageTime: Timestamp?,
    val avatarBase64: String? = null
)

// تحويل Timestamp لوقت
object TimestampUtils {
    fun format(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

    var chats by remember { mutableStateOf<Map<String, ChatItem>>(emptyMap()) }
    var searchText by remember { mutableStateOf("") }

    // Real-Time Listener
    LaunchedEffect(Unit) {
        db.collection("chats")
            .whereArrayContains("members", currentUserId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val updatedChats = chats.toMutableMap()
                    snapshot.documents.forEach { doc ->
                        val isGroup = doc.getBoolean("isGroup") ?: false
                        val members = doc.get("members") as? List<String> ?: emptyList()
                        val lastMsg = doc.getString("lastMessage") ?: ""
                        val lastTime = doc.getTimestamp("lastMessageTime")

                        if (isGroup) {
                            updatedChats[doc.id] = ChatItem(
                                id = doc.id,
                                name = doc.getString("name") ?: "Group",
                                isGroup = true,
                                lastMessage = lastMsg,
                                lastMessageTime = lastTime,
                                avatarBase64 = doc.getString("avatarBase64")
                            )
                        } else {
                            val otherUserId = members.firstOrNull { it != currentUserId }
                            if (otherUserId != null) {
                                db.collection("users").document(otherUserId).get()
                                    .addOnSuccessListener { userDoc ->
                                        updatedChats[doc.id] = ChatItem(
                                            id = doc.id,
                                            name = userDoc.getString("username") ?: "Unknown",
                                            isGroup = false,
                                            lastMessage = lastMsg,
                                            lastMessageTime = lastTime,
                                            avatarBase64 = userDoc.getString("avatarBase64")
                                        )
                                        chats = updatedChats.toMap()
                                    }
                            }
                        }
                    }
                    chats = updatedChats.toMap()
                }
            }
    }

    val sortedChats = chats.values.sortedByDescending { it.lastMessageTime?.seconds ?: 0 }
    val filteredChats = sortedChats.filter { it.name.contains(searchText, ignoreCase = true) }

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
                    Icon(Icons.Default.Person, contentDescription = "Private", tint = Color.White)
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
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                ) { Text("No chats found", color = Color.Gray) }
            } else {
                LazyColumn {
                    items(filteredChats) { chat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(
                                        if (chat.isGroup) "chatDetail/${chat.id}"
                                        else "privateChatDetail/${chat.id}"
                                    )
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!chat.avatarBase64.isNullOrEmpty()) {
                                val bytes = Base64.decode(chat.avatarBase64, Base64.DEFAULT)
                                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = chat.name,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.Gray, CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFF9B5DE5), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = chat.name.firstOrNull()?.uppercase() ?: "?",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(chat.name, color = Color.White, fontSize = 18.sp)
                                Text(chat.lastMessage, color = Color.Gray, fontSize = 14.sp)
                            }

                            val badgeColor = if (chat.isGroup) Color(0xFF9B5DE5) else Color(0xFF5DE59B)
                            Text(text = if (chat.isGroup) "Group" else "Private", color = badgeColor, fontSize = 14.sp)

                            chat.lastMessageTime?.let {
                                Text(
                                    TimestampUtils.format(it),
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 8.dp)
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
