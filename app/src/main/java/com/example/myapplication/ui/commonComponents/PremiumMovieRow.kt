package com.example.myapplication.ui.commonComponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.data.remote.MovieApiModel

@Composable
fun PremiumMovieRow(
    movies: List<MovieApiModel>,
    navController: NavHostController,
    isLoading: Boolean,
    showRank: Boolean = false
) {
    if (isLoading && movies.isEmpty()) {
        ShimmerMovieRow()
    } else if (movies.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No movies available", color = Color.Gray, fontSize = 16.sp)
                Text(
                    "Check back later",
                    color = Color.Gray.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            itemsIndexed(movies) { index, movie ->
                PremiumMovieCard(
                    movie = movie,
                    navController = navController,
                    rank = if (showRank) index + 1 else null // 🔥 هنا السحر
                )
            }
        }
    }
}

@Composable
fun ShimmerMovieRow() {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(6) {
            ShimmerMovieCard()
        }
    }
}