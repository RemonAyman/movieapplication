package com.example.myapplication.ui.screens.watched

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.appConstant.AppConstants
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchedScreen(
    onBack: () -> Unit,
    onMovieClick: (String) -> Unit,
    onTvShowClick: (String) -> Unit,
    viewModel: WatchedScreenViewModel,
    userId: String? = AppConstants.CURRENT_USER_ID

) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Watched", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2A1B3D)
                )
            )
        },
        containerColor = Color(0xFF1B1330)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF3A2C58))
                    }
                }
                uiState.watchedItems.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Your watched list is empty",
                            color = Color.Gray,
                            fontSize = 18.sp
                        )
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.watchedItems, key = { it.movieId }) { item ->
                            WatchedMovieCard(
                                item = item,
                                isFavorite = viewModel.isFavorite(item.movieId),
                                rating = viewModel.getRating(item.movieId),
                                onMovieClick = onMovieClick,
                                onTvShowClick = onTvShowClick,
                                onRemoveFromWatched = { viewModel.removeFromWatched(it) },
                                userId
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WatchedMovieCard(
    item: WatchedItem,
    isFavorite: Boolean,
    rating: Float?,
    onMovieClick: (String) -> Unit,
    onTvShowClick: (String) -> Unit,
    onRemoveFromWatched: (String) -> Unit,
    userId: String?
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.wrapContentSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1B1330))
                .combinedClickable(
                    onClick = {
                        if (item.movieId.startsWith("tv_")) {
                            val tvShowId = item.movieId.removePrefix("tv_")
                            onTvShowClick(tvShowId)
                        } else {
                            onMovieClick(item.movieId)
                        }
                    },
                    onLongClick = { if (userId== AppConstants.CURRENT_USER_ID)showMenu = true else showMenu=false }
                )
        ) {

            Image(
                painter = rememberAsyncImagePainter(item.poster),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.67f)
                    .clip(RoundedCornerShape(6.dp))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .background(Color(0xFF1B1330)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (rating != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val fullStars = floor(rating).toInt()
                        val hasHalfStar = (rating - fullStars) >= 0.5

                        repeat(fullStars) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        if (hasHalfStar) {
                            Text(
                                text = "½",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.size(12.dp))
                }

                if (isFavorite) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.size(12.dp))
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = DpOffset(0.dp, 0.dp),
            modifier = Modifier
                .zIndex(1f)
                .background(Color(0xFFF60000).copy(alpha = .95f))
                .clip(RoundedCornerShape(14.dp))
                .shadow(12.dp, RoundedCornerShape(14.dp), ambientColor = Color.Black)
        ) {
            DropdownMenuItem(
                text = { Text("Remove from Watched", color = Color.White) },
                onClick = {
                    onRemoveFromWatched(item.movieId)
                    showMenu = false
                }
            )
        }
    }
}