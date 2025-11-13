package com.example.myapplication.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateChatDetailScreen(chatId: String, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    // ✅ تحميل الرسائل في الوقت الحقيقي
    LaunchedEffect(chatId) {
        val chatRef = db.collection("chats").document(chatId).collection("messages")
        chatRef.orderBy("timestamp").addSnapshotListener { snapshot, _ ->
            if (snapshot != null && !snapshot.isEmpty) {
                messages = snapshot.documents.mapNotNull { doc ->
                    val text = doc.getString("text") ?: return@mapNotNull null
                    val senderId = doc.getString("senderId") ?: ""
                    ChatMessage(senderId, text)
                }
            } else {
                messages = emptyList()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(8.dp)
    ) {
        // ✅ عرض الرسائل
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                val isMine = msg.senderId == currentUserId
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (isMine) Color(0xFF9B5DE5) else Color(0xFF333333),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // ✅ إدخال الرسالة وإرسالها
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
                    val text = messageText.text.trim()
                    if (text.isNotEmpty()) {
                        coroutineScope.launch {
                            sendMessage(chatId, currentUserId, text)
                            messageText = TextFieldValue("")
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF9B5DE5))
            }
        }
    }
}

// ✅ موديل الرسالة
data class ChatMessage(
    val senderId: String = "",
    val text: String = ""
)

// ✅ دالة الإرسال
suspend fun sendMessage(chatId: String, senderId: String, text: String) {
    val db = FirebaseFirestore.getInstance()
    val messageData = hashMapOf(
        "senderId" to senderId,
        "text" to text,
        "timestamp" to Timestamp.now()
    )

    // إضافة الرسالة الجديدة
    db.collection("chats")
        .document(chatId)
        .collection("messages")
        .add(messageData)
        .await()

    // تحديث آخر رسالة في الشات
    db.collection("chats").document(chatId)
        .update(
            mapOf(
                "lastMessage" to text,
                "lastMessageTime" to Timestamp.now()
            )
        )
        .await()
}
