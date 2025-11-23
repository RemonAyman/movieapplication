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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.data.remote.*
import com.example.myapplication.ui.screens.favorites.FavoritesItem
import com.example.myapplication.ui.theme.MovitoBackground
import com.example.myapplication.ui.watchlist.WatchlistItem
import com.example.myapplication.ui.watchlist.WatchlistViewModel
import com.example.myapplication.ui.watchlist.WatchlistViewModelFactory
import com.example.myapplication.viewmodel.FavoritesViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

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

    var isWatched by remember { mutableStateOf(false) }
    var isInWatchlist by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    var movie by remember { mutableStateOf<MovieApiModel?>(null) }
    var trailerKey by remember { mutableStateOf<String?>(null) }
    var castList by remember { mutableStateOf<List<CastMember>>(emptyList()) }

    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    // ===== Load movie details =====
    LaunchedEffect(movieId) {
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
            castList = credits.cast
        } catch (e: Exception) {
            e.printStackTrace()
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

    if (movie == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MovitoBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF9B5DE5))
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
            animationSpec = tween(300)
        )
        val tint by animateColorAsState(
            targetValue = if (isActive) activeColor else inactiveColor,
            animationSpec = tween(300)
        )

        IconButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
            modifier = Modifier.scale(scale)
        ) {
            CompositionLocalProvider(LocalContentColor provides tint) {
                icon()
            }
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
            // Row 1: Watchlist / Favorites / Watched
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Watchlist
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedIconButton(
                        isActive = isInWatchlist,
                        activeColor = Color.Red,
                        icon = {
                            Icon(
                                painterResource(
                                    id = if (isInWatchlist) R.drawable.remove_from_wachlist
                                    else R.drawable.add_to_wachlist
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    ) {
                        if (isInWatchlist) watchlistViewModel.removeFromWatchlist(movieData.id.toString())
                        else watchlistViewModel.addToWatchlist(
                            WatchlistItem(
                                movieId = movieData.id.toString(),
                                title = movieData.title,
                                poster = "https://image.tmdb.org/t/p/w500${movieData.poster_path}"
                            )
                        )
                    }
                    Text("Watchlist", color = Color.White, fontSize = 13.sp)
                }

                // Favorites
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedIconButton(
                        isActive = isFavorite,
                        activeColor = Color.Magenta,
                        icon = {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Favorite
                                else Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    ) {
                        isFavorite = !isFavorite
                        val favItem = FavoritesItem(
                            movieData.id.toString(),
                            movieData.title,
                            "https://image.tmdb.org/t/p/w500${movieData.poster_path}"
                        )
                        if (isFavorite) favoritesViewModel.addToFavorites(favItem)
                        else favoritesViewModel.removeFromFavorites(favItem.movieId)
                    }
                    Text(if (isFavorite) "Liked" else "Like", color = Color.White, fontSize = 13.sp)
                }

                // Watched
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedIconButton(
                        isActive = isWatched,
                        activeColor = Color.Green,
                        icon = {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(34.dp)
                            )
                        },
                        onClick = { isWatched = !isWatched }
                    )
                    Text(if (isWatched) "Watched" else "Watch", color = Color.White, fontSize = 13.sp)
                }
            }

            // ===== Rating Section =====
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                var rating by remember { mutableStateOf(0f) }

                // Container للنجوم مع الـ Slider
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.85f) // نخلي النجوم في 85% من العرض
                        .height(50.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                // حساب نسبة المكان المضغوط
                                val newRating = (offset.x / size.width) * 5f
                                rating = ((newRating * 2).toInt() / 2f).coerceIn(0f, 5f)
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                ) {
                    // Slider شفاف
                    Slider(
                        value = rating,
                        onValueChange = {
                            rating = ((it * 2).toInt() / 2f).coerceIn(0f, 5f)
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        valueRange = 0f..5f,
                        steps = 9, // 10 خطوات (0, 0.5, 1, 1.5, ..., 5)
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
                                rating >= i -> Icons.Rounded.Star
                                rating >= i - 0.5f -> Icons.Rounded.StarHalf
                                else -> Icons.Rounded.StarOutline
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (rating >= i - 0.5f) Color(0xFFFFD700) else Color.Gray,
                                modifier = Modifier
                                    .size(44.dp)
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                }


            }

            Spacer(modifier = Modifier.height(30.dp))

            // DONE Button
            Button(
                onClick = { showSheet = false },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
            ) {
                Text("Done", color = Color.White)
            }
        }
    }

    // ============================
    // Main Movie Details
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
                .height(380.dp)
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(Color(0xFF2A1B3D))
                .shadow(12.dp)
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movieData.poster_path}",
                contentDescription = movieData.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF2A1B3D))
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                IconButton(onClick = { showSheet = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(movieData.title, color = Color.White, fontSize = 26.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("⭐ ${movieData.vote_average}", color = Color.LightGray)
                Text(movieData.release_date ?: "N/A", color = Color.LightGray)
                Text(movieData.original_language.uppercase(), color = Color.LightGray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                movieData.overview.ifEmpty { "No description available." },
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Play Trailer Button
            Button(
                onClick = {
                    trailerKey?.let { key ->
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/watch?v=$key")
                        )
                        context.startActivity(intent)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Play Trailer", color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CAST
            Text("Cast", color = Color.White, fontSize = 20.sp)

            Spacer(modifier = Modifier.height(12.dp))

            if (castList.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(castList) { actor ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (!actor.profile_path.isNullOrEmpty()) {
                                AsyncImage(
                                    model = "https://image.tmdb.org/t/p/w200${actor.profile_path}",
                                    contentDescription = actor.name,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4A3A64))
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(actor.name, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                Text("No cast available.", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}