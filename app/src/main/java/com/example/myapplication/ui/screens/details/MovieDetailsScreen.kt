package com.example.myapplication.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.data.FavoritesViewModel
import com.example.myapplication.data.remote.MovieApiModel
import com.example.myapplication.ui.theme.MovitoBackground

@Composable
fun MovieDetailsScreen(
    navController: NavHostController,
    movie: MovieApiModel,
    favoritesViewModel: FavoritesViewModel
) {
    var isFavorite by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MovitoBackground)
            .padding(bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFF2A1B3D))
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                contentDescription = movie.title,
                modifier = Modifier.fillMaxSize()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }
                IconButton(onClick = { /* bookmark later */ }) {
                    Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(movie.title, color = Color.White, fontSize = 24.sp)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⭐ ${movie.vote_average}", color = Color.LightGray, fontSize = 14.sp)
                Text(movie.release_date ?: "N/A", color = Color.LightGray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                movie.overview.ifEmpty { "No description available." },
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { /* TODO: Play Trailer */ },
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
                        if (isFavorite) favoritesViewModel.addToFavorites(movie)
                        else favoritesViewModel.removeFromFavorites(movie)
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

            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(5) { i ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4A3A64))
                                .clickable { }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Actor ${i + 1}", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
