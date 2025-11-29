package com.example.myapplication.ui.screens.seeMore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.data.remote.MovieApiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeeMoreMoviesScreen(
    title: String,
    category: String,
    navController: NavHostController,
    viewModel: SeeMoreMoviesViewModel,
    showRank: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(category) {
        if (uiState.movies.isEmpty()) {
            viewModel.loadMovies(category)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                uiState.isLoading && uiState.movies.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF3A2C58))
                    }
                }

                uiState.error != null && uiState.movies.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                uiState.error ?: "Error loading movies",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.retry(category) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3A2C58)
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                uiState.movies.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No movies available",
                            color = Color.Gray,
                            fontSize = 18.sp
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.movies.size) { index ->
                            SeeMoreMovieCard(
                                movie = uiState.movies[index],
                                navController = navController,
                                rank = if (showRank) index + 1 else null
                            )

                            if (index >= uiState.movies.size - 6 &&
                                uiState.hasMorePages &&
                                !uiState.isLoading
                            ) {
                                LaunchedEffect(Unit) {
                                    viewModel.loadMovies(category)
                                }
                            }
                        }

                        if (uiState.isLoading && uiState.movies.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF3A2C58),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeeMoreMovieCard(
    movie: MovieApiModel,
    navController: NavHostController,
    rank: Int? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.67f)
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2A1B3D))
                .clickable { navController.navigate("details/${movie.id}") }
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (rank != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .offset(y = (-15).dp)
                    .size(35.dp)
                    .border(
                        width = 4.dp,
                        color = Color(0xFF1B1330),
                        shape = CircleShape
                    )
                    .background(
                        color = Color(0xFF9B5DE5),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}
