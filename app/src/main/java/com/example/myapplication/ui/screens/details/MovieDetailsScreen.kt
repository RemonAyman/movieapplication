package com.example.myapplication.ui.screens.details

import com.example.myapplication.R
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarHalf
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.MovitoBackground
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.example.myapplication.ui.commonComponents.PremiumMovieCard
import com.example.myapplication.ui.commonComponents.PremiumMovieRow
import com.example.myapplication.ui.commonComponents.PremiumSectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailsScreen(
    navController: NavHostController,
    viewModel: MovieDetailsScreenViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    val posterAlpha by animateFloatAsState(
        targetValue = if (uiState.posterVisible) 1f else 0f,
        animationSpec = tween(600), label = ""
    )
    val posterScale by animateFloatAsState(
        targetValue = if (uiState.posterVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = ""
    )

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MovitoBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = Color(0xFF9B5DE5),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading movie details...", color = Color.White)
            }
        }
        return
    }

    if (uiState.error != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MovitoBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(uiState.error!!, color = Color.Red, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.retry() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5))
                ) {
                    Text("Retry")
                }
            }
        }
        return
    }

    val movieData = uiState.movie ?: return

    @Composable
    fun AnimatedIconButton(
        isActive: Boolean,
        activeColor: Color,
        inactiveColor: Color = Color.White,
        icon: @Composable () -> Unit,
        onClick: () -> Unit,
    ) {
        val scale by animateFloatAsState(
            targetValue = if (isActive) 1.2f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = ""
        )
        val tint by animateColorAsState(
            targetValue = if (isActive) activeColor else inactiveColor,
            animationSpec = tween(300), label = ""
        )

        IconButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
            modifier = Modifier.scale(scale)
        ) {
            CompositionLocalProvider(LocalContentColor provides tint) {
                icon()
            }
        }
    }

    @Composable
    fun getRatingDescription(rating: Float): String {
        return when {
            rating == 0f -> "Tap stars to rate"
            rating <= 1f -> "Terrible"
            rating <= 2f -> "Poor"
            rating <= 3f -> "Average"
            rating <= 4f -> "Good"
            rating < 5f -> "Great"
            else -> "Masterpiece!"
        }
    }

    // Main Content
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .background(MovitoBackground)
                .padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            ) {
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w500${movieData.poster_path}",
                    contentDescription = movieData.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(posterAlpha)
                        .scale(posterScale)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color(0xFF1A1A2E).copy(alpha = 0.7f),
                                    Color(0xFF1A1A2E)
                                ),
                                startY = 0f,
                                endY = 1000f
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { showShareSheet = true },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { showSheet = true },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    movieData.title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                String.format("%.1f", movieData.vote_average),
                                color = Color(0xFFFFD700),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    movieData.release_date?.let { releaseDate ->
                        if (releaseDate.isNotEmpty()) {
                            Text(
                                releaseDate.take(4),
                                color = Color.LightGray,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            movieData.original_language.uppercase(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Overview",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    movieData.overview.ifEmpty { "No description available." },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.trailerKey != null) {
                    Button(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.youtube.com/watch?v=${uiState.trailerKey}")
                            )
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Watch Trailer",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Cast",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (uiState.castList.isNotEmpty()) {
                        Text(
                            "View All",
                            color = Color(0xFF9B5DE5),
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.castList.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 0.dp)
                    ) {
                        items(uiState.castList) { actor ->
                            ActorItem(
                                actor = actor,
                                onClick = {
                                    navController.navigate("actorDetails/${actor.id}")
                                }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No cast information available", color = Color.Gray, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                PremiumSectionTitle(
                    title = "Similar Movies",
                    subtitle = "If you liked this, try these"
                )

                PremiumMovieRow(
                    movies = uiState.similarMovies,
                    navController = navController,
                    isLoading = uiState.isLoadingSimilar
                )
            }
        }

        // ✅ Share Sheet - Outside the scrollable content
        if (showShareSheet) {
            ShareMovieBottomSheet(
                navController = navController,
                movie = movieData,
                onDismiss = { showShareSheet = false }
            )
        }

        // Management Sheet
        if (showSheet) {
            ModalBottomSheet(
                sheetState = sheetState,
                containerColor = Color(0xFF2A1B3D),
                onDismissRequest = { showSheet = false }
            ) {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Text(
                        "Manage Movie",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AnimatedIconButton(
                                isActive = uiState.isInWatchlist,
                                activeColor = Color(0xFFE74C3C),
                                icon = {
                                    Icon(
                                        painterResource(
                                            id = if (uiState.isInWatchlist) R.drawable.remove_from_wachlist
                                            else R.drawable.add_to_wachlist
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            ) {
                                viewModel.toggleWatchlist()
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (uiState.isInWatchlist) "In Watchlist" else "Add to WatchList",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AnimatedIconButton(
                                isActive = uiState.isFavorite,
                                activeColor = Color(0xFFE91E63),
                                icon = {
                                    Icon(
                                        imageVector = if (uiState.isFavorite) Icons.Filled.Favorite
                                        else Icons.Outlined.FavoriteBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            ) {
                                viewModel.toggleFavorite()
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (uiState.isFavorite) "Liked" else "Like",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AnimatedIconButton(
                                isActive = uiState.isWatched,
                                activeColor = Color(0xFF4CAF50),
                                icon = {
                                    Icon(
                                        Icons.Default.Visibility,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            ) {
                                viewModel.toggleWatched()
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (uiState.isWatched) "Watched" else "Mark Watched",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Rate This Movie",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(50.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        val newRating = (offset.x / size.width) * 5f
                                        val rating = ((newRating * 2).toInt() / 2f).coerceIn(0f, 5f)
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.setRating(rating)
                                    }
                                }
                        ) {
                            Slider(
                                value = uiState.userRating,
                                onValueChange = {
                                    val rating = ((it * 2).toInt() / 2f).coerceIn(0f, 5f)
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setRating(rating)
                                },
                                valueRange = 0f..5f,
                                steps = 9,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .alpha(0f)
                            )

                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            ) {
                                for (i in 1..5) {
                                    val icon = when {
                                        uiState.userRating >= i -> Icons.Rounded.Star
                                        uiState.userRating >= i - 0.5f -> Icons.Rounded.StarHalf
                                        else -> Icons.Rounded.StarOutline
                                    }

                                    val iconScale by animateFloatAsState(
                                        targetValue = if (uiState.userRating >= i - 0.5f) 1.1f else 1f,
                                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                        label = ""
                                    )

                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (uiState.userRating >= i - 0.5f) Color(0xFFFFD700) else Color.Gray,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .padding(horizontal = 4.dp)
                                            .scale(iconScale)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val ratingText = if (uiState.userRating > 0f) {
                            " ${getRatingDescription(uiState.userRating)}"
                        } else {
                            getRatingDescription(uiState.userRating)
                        }

                        Text(
                            text = ratingText,
                            color = Color(0xFFFFD700),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { showSheet = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(52.dp)
                    ) {
                        Text("Done", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}