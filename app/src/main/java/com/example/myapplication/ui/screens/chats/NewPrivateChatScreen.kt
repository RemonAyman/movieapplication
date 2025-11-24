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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
                title = {
                    Text(
                        "Start Private Chat",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1330))
            )
        },
        containerColor = Color(0xFF0F0820)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0820))
                .padding(padding)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search user by name...", color = Color.Gray) },
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

            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No users found",
                            color = Color(0xFFBDBDBD),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Try a different search term",
                            color = Color(0xFF9B5DE5).copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredUsers) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(16.dp))
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
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1330)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // ✅ Avatar with proper styling
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
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
                                    if (!user.avatarBase64.isNullOrEmpty()) {
                                        val bytes = Base64.decode(user.avatarBase64, Base64.DEFAULT)
                                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = user.name,
                                            contentScale = ContentScale.Crop,  // ✅ أهم تعديل
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Text(
                                            text = user.name.firstOrNull()?.uppercase() ?: "?",
                                            color = Color(0xFF9B5DE5),
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Text(
                                    text = user.name,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    Icons.Default.PersonAdd,
                                    contentDescription = "Start Chat",
                                    tint = Color(0xFF9B5DE5),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
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