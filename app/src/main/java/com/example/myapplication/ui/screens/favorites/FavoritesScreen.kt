package com.example.myapplication.data

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.data.remote.MovieApiModel // ✅ الموديل الجديد من الـ API
import com.example.myapplication.ui.theme.MovitoBackground

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    navController: NavController
) {
    var movieToDelete by remember { mutableStateOf<MovieApiModel?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MovitoBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 🔹 Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Favorites",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 🔹 Main Content
        if (viewModel.favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No Favorites Yet", color = Color.White, fontSize = 16.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.favorites) { movie ->
                    FavoriteMovieCard(
                        movie = movie,
                        onSwipe = { movieToDelete = movie },
                        onClick = {
                            navController.navigate("details/${movie.id}")
                        }
                    )
                }
            }
        }
    }

    // 🔹 Confirm Delete Dialog
    if (movieToDelete != null) {
        ConfirmDeleteDialog(
            onConfirm = {
                viewModel.removeFromFavorites(movieToDelete!!)
                movieToDelete = null
            },
            onCancel = { movieToDelete = null }
        )
    }
}

@Composable
fun FavoriteMovieCard(
    movie: MovieApiModel,
    onSwipe: () -> Unit,
    onClick: () -> Unit
) {
    var dragOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A1B3D))
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount -> dragOffset += dragAmount },
                    onDragEnd = {
                        if (dragOffset > 120 || dragOffset < -120) onSwipe()
                        dragOffset = 0f
                    }
                )
            }
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                contentDescription = movie.title,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    movie.title ?: "Unknown Title",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "⭐ ${movie.vote_average ?: "N/A"} | ${movie.release_date ?: "Unknown"}",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "Are you sure you want to delete this movie?",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5))
                    ) {
                        Text("Yes", color = Color.White)
                    }
                    OutlinedButton(onClick = onCancel) {
                        Text("Cancel", color = Color.White)
                    }
                }
            }
        }
    }
}
