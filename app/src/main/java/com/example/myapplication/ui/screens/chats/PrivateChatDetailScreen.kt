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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateChatDetailScreen(chatId: String, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var members by remember { mutableStateOf<Map<String,String>>(emptyMap()) }
    var avatars by remember { mutableStateOf<Map<String,String>>(emptyMap()) }
    val coroutineScope = rememberCoroutineScope()

    var targetUserId by remember { mutableStateOf("") }
    var targetUserName by remember { mutableStateOf("User") }
    var targetUserAvatar by remember { mutableStateOf("") }

    var currentUserName by remember { mutableStateOf("Me") }
    var currentUserAvatar by remember { mutableStateOf("") }

    // تحميل بيانات المستخدمين
    LaunchedEffect(chatId) {
        val chatDoc = db.collection("chats").document(chatId).get().await()
        val memberIds = chatDoc.get("members") as? List<String> ?: emptyList()
        if(memberIds.isNotEmpty()){
            val usersSnap = db.collection("users").whereIn("__name__", memberIds).get().await()
            members = usersSnap.documents.associate { it.id to (it.getString("username") ?: "Unknown") }
            avatars = usersSnap.documents.associate { it.id to (it.getString("avatarBase64") ?: "") }

            targetUserId = memberIds.first { it != currentUserId }
            targetUserName = members[targetUserId] ?: "User"
            targetUserAvatar = avatars[targetUserId] ?: ""

            currentUserName = members[currentUserId] ?: "Me"
            currentUserAvatar = avatars[currentUserId] ?: ""
        }
    }

    // تحميل الرسائل
    LaunchedEffect(chatId) {
        val chatRef = db.collection("chats").document(chatId).collection("messages")
        chatRef.orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages = snapshot.documents.mapNotNull { doc ->
                        val text = doc.getString("text") ?: return@mapNotNull null
                        val senderId = doc.getString("senderId") ?: ""
                        ChatMessage(senderId, text)
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // TopBar مع زر الرجوع وصورة الطرف التاني
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
                .padding(8.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            AvatarImage(targetUserAvatar, targetUserName) {
                navController.navigate("profileMainScreen/$targetUserId") {
                    launchSingleTop = true
                    restoreState = true
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Text(targetUserName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // عرض الرسائل
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                val isMine = msg.senderId == currentUserId
                val senderName = if(isMine) currentUserName else members[msg.senderId] ?: "Unknown"
                val avatarBase64 = if(isMine) currentUserAvatar else avatars[msg.senderId] ?: ""

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if(isMine) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    if(!isMine){
                        AvatarImage(avatarBase64, senderName) {
                            navController.navigate("profileMainScreen/${msg.senderId}") {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Column(horizontalAlignment = if(isMine) Alignment.End else Alignment.Start) {
                        if(!isMine){
                            Text(senderName, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Box(
                            modifier = Modifier
                                .background(if(isMine) Color(0xFF9B5DE5) else Color(0xFF333333), shape = RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text(msg.text, color = Color.White, fontSize = 16.sp)
                        }
                    }
                    if(isMine) Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        // إدخال الرسائل
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                .padding(6.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Type a message...", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    val text = messageText.trim()
                    if(text.isNotEmpty()){
                        coroutineScope.launch {
                            sendMessage(chatId, currentUserId, text)
                            messageText = ""
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF9B5DE5))
            }
        }
    }
}

@Composable
fun AvatarImage(avatarBase64: String, name: String, onClick: () -> Unit) {
    Box(modifier = Modifier
        .size(40.dp)
        .clip(CircleShape)
        .background(if(avatarBase64.isEmpty()) Color(0xFF9B5DE5) else Color.Gray)
        .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if(avatarBase64.isNotEmpty()){
            val bytes = Base64.decode(avatarBase64, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            Image(bitmap = bmp.asImageBitmap(), contentDescription = name, modifier = Modifier.fillMaxSize())
        } else {
            Text(name.firstOrNull()?.uppercase() ?: "U", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

data class ChatMessage(val senderId: String = "", val text: String = "")

suspend fun sendMessage(chatId: String, senderId: String, text: String){
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