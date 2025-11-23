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
import androidx.compose.material.icons.filled.PersonAdd
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
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPrivateChatScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

    var searchText by remember { mutableStateOf("") }
    var users by remember { mutableStateOf<List<UserItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                users = snapshot.documents
                    .filter { it.id != currentUserId }
                    .map { doc ->
                        val name = doc.getString("username") ?: "Unknown"
                        val avatarBase64 = doc.getString("avatarBase64")
                        UserItem(doc.id, name, avatarBase64)
                    }
            }
    }

    val filteredUsers = users.filter {
        it.name.contains(searchText, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Start Private Chat", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1330))
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF121212))
                .padding(padding)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search user by name...", color = Color.Gray) },
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

            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No users found", color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(filteredUsers) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!isLoading) {
                                        isLoading = true
                                        createPrivateChat(
                                            db = db,
                                            navController = navController,
                                            currentUserId = currentUserId,
                                            targetUser = user,
                                            onFinish = { isLoading = false }
                                        )
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!user.avatarBase64.isNullOrEmpty()) {
                                val bytes = Base64.decode(user.avatarBase64, Base64.DEFAULT)
                                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = user.name,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.Gray, shape = CircleShape)
                                )
                            } else {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(0xFF9B5DE5), shape = CircleShape)
                                ) {
                                    Text(
                                        text = user.name.firstOrNull()?.uppercase() ?: "?",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = user.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f)
                            )

                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = "Start Chat",
                                tint = Color(0xFF9B5DE5)
                            )
                        }
                        Divider(color = Color.DarkGray, thickness = 0.6.dp)
                    }
                }
            }

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = Color(0xFF9B5DE5)
                )
            }
        }
    }
}

fun createPrivateChat(
    db: FirebaseFirestore,
    navController: NavController,
    currentUserId: String,
    targetUser: UserItem,
    onFinish: () -> Unit
) {
    val members = listOf(currentUserId, targetUser.id)

    db.collection("chats")
        .whereEqualTo("isGroup", false)
        .whereArrayContains("members", currentUserId)
        .get()
        .addOnSuccessListener { snapshot ->
            val existingChat = snapshot.documents.find { doc ->
                val membersList = doc.get("members") as? List<*> ?: emptyList<Any>()
                membersList.containsAll(members) && members.size == membersList.size
            }

            if (existingChat != null) {
                onFinish()
                navController.navigate("privateChatDetail/${existingChat.id}")
            } else {
                val chatData = hashMapOf(
                    "isGroup" to false,
                    "members" to members,
                    "name" to targetUser.name,
                    "lastMessage" to "",
                    "lastMessageTime" to Timestamp.now()
                )

                db.collection("chats")
                    .add(chatData)
                    .addOnSuccessListener { docRef ->
                        onFinish()
                        navController.navigate("privateChatDetail/${docRef.id}")
                    }
                    .addOnFailureListener { onFinish() }
            }
        }
        .addOnFailureListener { onFinish() }
}

data class UserItem(
    val id: String,
    val name: String,
    val avatarBase64: String? = null
)