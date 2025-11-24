package com.example.myapplication.ui.watched

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.ui.screens.watched.WatchedItem
import com.example.myapplication.viewmodel.FavoritesViewModel
import com.example.myapplication.viewmodel.WatchedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchedScreen(
    onBack: () -> Unit,
    onMovieClick: (String) -> Unit,
    watchedViewModel: WatchedViewModel = viewModel(),
    favoritesViewModel: FavoritesViewModel = viewModel(),
    userId: String? = null
) {
    val watchedItems by watchedViewModel.watched.collectAsState()
    val isLoading by watchedViewModel.loadingState.collectAsState()
    val favoriteItems by favoritesViewModel.favorites.collectAsState()

    LaunchedEffect(userId) {
        watchedViewModel.loadWatched(userId)
        favoritesViewModel.loadFavorites(userId)
    }

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
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF3A2C58))
                    }
                }
                watchedItems.isEmpty() -> {
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
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(watchedItems, key = { it.movieId }) { item ->
                            val isFavorite = favoriteItems.any { it.movieId == item.movieId }
                            WatchedMovieCard(
                                item = item,
                                isFavorite = isFavorite,
                                onMovieClick = onMovieClick,
                                onRemoveFromWatched = { watchedViewModel.removeFromWatched(it) }
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
    onMovieClick: (String) -> Unit,
    onRemoveFromWatched: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(DpOffset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.67f)
    ) {
        // Movie Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF3A2C58), Color(0xFF1B1330))
                    )
                )
                .combinedClickable(
                    onClick = { onMovieClick(item.movieId) },
                    onLongClick = {
                        showMenu = true
                    }
                )
        ) {
            Image(
                painter = rememberAsyncImagePainter(item.poster),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
            )

            // Favorite Heart Icon (Bottom Left)
            if (isFavorite) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Context Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            offset = menuOffset,
            modifier = Modifier
                .background(Color(0xFF2A1B3D))
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        "Remove from Watched",
                        color = Color.White
                    )
                },
                onClick = {
                    onRemoveFromWatched(item.movieId)
                    showMenu = false
                },
                modifier = Modifier.background(Color(0xFF2A1B3D))
            )
        }
    }
}