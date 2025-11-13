package com.example.myapplication.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // ✅ تحميل جميع المستخدمين من Firestore (باستثناء المستخدم الحالي)
    LaunchedEffect(Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                users = snapshot.documents
                    .filter { it.id != currentUserId }
                    .map { doc ->
                        val name = doc.getString("username") ?: "Unknown"
                        UserItem(doc.id, name)
                    }
            }
    }

    val filteredUsers = users.filter {
        it.name.contains(searchText, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text(
            text = "Start Private Chat",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ مربع البحث
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

        // ✅ عرض المستخدمين
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

        // ✅ شريط التحميل عند الإنشاء
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

// ✅ دالة إنشاء شات فردي جديد أو فتح الموجود
fun createPrivateChat(
    db: FirebaseFirestore,
    navController: NavController,
    currentUserId: String,
    targetUser: UserItem,
    onFinish: () -> Unit
) {
    val members = listOf(currentUserId, targetUser.id)

    // أولًا: التأكد هل فيه شات خاص موجود بالفعل
    db.collection("chats")
        .whereEqualTo("isGroup", false)
        .whereArrayContains("members", currentUserId)
        .get()
        .addOnSuccessListener { snapshot ->
            val existingChat = snapshot.documents.find { doc ->
                val membersList = doc.get("members") as? List<*> ?: emptyList<Any>()
                membersList.containsAll(members)
            }

            if (existingChat != null) {
                // ✅ لو الشات موجود بالفعل → نروح له
                onFinish()
                navController.navigate("privateChatDetail/${existingChat.id}")
            } else {
                // ✅ إنشاء شات جديد
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

// ✅ موديل المستخدم
data class UserItem(
    val id: String,
    val name: String
)
