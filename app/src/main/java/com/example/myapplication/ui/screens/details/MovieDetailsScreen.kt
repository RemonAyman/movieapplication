package com.example.myapplication.ui.details

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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.data.FavoritesViewModel
import com.example.myapplication.data.remote.*
import com.example.myapplication.ui.theme.MovitoBackground
import com.example.myapplication.ui.watchlist.WatchlistItem
import com.example.myapplication.ui.watchlist.WatchlistViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun MovieDetailsScreen(
    navController: NavHostController,
    movieId: Int,
    favoritesViewModel: FavoritesViewModel
) {
    val apiService = remember { MovieApiService.create() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val watchlistViewModel: WatchlistViewModel = viewModel()
    var isInWatchlist by remember { mutableStateOf(false) }

    var movie by remember { mutableStateOf<MovieApiModel?>(null) }
    var trailerKey by remember { mutableStateOf<String?>(null) }
    var castList by remember { mutableStateOf<List<CastMember>>(emptyList()) }
    var isFavorite by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // ===== Load movie details =====
    LaunchedEffect(movieId) {
        scope.launch {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MovitoBackground)
            .padding(bottom = 16.dp)
    ) {
        // ===== Movie Poster =====
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
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                }

                IconButton(
                    onClick = {
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
                ) {
                    Icon(
                        if (isInWatchlist) Icons.Filled.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ===== Movie Details =====
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(movieData.title, color = Color.White, fontSize = 26.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("⭐ ${movieData.vote_average}", color = Color.LightGray, fontSize = 14.sp)
                Text(movieData.release_date ?: "N/A", color = Color.LightGray, fontSize = 14.sp)
                Text(movieData.original_language.uppercase(), color = Color.LightGray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                movieData.overview.ifEmpty { "No description available." },
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        trailerKey?.let { key ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$key"))
                            context.startActivity(intent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play Trailer", color = Color.White)
                }

                Button(
                    onClick = {
                        isFavorite = !isFavorite
                        if (isFavorite) favoritesViewModel.addToFavorites(movieData)
                        else favoritesViewModel.removeFromFavorites(movieData)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFavorite) Color(0xFF9B5DE5) else Color(0xFF2A1B3D)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) Color(0xFFFF5C8D) else Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isFavorite) "Added" else "Favorite", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
                            Text(actor.name, color = Color.White, fontSize = 14.sp, maxLines = 1)
                        }
                    }
                }
            } else {
                Text("No cast available.", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
