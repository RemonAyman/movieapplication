package com.example.myapplication.ui.screens.details

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
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
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.data.remote.MovieApiModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class ShareChatItem(
    val id: String,
    val name: String,
    val isGroup: Boolean,
    val avatarBase64: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareMovieBottomSheet(
    navController: NavHostController,
    movie: MovieApiModel,
    onDismiss: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val scope = rememberCoroutineScope()

    var chats by remember { mutableStateOf<List<ShareChatItem>>(emptyList()) }
    var searchText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }

    // ✅ Load all chats
    LaunchedEffect(Unit) {
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val snapshot = db.collection("chats")
                        .whereArrayContains("members", currentUserId)
                        .get()
                        .await()

                    val tempChats = mutableListOf<ShareChatItem>()

                    snapshot.documents.forEach { doc ->
                        val id = doc.id
                        val isGroup = doc.getBoolean("isGroup") ?: false
                        val members = doc.get("members") as? List<String> ?: emptyList()

                        if (isGroup) {
                            tempChats.add(
                                ShareChatItem(
                                    id = id,
                                    name = doc.getString("name") ?: "Group",
                                    isGroup = true,
                                    avatarBase64 = doc.getString("avatarBase64")
                                )
                            )
                        } else {
                            val otherUserId = members.firstOrNull { it != currentUserId }
                            if (otherUserId != null) {
                                val userDoc = db.collection("users").document(otherUserId).get().await()
                                tempChats.add(
                                    ShareChatItem(
                                        id = id,
                                        name = userDoc.getString("username") ?: "Unknown",
                                        isGroup = false,
                                        avatarBase64 = userDoc.getString("avatarBase64")
                                    )
                                )
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        chats = tempChats
                        isLoading = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isLoading = false
                    }
                }
            }
        }
    }

    val filteredChats = chats.filter { it.name.contains(searchText, ignoreCase = true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000))
            .clickable(enabled = !isSending) { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .background(
                    Color(0xFF1B1330),
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .clickable(enabled = false) {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Share Movie",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { if (!isSending) onDismiss() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Movie Preview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1B3D)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        AsyncImage(
                            model = "https://image.tmdb.org/t/p/w200${movie.poster_path}",
                            contentDescription = movie.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                movie.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                "⭐ ${String.format("%.1f", movie.vote_average)}",
                                color = Color(0xFFFFD700),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search chats...", color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF9B5DE5)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A1B3D),
                        unfocusedContainerColor = Color(0xFF2A1B3D),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF9B5DE5),
                        focusedBorderColor = Color(0xFF9B5DE5),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isSending
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Chats List
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF9B5DE5))
                    }
                } else if (filteredChats.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "No chats found",
                                color = Color(0xFFBDBDBD),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Start a conversation first",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredChats) { chat ->
                            ShareChatCard(
                                chat = chat,
                                isSending = isSending,
                                onClick = {
                                    isSending = true
                                    scope.launch {
                                        shareMovieToChat(
                                            db = db,
                                            chatId = chat.id,
                                            currentUserId = currentUserId,
                                            movie = movie
                                        )
                                        withContext(Dispatchers.Main) {
                                            isSending = false
                                            onDismiss()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                if (isSending) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF9B5DE5)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareChatCard(
    chat: ShareChatItem,
    isSending: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(14.dp))
            .clickable(enabled = !isSending, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1B3D)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            val avatarBitmap = remember(chat.avatarBase64) {
                if (!chat.avatarBase64.isNullOrEmpty()) {
                    try {
                        val bytes = Base64.decode(chat.avatarBase64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }
            }

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
                if (avatarBitmap != null) {
                    Image(
                        bitmap = avatarBitmap.asImageBitmap(),
                        contentDescription = chat.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    FallbackAvatar(chat)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    chat.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (chat.isGroup) Icons.Default.Group else Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF9B5DE5),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (chat.isGroup) "Group" else "Private",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FallbackAvatar(chat: ShareChatItem) {
    Icon(
        if (chat.isGroup) Icons.Default.Group else Icons.Default.Person,
        contentDescription = null,
        tint = Color(0xFF9B5DE5),
        modifier = Modifier.size(24.dp)
    )
}

//  Share movie to chat
suspend fun shareMovieToChat(
    db: FirebaseFirestore,
    chatId: String,
    currentUserId: String,
    movie: MovieApiModel
) {
    withContext(Dispatchers.IO) {
        try {
            val messageData = hashMapOf(
                "text" to "🎬 Shared a movie",
                "senderId" to currentUserId,
                "timestamp" to Timestamp.now(),
                "movieId" to movie.id.toString(),
                "movieTitle" to movie.title,
                "moviePoster" to "https://image.tmdb.org/t/p/w200${movie.poster_path}",
                "movieRating" to movie.vote_average,
                "isSharedMovie" to true
            )

            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(messageData)
                .await()

            db.collection("chats")
                .document(chatId)
                .update(
                    mapOf(
                        "lastMessage" to "🎬 Shared: ${movie.title}",
                        "lastMessageTime" to Timestamp.now()
                    )
                )
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}