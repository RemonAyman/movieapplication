package com.example.myapplication.ui.screens.watchlist

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
import com.example.myapplication.ui.watchlist.WatchlistItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    viewModel: WatchlistScreenViewModel,
    onBack: () -> Unit,
    onMovieClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Watchlist", color = Color.White) },
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
                uiState.watchlistItems.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Your watchlist is empty",
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
                        items(uiState.watchlistItems, key = { it.movieId }) { item ->
                            WatchlistMovieCard(
                                item = item,
                                onMovieClick = onMovieClick,
                                onRemoveFromWatchlist = { viewModel.removeFromWatchlist(it) }
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
fun WatchlistMovieCard(
    item: WatchlistItem,
    onMovieClick: (String) -> Unit,
    onRemoveFromWatchlist: (String) -> Unit
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
                    onClick = { onMovieClick(item.movieId) },
                    onLongClick = { showMenu = true }
                )
        ) {

            // Poster
            Image(
                painter = rememberAsyncImagePainter(item.poster),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.67f)
                    .clip(RoundedCornerShape(6.dp))
            )
        }

        // DropdownMenu فوق كل العناصر
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
                text = { Text("Remove from Watchlist", color = Color.White) },
                onClick = {
                    onRemoveFromWatchlist(item.movieId)
                    showMenu = false
                }
            )
        }
    }
}

