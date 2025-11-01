package com.example.myapplication.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.R
import com.example.myapplication.data.FavoritesViewModel
import com.example.myapplication.ui.theme.MovitoBackground

data class Movie(val title: String, val poster: Int)

@Composable
fun HomeScreen(navController: NavHostController, favoritesViewModel: FavoritesViewModel) {
    val trendingMovies = listOf(
        Movie("Neon Nights", R.drawable.movito_logo),
        Movie("Cyberpunk Dreams", R.drawable.movito_logo),
        Movie("Electric Shadows", R.drawable.movito_logo),
        Movie("Neon Pulse", R.drawable.movito_logo),
        Movie("City of Lights", R.drawable.movito_logo)
    )

    val newReleases = listOf(
        Movie("Shadow Pulse", R.drawable.movito_logo),
        Movie("Digital Horizon", R.drawable.movito_logo),
        Movie("Synthwave City", R.drawable.movito_logo),
        Movie("Dream Circuit", R.drawable.movito_logo),
        Movie("Nova Dawn", R.drawable.movito_logo)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MovitoBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Movito",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        FeaturedMovieSection()
        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle("Trending")
        MovieRow(movies = trendingMovies, navController = navController)
        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle("New Releases")
        MovieRow(movies = newReleases, navController = navController)
        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun FeaturedMovieSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2A1B3D)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Featured Movie",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
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
fun MovieRow(movies: List<Movie>, navController: NavHostController) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(movies) { movie ->
            Box(
                modifier = Modifier
                    .size(140.dp, 200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF4A3A64))
                    .clickable {
                        // هنا ممكن تنقل لتفاصيل الفيلم
                        // navController.navigate("details/${movie.title}/${movie.poster}")
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(id = movie.poster),
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
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
