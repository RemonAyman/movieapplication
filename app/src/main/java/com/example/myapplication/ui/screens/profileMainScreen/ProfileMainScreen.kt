package com.example.myapplication.ui.screens.profileMainScreen

import MovieApiModel
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.AppColors
import com.example.myapplication.ui.screens.favorites.FavoritesItem
import com.example.myapplication.viewmodel.FavoritesViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileMainScreen(
    navController: NavHostController,
    userId: String,
    onEditProfile: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onFriendsClick: () -> Unit = {},
    onRequestsClick: () -> Unit = {},
    onWatchlistClick: () -> Unit = { navController.navigate("watchlist")},
) {
    val favoritesViewModel: FavoritesViewModel = viewModel()
    val db = FirebaseFirestore.getInstance()

    var username by remember { mutableStateOf("") }
    var avatarBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var loading by remember { mutableStateOf(true) }

    val favorites by favoritesViewModel.favorites.collectAsState()

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

    LaunchedEffect(userId) {
        loadUserData()
        favoritesViewModel.loadFirst4Favorites()
    }

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

                // زر تعديل البروفايل
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

                // الأقسام
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Favorites Section مع See More
                    FavoritesSection(
                        favorites = favorites,
                        navController = navController,
                        onSeeMore = { onFavoritesClick() }
                    )
                    ProfileCardItem("Watchlist", Icons.Default.Visibility, onWatchlistClick)
                    ProfileCardItem("Friends", Icons.Default.Person, onFriendsClick)
                    ProfileCardItem("Friend Requests", Icons.Default.GroupAdd, onRequestsClick)
                }
            }
        }
    }
}

@Composable
fun FavoritesSection(
    favorites: List<FavoritesItem>,
    navController: NavHostController,
    onSeeMore: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Favorites",
                color = AppColors.TextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See More",
                color = AppColors.NeonGlow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onSeeMore() }
            )
        }

        MovieRow(
            movies = favorites.take(4), // نعرض أول 4
            navController = navController
        )
    }
}@Composable
fun MovieRow(
    movies: List<FavoritesItem>,
    navController: NavHostController
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(movies) { movie ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(if (isPressed) 0.95f else 1f)

            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(130.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .clip(RoundedCornerShape(16.dp))
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        navController.navigate("details/${movie.movieId}")
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                // Poster
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w500${movie.poster}",
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC2A1B3D))
                            )
                        )
                )

                // Title
                Text(
                    text = movie.title,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                        .align(Alignment.BottomCenter)
                )

                // Rating
                if (movie.rating > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(Color(0xAA000000), RoundedCornerShape(6.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = "Rating",
                                tint = Color.Yellow,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format("%.1f", movie.vote_average),
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
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
