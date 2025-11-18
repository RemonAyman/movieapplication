package com.example.myapplication.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
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

            SectionTitle("Coming Soon")
            MovieRow(upcomingMovies, navController)

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle("Popular Movies")
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = Color.White) }
                }
                error != null -> Text("Error: $error", color = Color.Red, fontSize = 16.sp)
                else -> MovieRow(popularMovies, navController)
            }

            Spacer(modifier = Modifier.height(24.dp))
            if (actionMovies.isNotEmpty()) {
                SectionTitle("Action")
                MovieRow(actionMovies, navController)
            }

            Spacer(modifier = Modifier.height(24.dp))
            if (comedyMovies.isNotEmpty()) {
                SectionTitle("Comedy")
                MovieRow(comedyMovies, navController)
            }

            Spacer(modifier = Modifier.height(24.dp))
            if (romanceMovies.isNotEmpty()) {
                SectionTitle("Romance")
                MovieRow(romanceMovies, navController)
            }

            Spacer(modifier = Modifier.height(24.dp))
            if (cartoonMovies.isNotEmpty()) {
                SectionTitle("Cartoon")
                MovieRow(cartoonMovies, navController)
            }

            Spacer(modifier = Modifier.height(24.dp))
            if (animeMovies.isNotEmpty()) {
                SectionTitle("Anime")
                MovieRow(animeMovies, navController)
            }

            Spacer(modifier = Modifier.height(24.dp))
            if (arabicMovies.isNotEmpty()) {
                SectionTitle("Arabic")
                MovieRow(arabicMovies, navController)
            }

            Spacer(modifier = Modifier.height(60.dp))
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