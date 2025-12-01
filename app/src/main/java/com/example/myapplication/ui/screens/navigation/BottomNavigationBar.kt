package com.example.myapplication.ui.navigation

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    // ⭐ جلب بيانات المستخدم من Firebase
    var username by remember { mutableStateOf("") }
    var avatarBase64 by remember { mutableStateOf("") }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            try {
                val doc = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUserId)
                    .get()
                    .await()

                username = doc.getString("username") ?: ""
                avatarBase64 = doc.getString("avatarBase64") ?: ""
            } catch (e: Exception) {
                // في حالة الخطأ
            }
        }
    }

    val items = listOf(
        BottomNavItem("HomeScreen", Icons.Default.Home, isProfile = false),
        BottomNavItem("search", Icons.Default.Search, isProfile = false),
        BottomNavItem("chats", Icons.Default.Chat, isProfile = false),
        BottomNavItem("addFriend", Icons.Default.PersonAdd, isProfile = false),
        BottomNavItem("profile", Icons.Default.Person, isProfile = true)
    )

    var currentRoute by remember { mutableStateOf(navController.currentDestination?.route ?: "") }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            currentRoute = entry.destination.route ?: ""
        }
    }

    NavigationBar(containerColor = Color(0xFF1A1A1A)) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            val scale by animateFloatAsState(targetValue = if (isSelected) 1.35f else 1f, label = "")

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .background(Color(0xFF9B5DE5), CircleShape)
                    )
                }

                IconButton(
                    onClick = {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            restoreState = true
                        }
                    },
                    modifier = Modifier.scale(scale)
                ) {
                    // ⭐ إذا كان Profile، عرض الصورة أو الحرف الأول
                    if (item.isProfile) {
                        ProfileAvatar(
                            avatarBase64 = avatarBase64,
                            username = username,
                            isSelected = isSelected
                        )
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.route,
                            tint = if (isSelected) Color.White else Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileAvatar(
    avatarBase64: String,
    username: String,
    isSelected: Boolean
) {
    // تحويل Base64 إلى صورة
    val bitmap = remember(avatarBase64) {
        try {
            if (avatarBase64.isNotEmpty()) {
                val bytes = Base64.decode(avatarBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (bitmap == null) Color(0xFF9B5DE5).copy(alpha = 0.5f) else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            // عرض الصورة
            Image(
                bitmap = bitmap,
                contentDescription = "Profile Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            // عرض الحرف الأول من الاسم
            Text(
                text = username.firstOrNull()?.uppercase() ?: "U",
                color = if (isSelected) Color.White else Color.LightGray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isProfile: Boolean = false
)