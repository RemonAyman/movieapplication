package com.example.myapplication.ui.screens.chats

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

data class UserData(
    val username: String,
    val avatarBase64: String? = null
)

data class ChatListItem(
    val id: String,
    val name: String,
    val lastMessage: String,
    val lastMessageTime: Timestamp,
    val isGroup: Boolean,
    val avatarBase64: String?,
    val unreadCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var chats by remember { mutableStateOf<List<ChatListItem>>(emptyList()) }
    var usersMap by remember { mutableStateOf<Map<String, UserData>>(emptyMap()) }
    var rawChatDocs by remember { mutableStateOf<List<DocumentSnapshot>>(emptyList()) }

    LaunchedEffect(Unit) {
        db.collection("users").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                usersMap = snapshot.documents.associate { doc ->
                    val id = doc.id
                    val username = doc.getString("username") ?: "Unknown"
                    val avatar = doc.getString("avatarBase64")
                    id to UserData(username, avatar)
                }
            }
        }
    }

    DisposableEffect(currentUserId) {
        val listener = db.collection("chats")
            .whereArrayContains("members", currentUserId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) rawChatDocs = snapshot.documents
            }
        onDispose { listener.remove() }
    }

    LaunchedEffect(rawChatDocs, usersMap) {
        chats = rawChatDocs.mapNotNull { doc ->
            try {
                val id = doc.id
                val lastMessage = doc.getString("lastMessage") ?: ""
                val lastTime = doc.getTimestamp("lastMessageTime") ?: Timestamp.now()
                val isGroup = doc.getBoolean("isGroup") ?: false
                val unread = (doc.getLong("unread") ?: 0L).toInt()
                val (name, avatar) =
                    if (isGroup) {
                        (doc.getString("name") ?: "Group") to doc.getString("avatarBase64")
                    } else {
                        val members = doc.get("members") as? List<*> ?: emptyList<String>()
                        val otherUserId = members.firstOrNull { it != currentUserId } as? String
                        val u = otherUserId?.let { usersMap[it] }
                        (u?.username ?: "Unknown") to u?.avatarBase64
                    }
                ChatListItem(id, name, lastMessage, lastTime, isGroup, avatar, unread)
            } catch (e: Exception) {
                null
            }
        }
    }

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
                ),
                actions = {
                    IconButton(onClick = { /* TODO: Add New Chat */ }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Chat",
                            tint = Color(0xFF9B5DE5)
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFF0F0820)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0820))
                .padding(padding)
                .padding(horizontal = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            if (chats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No chats yet",
                            color = Color(0xFFBDBDBD),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Start a conversation with your friends!",
                            color = Color(0xFF9B5DE5).copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(chats, key = { _, item -> item.id }) { index, chat ->
                        ChatItemCard(
                            chat = chat,
                            onClick = {
                                if (chat.isGroup) {
                                    navController.navigate("chatDetail/${chat.id}")
                                } else {
                                    navController.navigate("privateChatDetail/${chat.id}")
                                }
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
private fun ChatItemCard(
    chat: ChatListItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape = RoundedCornerShape(16.dp))
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
            // Avatar with better styling
            Avatar(
                avatarBase64 = chat.avatarBase64,
                name = chat.name,
                sizeDp = 60
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

                    Text(
                        text = formatTimestampSmart(chat.lastMessageTime),
                        color = Color(0xFF9B5DE5).copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
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

                    if (chat.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .wrapContentWidth()
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF9B5DE5),
                                            Color(0xFF7B3FD5)
                                        )
                                    )
                                )
                                .shadow(4.dp, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Avatar(avatarBase64: String?, name: String, sizeDp: Int = 60) {
    val imageBitmap = remember(avatarBase64) {
        try {
            if (!avatarBase64.isNullOrEmpty()) {
                val pure = avatarBase64.substringAfter(",")
                val bytes = Base64.decode(pure, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF9B5DE5).copy(alpha = 0.3f),
                        Color(0xFF2A1B3D)
                    )
                )
            )
            .border(2.dp, Color(0xFF9B5DE5).copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = name,
                contentScale = ContentScale.Crop,  // ✅ أهم تعديل
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)  // ✅ تأكيد الشكل الدائري
            )
        } else {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                color = Color(0xFF9B5DE5),
                fontWeight = FontWeight.ExtraBold,
                fontSize = (sizeDp / 2.5).sp
            )
        }
    }
}

fun formatTimestamp(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

fun formatTimestampSmart(timestamp: Timestamp): String {
    val now = System.currentTimeMillis()
    val then = timestamp.toDate().time
    val diff = now - then
    val oneDay = 24 * 60 * 60 * 1000L
    return when {
        diff < 60 * 1000L -> "Now"
        diff < 60 * 60 * 1000L -> "${diff / (60 * 1000L)}m"
        diff < oneDay -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(then))
        diff < oneDay * 7 -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(then))
        else -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(then))
    }
}