package com.example.myapplication.ui.screens.chats

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateChatDetailScreen(chatId: String, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<PrivateChatMessage>>(emptyList()) }
    var members by remember { mutableStateOf<Map<String,String>>(emptyMap()) }
    var avatars by remember { mutableStateOf<Map<String,Bitmap?>>(emptyMap()) }
    val coroutineScope = rememberCoroutineScope()

    var targetUserId by remember { mutableStateOf("") }
    var targetUserName by remember { mutableStateOf("User") }
    var targetUserAvatar by remember { mutableStateOf<Bitmap?>(null) }

    var currentUserName by remember { mutableStateOf("Me") }
    var currentUserAvatar by remember { mutableStateOf<Bitmap?>(null) }

    // ✅ LazyList State for auto-scroll
    val listState = rememberLazyListState()

    LaunchedEffect(chatId) {
        withContext(Dispatchers.IO) {
            try {
                val chatDoc = db.collection("chats").document(chatId).get().await()
                val memberIds = chatDoc.get("members") as? List<String> ?: emptyList()

                if(memberIds.isNotEmpty()){
                    val usersSnap = db.collection("users").whereIn("__name__", memberIds).get().await()
                    val tempMembers = mutableMapOf<String, String>()
                    val tempAvatars = mutableMapOf<String, Bitmap?>()

                    usersSnap.documents.forEach { doc ->
                        tempMembers[doc.id] = doc.getString("username") ?: "Unknown"
                        val avatarBase64 = doc.getString("avatarBase64") ?: ""
                        tempAvatars[doc.id] = if(avatarBase64.isNotEmpty()) {
                            decodeBase64ToBitmap(avatarBase64)
                        } else null
                    }

                    withContext(Dispatchers.Main) {
                        members = tempMembers
                        avatars = tempAvatars

                        targetUserId = memberIds.first { it != currentUserId }
                        targetUserName = members[targetUserId] ?: "User"
                        targetUserAvatar = avatars[targetUserId]

                        currentUserName = members[currentUserId] ?: "Me"
                        currentUserAvatar = avatars[currentUserId]
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(chatId) {
        val chatRef = db.collection("chats").document(chatId).collection("messages")
        chatRef.orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages = snapshot.documents.mapNotNull { doc ->
                        val text = doc.getString("text") ?: return@mapNotNull null
                        val senderId = doc.getString("senderId") ?: ""
                        val timestamp = doc.getTimestamp("timestamp")

                        val isSharedMovie = doc.getBoolean("isSharedMovie") ?: false
                        val movieId = doc.getString("movieId")
                        val movieTitle = doc.getString("movieTitle")
                        val moviePoster = doc.getString("moviePoster")
                        val movieRating = doc.getDouble("movieRating")

                        PrivateChatMessage(
                            senderId = senderId,
                            text = text,
                            timestamp = timestamp,
                            isSharedMovie = isSharedMovie,
                            movieId = movieId,
                            movieTitle = movieTitle,
                            moviePoster = moviePoster,
                            movieRating = movieRating
                        )
                    }
                }
            }
    }

    // ✅ Auto-scroll to bottom when messages change
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .imePadding() // ✅ يرفع المحتوى فوق الكيبورد
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            OptimizedAvatarImage(targetUserAvatar, targetUserName) {
                navController.navigate("profileMainScreen/$targetUserId") {
                    launchSingleTop = true
                    restoreState = true
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Text(targetUserName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        // ✅ Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            reverseLayout = false // ✅ الترتيب الطبيعي
        ) {
            val groupedMessages = messages.groupBy { msg ->
                msg.timestamp?.let { formatPrivateDateHeader(it) } ?: "Unknown"
            }

            groupedMessages.forEach { (dateHeader, messagesInGroup) ->
                item(key = "header_$dateHeader") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(0xFF2A1B3D).copy(alpha = 0.6f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                dateHeader,
                                color = Color(0xFF9B5DE5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                items(messagesInGroup, key = { it.senderId + it.text + it.timestamp.toString() }) { msg ->
                    val isMine = msg.senderId == currentUserId
                    val senderName = if(isMine) currentUserName else members[msg.senderId] ?: "Unknown"
                    val avatarBitmap = if(isMine) currentUserAvatar else avatars[msg.senderId]

                    if (msg.isSharedMovie) {
                        PrivateSharedMovieBubble(
                            msg = msg,
                            isMine = isMine,
                            senderName = senderName,
                            avatarBitmap = avatarBitmap,
                            onAvatarClick = {
                                navController.navigate("profileMainScreen/${msg.senderId}") {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onMovieClick = {
                                msg.movieId?.let {
                                    navController.navigate("details/$it")
                                }
                            }
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = if(isMine) Arrangement.End else Arrangement.Start,
                            verticalAlignment = Alignment.Top
                        ) {
                            if(!isMine){
                                OptimizedAvatarImage(avatarBitmap, senderName) {
                                    navController.navigate("profileMainScreen/${msg.senderId}") {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Column(horizontalAlignment = if(isMine) Alignment.End else Alignment.Start) {
                                if(!isMine){
                                    Text(
                                        senderName,
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.Bottom) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if(isMine) Color(0xFF9B5DE5) else Color(0xFF333333),
                                                shape = RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = if(isMine) 16.dp else 4.dp,
                                                    bottomEnd = if(isMine) 4.dp else 16.dp
                                                )
                                            )
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Text(msg.text, color = Color.White, fontSize = 15.sp)
                                    }

                                    msg.timestamp?.let {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            formatPrivateMessageTime(it),
                                            color = Color.Gray,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                }
                            }

                            if(isMine) Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            }
        }

        // ✅ Input Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Type a message...", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedContainerColor = Color(0xFF2A1B3D),
                    unfocusedContainerColor = Color(0xFF2A1B3D),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    val text = messageText.trim()
                    if(text.isNotEmpty()){
                        coroutineScope.launch {
                            sendPrivateMessage(chatId, currentUserId, text)
                            messageText = ""
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF9B5DE5), CircleShape)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun PrivateSharedMovieBubble(
    msg: PrivateChatMessage,
    isMine: Boolean,
    senderName: String,
    avatarBitmap: Bitmap?,
    onAvatarClick: () -> Unit,
    onMovieClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isMine) {
            OptimizedAvatarImage(avatarBitmap, senderName, onAvatarClick)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
            if (!isMine) {
                Text(
                    senderName,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Card(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clickable { onMovieClick() },
                colors = CardDefaults.cardColors(
                    containerColor = if (isMine) Color(0xFF9B5DE5) else Color(0xFF333333)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        coil.compose.AsyncImage(
                            model = msg.moviePoster,
                            contentDescription = msg.movieTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                msg.movieTitle ?: "Movie",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                "⭐ ${String.format("%.1f", msg.movieRating ?: 0.0)}",
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Tap to view details",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            msg.timestamp?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    formatPrivateMessageTime(it),
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }

        if (isMine) Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun OptimizedAvatarImage(bitmap: Bitmap?, name: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if(bitmap == null) Color(0xFF9B5DE5) else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if(bitmap != null){
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Text(
                name.firstOrNull()?.uppercase() ?: "U",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

suspend fun decodeBase64ToBitmap(base64: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }
}

fun formatPrivateDateHeader(timestamp: Timestamp): String {
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply {
        time = timestamp.toDate()
    }

    val daysDiff = ((now.timeInMillis - messageTime.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

    return when {
        daysDiff == 0 && now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) -> "Today"
        daysDiff == 1 || (daysDiff == 0 && now.get(Calendar.DAY_OF_YEAR) - messageTime.get(Calendar.DAY_OF_YEAR) == 1) -> "Yesterday"
        daysDiff < 7 && now.get(Calendar.WEEK_OF_YEAR) == messageTime.get(Calendar.WEEK_OF_YEAR) -> {
            SimpleDateFormat("EEEE", Locale.ENGLISH).format(messageTime.time)
        }
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(messageTime.time)
    }
}

fun formatPrivateMessageTime(timestamp: Timestamp): String {
    return SimpleDateFormat("HH:mm", Locale.ENGLISH).format(timestamp.toDate())
}

data class PrivateChatMessage(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null,
    val isSharedMovie: Boolean = false,
    val movieId: String? = null,
    val movieTitle: String? = null,
    val moviePoster: String? = null,
    val movieRating: Double? = null
)

suspend fun sendPrivateMessage(chatId: String, senderId: String, text: String){
    withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val messageData = hashMapOf(
            "senderId" to senderId,
            "text" to text,
            "timestamp" to Timestamp.now()
        )
        db.collection("chats").document(chatId).collection("messages").add(messageData).await()
        db.collection("chats").document(chatId).update(
            mapOf("lastMessage" to text, "lastMessageTime" to Timestamp.now())
        ).await()
    }
}