package com.example.myapplication.ui.screens.profileMainScreen

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.AppColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileMainScreen(
    navController: NavHostController,
    userId: String, // ✅ البروفايل اللي عايز تعرضه
    onEditProfile: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onFriendsClick: () -> Unit = {},
    onRequestsClick: () -> Unit = {},
    onWatchlistClick: () -> Unit = { navController.navigate("watchlist") }
) {
    val db = FirebaseFirestore.getInstance()
    var username by remember { mutableStateOf("") }
    var avatarBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var loading by remember { mutableStateOf(true) }

    // تحميل بيانات أي يوزر حسب userId
    suspend fun loadUserData() {
        try {
            val snapshot = db.collection("users").document(userId).get().await()
            if (snapshot.exists()) {
                username = snapshot.getString("username") ?: "No Name"
                val avatarBase64 = snapshot.getString("avatarBase64")
                if (!avatarBase64.isNullOrEmpty()) {
                    val decodedBytes = Base64.decode(avatarBase64, Base64.DEFAULT)
                    avatarBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                }
            }
        } catch (e: Exception) {
            println("❌ Failed to load user data: ${e.message}")
        } finally {
            loading = false
        }
    }

    LaunchedEffect(userId) { loadUserData() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBg)
            .padding(16.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = AppColors.NeonGlow
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(AppColors.NeonGlow.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            bitmap = avatarBitmap!!.asImageBitmap(),
                            contentDescription = "User Avatar",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = if (username.isNotEmpty()) username.first().uppercase() else "?",
                            color = AppColors.NeonGlow,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // اسم المستخدم
                Text(
                    text = username,
                    color = AppColors.TextColor,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // زر تعديل البروفايل بس يظهر لو ده المستخدم نفسه
                if(userId == FirebaseAuth.getInstance().currentUser?.uid) {
                    Button(
                        onClick = onEditProfile,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.NeonGlow),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(48.dp)
                    ) {
                        Text("Edit Profile", color = AppColors.TextColor, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // الأقسام
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileCardItem("Favorites", Icons.Default.Favorite, onFavoritesClick)
                    ProfileCardItem("Friends", Icons.Default.Person, onFriendsClick)
                    ProfileCardItem("Friend Requests", Icons.Default.GroupAdd, onRequestsClick)
                    ProfileCardItem("Watchlist", Icons.Default.Visibility, onWatchlistClick)
                }
            }
        }
    }
}

@Composable
fun ProfileCardItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = AppColors.DarkBg.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = AppColors.NeonGlow,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                color = AppColors.TextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileModernScreenFirebasePreview() {
    // ⚠️ Preview: لازم تبعت navController حقيقي عشان ما يحصلش crash
}
