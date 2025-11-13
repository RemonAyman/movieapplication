package com.example.myapplication.ui.screens.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
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
fun NewGroupScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

    var groupName by remember { mutableStateOf("") }
    var friends by remember { mutableStateOf<List<FriendItem>>(emptyList()) }
    var selectedFriends by remember { mutableStateOf(mutableSetOf<String>()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    // تحميل الأصدقاء من Firestore (users collection)
    LaunchedEffect(Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                friends = snapshot.documents
                    .filter { it.id != currentUserId } // استبعاد المستخدم نفسه
                    .map { doc ->
                        val name = doc.getString("username") ?: "Unknown"
                        FriendItem(doc.id, name)
                    }
            }
    }

    val filteredFriends = friends.filter {
        it.name.contains(searchText, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        // عنوان الصفحة مع Avatar أول حرف
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF9B5DE5), shape = CircleShape)
            ) {
                Text(
                    text = groupName.firstOrNull()?.uppercase() ?: "G",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                placeholder = { Text("Group Name", color = Color.Gray) },
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        // سيرش عن الأصدقاء
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = { Text("Search friends...", color = Color.Gray) },
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

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Select Friends",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // عرض الأصدقاء
        if (filteredFriends.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No friends found", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredFriends) { friend ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedFriends.contains(friend.id)) {
                                    selectedFriends.remove(friend.id)
                                } else {
                                    selectedFriends.add(friend.id)
                                }
                            }
                            .background(
                                if (selectedFriends.contains(friend.id)) Color(0xFF2A1B3D)
                                else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = friend.name,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        if (selectedFriends.contains(friend.id)) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFF9B5DE5)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // زر الإنشاء
        Button(
            onClick = {
                if (groupName.isNotBlank() && selectedFriends.isNotEmpty()) {
                    isLoading = true
                    val members = (selectedFriends + currentUserId).toList()

                    val groupData = hashMapOf(
                        "name" to groupName,
                        "isGroup" to true,
                        "members" to members,
                        "lastMessage" to "",
                        "lastMessageTime" to Timestamp.now()
                    )

                    db.collection("chats")
                        .add(groupData)
                        .addOnSuccessListener { docRef ->
                            isLoading = false
                            navController.navigate("chatDetail/${docRef.id}") {
                                popUpTo("chats") { inclusive = false }
                            }
                        }
                        .addOnFailureListener {
                            isLoading = false
                        }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5)),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Default.Group, contentDescription = "Group", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Create Group", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

// موديل بسيط للأصدقاء
data class FriendItem(
    val id: String,
    val name: String
)
