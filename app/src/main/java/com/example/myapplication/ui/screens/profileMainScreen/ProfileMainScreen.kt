package com.example.myapplication.ui.screens.profileMainScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.AppColors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileMainScreen(
    navController: NavHostController? = null,
    onEditProfile: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onFriendsClick: () -> Unit = {},
    onRequestsClick: () -> Unit = {},
    onWatchlistClick: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    // moaaz: حالة تخزين بيانات المستخدم
    var username by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    // moaaz: تحميل البيانات من Firebase
    suspend fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        try {
            val snapshot = db.collection("users").document(uid).get().await()
            if (snapshot.exists()) {
                username = snapshot.getString("username") ?: "No Name"
                avatarUrl = snapshot.getString("image") // لو عندك خانة الصورة اسمها "image"
            }
        } catch (e: Exception) {
            println("❌ Failed to load user data: ${e.message}")
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        loadUserData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBg)
            .padding(16.dp)
    ) {
        if (loading) {
            // moaaz: مؤشر تحميل أثناء جلب البيانات
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = AppColors.NeonGlow
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // moaaz: Avatar حقيقي لو موجود، وإلا أول حرف من الاسم
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(AppColors.NeonGlow.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUrl.isNullOrEmpty()) {
                        // moaaz: نعرض الصورة باستخدام Coil
                        androidx.compose.foundation.Image(
                            painter = rememberAsyncImagePainter(avatarUrl),
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

                // moaaz: اسم المستخدم الحقيقي
                Text(
                    text = username,
                    color = AppColors.TextColor,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // moaaz: زر تعديل البروفايل
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

                // moaaz: الأقسام (Favorites, Friends, Requests, Watchlist)
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
    ProfileMainScreen()
}
