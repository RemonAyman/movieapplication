package com.example.myapplication.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.data.MoviesRepository
import com.example.myapplication.data.remote.MovieApiModel
import com.example.myapplication.data.remote.MovieApiService
import com.example.myapplication.ui.theme.MovitoBackground
import com.example.myapplication.viewmodel.FavoritesViewModel
import com.example.myapplication.viewmodel.MoviesViewModel
import com.example.myapplication.viewmodel.MoviesViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    favoritesViewModel: FavoritesViewModel
) {
    val apiService = MovieApiService.create()
    val moviesFactory = MoviesViewModelFactory(MoviesRepository(apiService))
    val moviesViewModel: MoviesViewModel = viewModel(factory = moviesFactory)

    val popularMovies by moviesViewModel.movies.collectAsState()
    val upcomingMovies by moviesViewModel.upcomingMovies.collectAsState()
    val isLoading by moviesViewModel.isLoading.collectAsState()
    val error by moviesViewModel.errorMessage.collectAsState()

    // Pull to Refresh State
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                moviesViewModel.loadPopularMovies()
                moviesViewModel.loadUpcomingMovies()
                isRefreshing = false
            }
        }
    )

    // Load movies on first launch
    LaunchedEffect(Unit) {
        moviesViewModel.loadPopularMovies()
        moviesViewModel.loadUpcomingMovies()
    }

    val allMovies = popularMovies + upcomingMovies
    val actionMovies = popularMovies.filter { it.genre_ids.contains(28) }
    val comedyMovies = popularMovies.filter { it.genre_ids.contains(35) }
    val romanceMovies = popularMovies.filter { it.genre_ids.contains(10749) }
    val cartoonMovies = popularMovies.filter { it.genre_ids.contains(16) }
    val animeMovies = popularMovies.filter { it.genre_ids.contains(16) && it.title.contains("Anime", ignoreCase = true) }
    val arabicMovies = allMovies.filter { it.original_language == "ar" }

    Scaffold(
        containerColor = MovitoBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = paddingValues.calculateBottomPadding() + 16.dp
                    )
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Coming Soon Section
                SectionTitle("Coming Soon")
                when {
                    isLoading && upcomingMovies.isEmpty() -> LoadingIndicator()
                    error != null && upcomingMovies.isEmpty() -> ErrorCard(
                        message = "Failed to load upcoming movies",
                        onRetry = {
                            scope.launch {
                                moviesViewModel.loadUpcomingMovies()
                            }
                        }
                    )
                    else -> MovieRow(upcomingMovies, navController)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Popular Movies Section
                SectionTitle("Popular Movies")
                when {
                    isLoading && popularMovies.isEmpty() -> LoadingIndicator()
                    error != null && popularMovies.isEmpty() -> ErrorCard(
                        message = "No internet connection. Pull down to refresh.",
                        onRetry = {
                            scope.launch {
                                moviesViewModel.loadPopularMovies()
                            }
                        }
                    )
                    else -> MovieRow(popularMovies, navController)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Section
                if (actionMovies.isNotEmpty()) {
                    SectionTitle("Action")
                    MovieRow(actionMovies, navController)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Comedy Section
                if (comedyMovies.isNotEmpty()) {
                    SectionTitle("Comedy")
                    MovieRow(comedyMovies, navController)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Romance Section
                if (romanceMovies.isNotEmpty()) {
                    SectionTitle("Romance")
                    MovieRow(romanceMovies, navController)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Cartoon Section
                if (cartoonMovies.isNotEmpty()) {
                    SectionTitle("Cartoon")
                    MovieRow(cartoonMovies, navController)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Anime Section
                if (animeMovies.isNotEmpty()) {
                    SectionTitle("Anime")
                    MovieRow(animeMovies, navController)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Arabic Section
                if (arabicMovies.isNotEmpty()) {
                    SectionTitle("Arabic")
                    MovieRow(arabicMovies, navController)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Spacer(modifier = Modifier.height(60.dp))
            }

            // Pull to Refresh Indicator
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color(0xFF9B5DE5),
                contentColor = Color.White
            )
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun MovieRow(
    movies: List<MovieApiModel>,
    navController: NavHostController
) {
    if (movies.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No movies available",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(movies) { movie ->
                Box(
                    modifier = Modifier
                        .size(140.dp, 200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF4A3A64))
                        .clickable { navController.navigate("details/${movie.id}") },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x55000000))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = movie.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF9B5DE5),
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
fun ErrorCard(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A1B3D)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "⚠️",
                fontSize = 48.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                message,
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9B5DE5)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Try Again", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}