package com.example.myapplication.ui.details

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.data.remote.*
import com.example.myapplication.ui.screens.favorites.FavoritesItem
import com.example.myapplication.ui.screens.watched.WatchedItem
import com.example.myapplication.ui.theme.MovitoBackground
import com.example.myapplication.ui.watchlist.WatchlistItem
import com.example.myapplication.ui.watchlist.WatchlistViewModel
import com.example.myapplication.ui.watchlist.WatchlistViewModelFactory
import com.example.myapplication.viewmodel.FavoritesViewModel
import com.example.myapplication.viewmodel.WatchedViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailsScreen(
    navController: NavHostController,
    movieId: Int,
    favoritesViewModel: FavoritesViewModel,
) {
    val apiService = remember { MovieApiService.create() }
    val context = LocalContext.current
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val watchlistViewModel: WatchlistViewModel = viewModel(
        factory = WatchlistViewModelFactory(currentUserId)
    )

    // ✅ إضافة WatchedViewModel
    val watchedViewModel: WatchedViewModel = viewModel()

    var isInWatchlist by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    var isWatched by remember { mutableStateOf(false) }
    var movie by remember { mutableStateOf<MovieApiModel?>(null) }
    var trailerKey by remember { mutableStateOf<String?>(null) }
    var castList by remember { mutableStateOf<List<CastMember>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userRating by remember { mutableStateOf(0f) }

    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    // Animation for poster entrance
    var posterVisible by remember { mutableStateOf(false) }
    val posterAlpha by animateFloatAsState(
        targetValue = if (posterVisible) 1f else 0f,
        animationSpec = tween(600)
    )
    val posterScale by animateFloatAsState(
        targetValue = if (posterVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    // ===== Load movie details =====
    LaunchedEffect(movieId) {
        isLoading = true
        errorMessage = null
        try {
            val movieDetails = apiService.getMovieDetails(movieId)
            movie = MovieApiModel(
                id = movieDetails.id,
                title = movieDetails.title,
                overview = movieDetails.overview,
                poster_path = movieDetails.poster_path,
                release_date = movieDetails.release_date,
                vote_average = movieDetails.vote_average,
                genre_ids = emptyList(),
                original_language = movieDetails.original_language ?: "ar"
            )

            val videos = apiService.getMovieVideos(movieId)
            trailerKey = videos.results.find { it.site == "YouTube" && it.type == "Trailer" }?.key

            val credits = apiService.getMovieCredits(movieId)
            castList = credits.cast.take(10)

            delay(100)
            posterVisible = true
            isLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "Failed to load movie details. Please try again."
            isLoading = false
        }
    }

    // ===== Watchlist collector =====
    LaunchedEffect(movie) {
        movie?.let { m ->
            watchlistViewModel.watchlist.collectLatest { list ->
                isInWatchlist = list.any { it.movieId == m.id.toString() }
            }
        }
    }

    // ✅ Watched collector
    LaunchedEffect(movie) {
        movie?.let { m ->
            watchedViewModel.watched.collectLatest { list ->
                isWatched = list.any { it.movieId == m.id.toString() }
            }
        }
    }

    // ===== Error/Loading States =====
    if (isLoading) {
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

    if (errorMessage != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MovitoBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(errorMessage!!, color = Color.Red, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5))
                ) {
                    Text("Retry")
                }
            }
        }
        return
    }

    val movieData = movie!!

    // ===== Animated Icon Button =====
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
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
        val tint by animateColorAsState(
            targetValue = if (isActive) activeColor else inactiveColor,
            animationSpec = tween(300)
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

    // ===== Rating Description =====
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

    // ============================
    // Bottom Sheet
    // ============================
    if (showSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            containerColor = Color(0xFF2A1B3D),
            onDismissRequest = { showSheet = false }
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                // Title
                Text(
                    "Manage Movie",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Row 1: Watchlist / Favorites / Watched
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Watchlist
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AnimatedIconButton(
                            isActive = isInWatchlist,
                            activeColor = Color(0xFFE74C3C),
                            icon = {
                                Icon(
                                    painterResource(
                                        id = if (isInWatchlist) R.drawable.remove_from_wachlist
                                        else R.drawable.add_to_wachlist
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        ) {
                            if (isInWatchlist) {
                                watchlistViewModel.removeFromWatchlist(movieData.id.toString())
                            } else {
                                watchlistViewModel.addToWatchlist(
                                    WatchlistItem(
                                        movieId = movieData.id.toString(),
                                        title = movieData.title,
                                        poster = "https://image.tmdb.org/t/p/w500${movieData.poster_path}"
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (isInWatchlist) "In Watchlist" else "Add to List",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    // Favorites
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AnimatedIconButton(
                            isActive = isFavorite,
                            activeColor = Color(0xFFE91E63),
                            icon = {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite
                                    else Icons.Outlined.FavoriteBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        ) {
                            val favItem = FavoritesItem(
                                movieData.id.toString(),
                                movieData.title,
                                "https://image.tmdb.org/t/p/w500${movieData.poster_path}"
                            )
                            if (isFavorite) {
                                favoritesViewModel.removeFromFavorites(favItem.movieId)
                            } else {
                                favoritesViewModel.addToFavorites(favItem)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (isFavorite) "Favorited" else "Favorite",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    // ✅ Watched - نفس اللوجيك
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AnimatedIconButton(
                            isActive = isWatched,
                            activeColor = Color(0xFF4CAF50),
                            icon = {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp)
                                )
                            },
                            onClick = {
                                if (isWatched) {
                                    watchedViewModel.removeFromWatched(movieData.id.toString())
                                } else {
                                    watchedViewModel.addToWatched(
                                        WatchedItem(
                                            movieId = movieData.id.toString(),
                                            title = movieData.title,
                                            poster = "https://image.tmdb.org/t/p/w500${movieData.poster_path}",
                                            rating = 0,
                                            vote_average = movieData.vote_average.toInt()
                                        )
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (isWatched) "Watched" else "Mark Watched",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Divider
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ===== Rating Section =====
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

                    // Container للنجوم مع الـ Slider
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(50.dp)
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val newRating = (offset.x / size.width) * 5f
                                    userRating = ((newRating * 2).toInt() / 2f).coerceIn(0f, 5f)
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                    ) {
                        // Slider شفاف
                        Slider(
                            value = userRating,
                            onValueChange = {
                                userRating = ((it * 2).toInt() / 2f).coerceIn(0f, 5f)
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            valueRange = 0f..5f,
                            steps = 9,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .alpha(0f)
                        )

                        // Stars Row
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            for (i in 1..5) {
                                val icon = when {
                                    userRating >= i -> Icons.Rounded.Star
                                    userRating >= i - 0.5f -> Icons.Rounded.StarHalf
                                    else -> Icons.Rounded.StarOutline
                                }

                                val iconScale by animateFloatAsState(
                                    targetValue = if (userRating >= i - 0.5f) 1.1f else 1f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                )

                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (userRating >= i - 0.5f) Color(0xFFFFD700) else Color.Gray,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .padding(horizontal = 4.dp)
                                        .scale(iconScale)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rating Text with animation
                    val ratingText = if (userRating > 0f) {
                        " ${getRatingDescription(userRating)}"
                    } else {
                        getRatingDescription(userRating)
                    }

                    Text(
                        text = ratingText,
                        color = Color(0xFFFFD700),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // DONE Button
                Button(
                    onClick = {
                        showSheet = false
                        // هنا ممكن تحفظ الـ rating في database
                    },
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

    // ============================
    // Main Movie Details (نفس الكود السابق)
    // ============================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MovitoBackground)
            .padding(bottom = 16.dp)
    ) {
        // Movie Poster
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        ) {
            // Poster with animation
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movieData.poster_path}",
                contentDescription = movieData.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(posterAlpha)
                    .scale(posterScale)
            )

            // Gradient overlay
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

            // Top bar
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
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = { showSheet = true },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                }
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        // Movie Info
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // Title
            Text(
                movieData.title,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Meta info with improved design
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rating chip
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

                // Year
                movieData.release_date?.let { releaseDate ->
                    if (releaseDate.isNotEmpty()) {
                        Text(
                            releaseDate.take(4),
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }
                }

                // Language
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

            // Overview
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

            // Trailer Button with improved design
            if (trailerKey != null) {
                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/watch?v=$trailerKey")
                        )
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9B5DE5)
                    ),
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

            // CAST with improved design
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

                if (castList.isNotEmpty()) {
                    Text(
                        "View All",
                        color = Color(0xFF9B5DE5),
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { /* Navigate to full cast */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (castList.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    items(castList) { actor ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(90.dp)
                        ) {
                            // Actor image with border
                            Box {
                                val hasProfilePath = actor.profile_path != null && actor.profile_path.isNotEmpty()

                                if (hasProfilePath) {
                                    AsyncImage(
                                        model = "https://image.tmdb.org/t/p/w200${actor.profile_path}",
                                        contentDescription = actor.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4A3A64))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4A3A64)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            actor.name.take(1),
                                            color = Color.White,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Actor name
                            Text(
                                actor.name,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
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
        }
    }
}