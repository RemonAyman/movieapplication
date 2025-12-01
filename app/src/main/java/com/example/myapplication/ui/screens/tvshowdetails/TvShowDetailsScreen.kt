package com.example.myapplication.ui.screens.tvshowdetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.data.remote.Cast
import com.example.myapplication.data.remote.Episode
import com.example.myapplication.data.remote.Season
import com.example.myapplication.ui.theme.MovitoBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvShowDetailsScreen(
    navController: NavHostController,
    viewModel: TvShowDetailsScreenViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSeasonsDialog by remember { mutableStateOf(false) }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF9B5DE5))
        }
        return
    }

    val tvShow = uiState.tvShow ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MovitoBackground)
    ) {
        // ======= Backdrop + Back Button =======
        item {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                Image(
                    painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/original${tvShow.backdrop_path}"),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MovitoBackground),
                                startY = 200f
                            )
                        )
                )
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        }

        // ======= Title + Rating =======
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = tvShow.name,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", tvShow.vote_average),
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${tvShow.number_of_seasons} Seasons • ${tvShow.number_of_episodes} Episodes",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // ======= Overview =======
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Overview",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tvShow.overview ?: "No overview available",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }

        // ======= Info =======
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Information",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                InfoRow("Status", tvShow.status ?: "Unknown")
                InfoRow("Type", tvShow.type ?: "Unknown")
                InfoRow("First Air Date", tvShow.first_air_date ?: "Unknown")
                if (tvShow.last_air_date != null) {
                    InfoRow("Last Air Date", tvShow.last_air_date)
                }
                InfoRow("Genres", tvShow.genres?.joinToString(", ") { it.name } ?: "Unknown")
                if (tvShow.networks?.isNotEmpty() == true) {
                    InfoRow("Networks", tvShow.networks.joinToString(", ") { it.name })
                }
            }
        }

        // ======= Seasons =======
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seasons",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showSeasonsDialog = true }) {
                        Text("View All", color = Color(0xFF9B5DE5))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(tvShow.seasons ?: emptyList()) { season ->
                    SeasonCard(season) {
                        viewModel.loadSeasonDetails(season.season_number)
                    }
                }
            }
        }

        // ======= Cast =======
        item {
            if (tvShow.credits?.cast?.isNotEmpty() == true) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cast",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        item {
            if (tvShow.credits?.cast?.isNotEmpty() == true) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(tvShow.credits.cast.take(10)) { cast ->
                        CastCard(cast) {
                            navController.navigate("actorDetails/${cast.id}")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // ======= Seasons Dialog =======
    if (showSeasonsDialog) {
        SeasonsDialog(
            seasons = tvShow.seasons ?: emptyList(),
            onDismiss = { showSeasonsDialog = false },
            onSeasonClick = { season ->
                viewModel.loadSeasonDetails(season.season_number)
                showSeasonsDialog = false
            }
        )
    }

    // ======= Selected Season Episodes =======
    uiState.selectedSeason?.let { selectedSeason ->
        EpisodesDialog(
            season = selectedSeason,
            onDismiss = { viewModel.clearSelectedSeason() }
        )
    }

    // ======= Error Snackbar =======
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error
            viewModel.clearError()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "$label:",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SeasonCard(season: Season, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
    ) {
        Image(
            painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${season.poster_path}"),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = season.name,
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${season.episode_count} Episodes",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun CastCard(cast: Cast, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
    ) {
        if (cast.profile_path != null) {
            Image(
                painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${cast.profile_path}"),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A1B3D)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cast.name.firstOrNull()?.uppercase() ?: "?",
                    color = Color(0xFF9B5DE5),
                    fontSize = 32.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = cast.name,
            color = Color.White,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SeasonsDialog(seasons: List<Season>, onDismiss: () -> Unit, onSeasonClick: (Season) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("All Seasons") },
        text = {
            LazyColumn {
                items(seasons) { season ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSeasonClick(season) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(season.name, modifier = Modifier.weight(1f))
                        Text("${season.episode_count} episodes", color = Color.Gray, fontSize = 12.sp)
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun EpisodesDialog(season: com.example.myapplication.data.remote.SeasonDetailsApiModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(season.name) },
        text = {
            LazyColumn {
                items(season.episodes ?: emptyList()) { episode ->
                    EpisodeItem(episode)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun EpisodeItem(episode: Episode) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row {
            Text("${episode.episode_number}.", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(episode.name, modifier = Modifier.weight(1f))
        }
        if (episode.overview != null) {
            Text(
                text = episode.overview,
                fontSize = 12.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
    HorizontalDivider()
}