package com.example.myapplication.ui.screens.profileMainScreen

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.myapplication.ui.commonComponents.PremiumMovieRow
import com.example.myapplication.ui.commonComponents.PremiumSectionTitle
import com.example.myapplication.ui.screens.favorites.FavoritesItem
import com.example.myapplication.ui.screens.ratings.RatingItem
import com.example.myapplication.ui.screens.watched.WatchedItem
import com.example.myapplication.ui.watchlist.WatchlistItem
import com.example.myapplication.ui.theme.MovitoBackground
import com.example.myapplication.ui.theme.PrimaryPurple
import com.example.myapplication.ui.theme.CardBackground
import com.example.myapplication.ui.theme.DarkPurple
import androidx.compose.ui.graphics.Brush
import com.example.myapplication.appConstant.AppConstants

@Composable
fun ProfileMainScreen(
    navController: NavHostController,
    viewModel: ProfileScreenViewModel = viewModel(factory = ProfileScreenViewModelFactory(null)),
    onEditProfile: () -> Unit,
    onFavoritesClick: () -> Unit,
    onFriendsClick: () -> Unit,
    onRequestsClick: () -> Unit,
    onWatchlistClick: () -> Unit,
    onWatchedClick: () -> Unit,
    onRatingsClick: () -> Unit,
    userId: String = AppConstants.CURRENT_USER_ID,
) {
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MovitoBackground)
    ) {
        // ===== TOP GRADIENT AREA =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF23133D),
                            Color(0xFF0F0421),
                            MovitoBackground
                        ),
                        startY = 0.9f,
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 30.dp)
            ) {
                // ===== Edit Icon فوق على اليمين =====
                if (userId == AppConstants.CURRENT_USER_ID) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .size(25.dp)
                                .clickable { onEditProfile() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(200.dp))
                // ===== PROFILE HEADER =====
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar
                    val bmp = remember(uiState.avatarBase64) {
                        try {
                            if (uiState.avatarBase64.isNotEmpty()) {
                                val bytes = Base64.decode(uiState.avatarBase64, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                            } else null
                        } catch (e: Exception) {
                            null
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (bmp != null) {
                            Image(
                                bitmap = bmp,
                                contentDescription = "avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = uiState.username.firstOrNull()?.uppercase() ?: "U",
                                color = Color.White,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = uiState.username,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Since ${uiState.joinDate}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onFriendsClick() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.People, // أيقونة الأصدقاء
                                contentDescription = "Friends",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${uiState.friendsCount}",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ===== TIME SPENT WATCHING CARD =====
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        PrimaryPurple.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Time spent watching TV",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            val timeString = uiState.totalWatchTime
                            val days = timeString.substringBefore("d").trim()
                            val hours = timeString.substringAfter("d").substringBefore("h").trim()
                            val minutes = timeString.substringAfter("h").substringBefore("m").trim()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                TimeStatItem(days, "Days")
                                TimeStatItem(hours, "Hours")
                                TimeStatItem(minutes, "Minutes")

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(50.dp)
                                        .background(Color.White.copy(alpha = 0.15f))
                                )

                                TimeStatItem(
                                    value = uiState.watchedMovies.size.toString(),
                                    label = "Movies"
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }


        // ===== باقي الشاشة بدون gradient =====
        if (uiState.favoriteMovies.isNotEmpty()) {
            PremiumSectionTitle(
                title = "Favorites",
                subtitle = "Your top picks",
                onSeeMoreClick = onFavoritesClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            PremiumMovieRow(
                movies = uiState.favoriteMovies.map { it.toMovieApiModel() },
                navController = navController,
                isLoading = false
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (uiState.ratingsMovies.isNotEmpty()) {
            PremiumSectionTitle(
                title = "Ratings",
                subtitle = "Movies you've rated",
                onSeeMoreClick = onRatingsClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            PremiumMovieRow(
                movies = uiState.ratingsMovies.map { it.toMovieApiModel() },
                navController = navController,
                isLoading = false
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (uiState.watchlistMovies.isNotEmpty()) {
            PremiumSectionTitle(
                title = "Watchlist",
                subtitle = "Movies to watch later",
                onSeeMoreClick = onWatchlistClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            PremiumMovieRow(
                movies = uiState.watchlistMovies.map { it.toMovieApiModel() },
                navController = navController,
                isLoading = false
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (uiState.watchedMovies.isNotEmpty()) {
            PremiumSectionTitle(
                title = "Watched",
                subtitle = "Your viewing history",
                onSeeMoreClick = onWatchedClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            PremiumMovieRow(
                movies = uiState.watchedMovies.map { it.toMovieApiModel() },
                navController = navController,
                isLoading = false
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun TimeStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
    }
}

// ===== Extension Functions للتحويل =====
fun FavoritesItem.toMovieApiModel() = com.example.myapplication.data.remote.MovieApiModel(
    id = movieId.toIntOrNull() ?: 0,
    title = title,
    overview = "",
    poster_path = poster,
    backdrop_path = null,
    release_date = "",
    vote_average = vote_average.toDouble(),
    genre_ids = emptyList(),
    original_language = "en"
)

fun RatingItem.toMovieApiModel() = com.example.myapplication.data.remote.MovieApiModel(
    id = movieId.toIntOrNull() ?: 0,
    title = title,
    overview = review,
    poster_path = poster,
    backdrop_path = null,
    release_date = "",
    vote_average = vote_average,
    genre_ids = emptyList(),
    original_language = "en"
)

fun WatchlistItem.toMovieApiModel() = com.example.myapplication.data.remote.MovieApiModel(
    id = movieId.toIntOrNull() ?: 0,
    title = title,
    overview = "",
    poster_path = poster,
    backdrop_path = null,
    release_date = "",
    vote_average = 0.0,
    genre_ids = emptyList(),
    original_language = "en"
)

fun WatchedItem.toMovieApiModel() = com.example.myapplication.data.remote.MovieApiModel(
    id = movieId.toIntOrNull() ?: 0,
    title = title,
    overview = "",
    poster_path = poster,
    backdrop_path = null,
    release_date = "",
    vote_average = vote_average.toDouble(),
    genre_ids = emptyList(),
    original_language = "en"
)
