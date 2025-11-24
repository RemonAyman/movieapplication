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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

// ✅ تحويل Timestamp لوقت مع التاريخ الذكي
object TimestampUtils {
    fun format(timestamp: Timestamp): String {
        val now = Calendar.getInstance()
        val messageTime = Calendar.getInstance().apply {
            time = timestamp.toDate()
        }

        // حساب الفرق بالأيام
        val daysDiff = ((now.timeInMillis - messageTime.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        return when {
            // نفس اليوم - عرض الوقت فقط
            daysDiff == 0 && now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) -> {
                SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(messageTime.time)
            }
            // امبارح
            daysDiff == 1 || (daysDiff == 0 && now.get(Calendar.DAY_OF_YEAR) - messageTime.get(Calendar.DAY_OF_YEAR) == 1) -> {
                "Yesterday"
            }
            // خلال الأسبوع الحالي - عرض اسم اليوم
            daysDiff < 7 && now.get(Calendar.WEEK_OF_YEAR) == messageTime.get(Calendar.WEEK_OF_YEAR) -> {
                SimpleDateFormat("EEEE", Locale.ENGLISH).format(messageTime.time)
            }
            // أكتر من أسبوع - عرض التاريخ الكامل
            else -> {
                SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(messageTime.time)
            }
        }
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chats",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B1330)
                )
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate("newPrivateChat") },
                    containerColor = Color(0xFF6930C3),
                    shape = CircleShape,
                    modifier = Modifier.shadow(8.dp, CircleShape)
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Private", tint = Color.White)
                }
                FloatingActionButton(
                    onClick = { navController.navigate("newGroup") },
                    containerColor = Color(0xFF9B5DE5),
                    shape = CircleShape,
                    modifier = Modifier.shadow(8.dp, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Group", tint = Color.White)
                }
            }
        },
        containerColor = Color(0xFF0F0820)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0820))
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search chats...", color = Color.Gray) },
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

            if (filteredChats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No chats found",
                            color = Color(0xFFBDBDBD),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Start a new conversation!",
                            color = Color(0xFF9B5DE5).copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredChats) { chat ->
                        ChatCard(
                            chat = chat,
                            onClick = {
                                navController.navigate(
                                    if (chat.isGroup) "chatDetail/${chat.id}"
                                    else "privateChatDetail/${chat.id}"
                                )
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatCard(
    chat: ChatItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            ChatAvatar(
                avatarBase64 = chat.avatarBase64,
                name = chat.name,
                isGroup = chat.isGroup
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = chat.name,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (chat.isGroup) Color(0xFF9B5DE5).copy(alpha = 0.2f)
                                else Color(0xFF5DE59B).copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (chat.isGroup) "Group" else "Private",
                            color = if (chat.isGroup) Color(0xFF9B5DE5) else Color(0xFF5DE59B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = chat.lastMessage,
                        color = Color(0xFFBDBDBD),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    chat.lastMessageTime?.let {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            TimestampUtils.format(it),
                            color = Color(0xFF9B5DE5).copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatAvatar(
    avatarBase64: String?,
    name: String,
    isGroup: Boolean
) {
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
        if (!avatarBase64.isNullOrEmpty()) {
            val bytes = Base64.decode(avatarBase64, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            bmp?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        } else {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                color = Color(0xFF9B5DE5),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
            )
        }
    }
}