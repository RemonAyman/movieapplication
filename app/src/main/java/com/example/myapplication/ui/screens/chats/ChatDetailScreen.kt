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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavController,
    chatId: String
) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var groupName by remember { mutableStateOf("Group Chat") }
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<MessageItem>>(emptyList()) }
    var members by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var avatars by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var showMembersSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Load group info and members
    LaunchedEffect(chatId) {
        db.collection("chats").document(chatId).get()
            .addOnSuccessListener { doc ->
                groupName = doc.getString("name") ?: "Group Chat"
                val memberIds = doc.get("members") as? List<String> ?: emptyList()
                if (memberIds.isNotEmpty()) {
                    db.collection("users").whereIn(FieldPath.documentId(), memberIds)
                        .get()
                        .addOnSuccessListener { usersSnap ->
                            members = usersSnap.documents.associate { userDoc ->
                                userDoc.id to (userDoc.getString("username") ?: "Unknown")
                            }
                            avatars = usersSnap.documents.associate { userDoc ->
                                userDoc.id to (userDoc.getString("avatarBase64") ?: "")
                            }
                        }
                }
            }
    }

    // Listener for messages
    DisposableEffect(chatId) {
        val listener = db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    messages = snapshot.documents.mapNotNull { doc ->
                        val text = doc.getString("text") ?: return@mapNotNull null
                        val senderId = doc.getString("senderId") ?: ""
                        val timestamp = doc.getTimestamp("timestamp")
                        MessageItem(doc.id, text, senderId, timestamp)
                    }
                }
            }
        onDispose { listener.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(groupName, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showMembersSheet = true }) {
                        Icon(Icons.Default.GroupAdd, contentDescription = "Add/Remove Member", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = false
                ) {
                    items(messages) { msg ->
                        val senderName = members[msg.senderId] ?: "Unknown"
                        val avatarBase64 = avatars[msg.senderId] ?: ""
                        MessageBubble(
                            msg,
                            isMe = msg.senderId == currentUserId,
                            senderName = senderName,
                            avatarBase64 = avatarBase64,
                            onAvatarClick = {
                                // Navigate to ProfileMainScreen of that user
                                navController.navigate("profileMainScreen/${msg.senderId}")
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Type a message...", color = Color.Gray) },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color(0xFF2A1B3D),
                            unfocusedContainerColor = Color(0xFF2A1B3D),
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )

                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                sendMessage(db, chatId, currentUserId, messageText.trim())
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(48.dp)
                            .background(Color(0xFF9B5DE5), RoundedCornerShape(24.dp))
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }

            if (showMembersSheet) {
                MembersBottomSheet(
                    members = members,
                    avatars = avatars,
                    db = db,
                    chatId = chatId,
                    currentUserId = currentUserId,
                    onDismiss = { showMembersSheet = false },
                    onUpdateMembers = { updatedMembers, actionUserId, affectedUsers, action ->
                        members = updatedMembers
                        val actionUserName = members[actionUserId] ?: "Someone"
                        affectedUsers.forEach { (targetUserId, targetUserName) ->
                            val sysMessage = when(action) {
                                "add" -> "$actionUserName added $targetUserName"
                                "remove" -> "$actionUserName removed $targetUserName"
                                else -> ""
                            }
                            if(sysMessage.isNotEmpty()){
                                sendMessage(db, chatId, actionUserId, sysMessage)
                            }
                        }
                    }
                )
            }
        }
    }
}

fun sendMessage(db: FirebaseFirestore, chatId: String, senderId: String, text: String) {
    val chatRef = db.collection("chats").document(chatId)
    val messageData = hashMapOf(
        "text" to text,
        "senderId" to senderId,
        "timestamp" to Timestamp.now()
    )
    chatRef.collection("messages").add(messageData)
    chatRef.set(
        mapOf(
            "lastMessage" to text,
            "lastMessageTime" to Timestamp.now(),
            "members" to FieldValue.arrayUnion(senderId)
        ),
        SetOptions.merge()
    )
}

@Composable
fun MessageBubble(
    message: MessageItem,
    isMe: Boolean,
    senderName: String,
    avatarBase64: String = "",
    onAvatarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            if (avatarBase64.isNotBlank()) {
                val bytes = Base64.decode(avatarBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onAvatarClick() }
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9B5DE5))
                        .clickable { onAvatarClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = senderName.firstOrNull()?.uppercase() ?: "U",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column {
            if (!isMe) {
                Text(
                    senderName,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        color = if (isMe) Color(0xFF9B5DE5) else Color(0xFF3A3A3A),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(message.text, color = Color.White, fontSize = 16.sp)
            }
        }

        if (isMe) Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun MembersBottomSheet(
    members: Map<String, String>,
    avatars: Map<String, String>,
    db: FirebaseFirestore,
    chatId: String,
    currentUserId: String,
    onDismiss: () -> Unit,
    onUpdateMembers: (Map<String,String>, String, List<Pair<String,String>>, String) -> Unit
) {
    val allUsers by remember { mutableStateOf(mutableStateListOf<Triple<String,String,String>>()) } // id, username, avatar
    var selectedUsers by remember { mutableStateOf(mutableStateListOf<Triple<String,String,String>>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("users").get()
            .addOnSuccessListener { snapshot ->
                allUsers.clear()
                allUsers.addAll(snapshot.documents.map {
                    Triple(it.id, it.getString("username") ?: "Unknown", it.getString("avatarBase64") ?: "")
                })
                isLoading = false
            }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x80000000))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 500.dp)
                .background(Color(0xFF1A1A1A), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .clickable(enabled = false) {}
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
            } else {
                Column {
                    Text("Manage Members", color = Color.White, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(allUsers) { user ->
                            val isMember = members.containsKey(user.first)
                            val isSelected = selectedUsers.any { it.first == user.first }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) {
                                            selectedUsers.removeAll { it.first == user.first }
                                        } else {
                                            selectedUsers.add(user)
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.Green else Color(0xFF9B5DE5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(user.second.firstOrNull()?.uppercase() ?: "U", color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(user.second, color = Color.White)
                                Spacer(modifier = Modifier.weight(1f))
                                if (isMember) Icon(Icons.Default.Check, contentDescription = "Member", tint = Color.Green)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                val chatRef = db.collection("chats").document(chatId)
                                val usersToAdd = selectedUsers.filter { !members.containsKey(it.first) }
                                val usersToRemove = selectedUsers.filter { members.containsKey(it.first) }

                                if (usersToAdd.isNotEmpty()) {
                                    chatRef.update("members", FieldValue.arrayUnion(*usersToAdd.map { it.first }.toTypedArray()))
                                        .addOnSuccessListener {
                                            val updatedMembers = members + usersToAdd.associate { it.first to it.second }
                                            onUpdateMembers(updatedMembers, currentUserId, usersToAdd.map { it.first to it.second }, "add")
                                        }
                                }

                                if (usersToRemove.isNotEmpty()) {
                                    chatRef.update("members", FieldValue.arrayRemove(*usersToRemove.map { it.first }.toTypedArray()))
                                        .addOnSuccessListener {
                                            val updatedMembers = members - usersToRemove.map { it.first }.toSet()
                                            onUpdateMembers(updatedMembers, currentUserId, usersToRemove.map { it.first to it.second }, "remove")
                                        }
                                }

                                selectedUsers.clear()
                                onDismiss()
                            }
                        ) {
                            Text("Apply", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

data class MessageItem(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val timestamp: Timestamp? = null
)
