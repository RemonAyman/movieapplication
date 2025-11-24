package com.example.myapplication.ui.screens.details

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.MovitoBackground
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActorDetailsScreen(
    navController: NavHostController,
    actorId: Int,
    viewModel: ActorDetailsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Animation states
    var profileVisible by remember { mutableStateOf(false) }
    val profileAlpha by animateFloatAsState(
        targetValue = if (profileVisible) 1f else 0f,
        animationSpec = tween(600)
    )
    val profileScale by animateFloatAsState(
        targetValue = if (profileVisible) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    // Load actor details
    LaunchedEffect(actorId) {
        viewModel.loadActorDetails(actorId)
        delay(100)
        profileVisible = true
    }

    // Loading State
    if (uiState is ActorDetailsUiState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MovitoBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = Color(0xFF9B5DE5),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Loading actor details...", color = Color.White)
            }
        }
        return
    }

    // Error State
    if (uiState is ActorDetailsUiState.Error) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MovitoBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    (uiState as ActorDetailsUiState.Error).message,
                    color = Color.Red,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.loadActorDetails(actorId) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5))
                ) {
                    Text("Retry")
                }
            }
        }
        return
    }

    // Success State
    val successState = uiState as ActorDetailsUiState.Success
    val actor = successState.actorDetails
    val movies = successState.movies

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MovitoBackground)
            .padding(bottom = 16.dp)
    ) {
        // Actor Profile Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        ) {
            // Background Image with gradient
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${actor.profile_path}",
                contentDescription = actor.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(profileAlpha)
                    .scale(profileScale)
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color(0xFF1A1A2E).copy(alpha = 0.7f),
                                Color(0xFF1A1A2E)
                            ),
                            startY = 0f,
                            endY = 1200f
                        )
                    )
            )

            // Back button
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Actor name at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    actor.name,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 38.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Department
                    actor.known_for_department?.let { dept ->
                        Surface(
                            color = Color(0xFF9B5DE5).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                dept,
                                color = Color(0xFF9B5DE5),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Age
                    Text(
                        viewModel.calculateAge(actor.birthday),
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Actor Info Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            // Personal Info
            Text(
                "Personal Info",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Birthday
            actor.birthday?.let { birthday ->
                InfoRow(label = "Birthday", value = birthday)
            }

            // Place of Birth
            actor.place_of_birth?.let { place ->
                InfoRow(label = "Place of Birth", value = place)
            }

            // Popularity
            actor.popularity?.let { pop ->
                InfoRow(label = "Popularity", value = String.format("%.1f", pop))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Biography
            if (!actor.biography.isNullOrEmpty()) {
                Text(
                    "Biography",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                var isExpanded by remember { mutableStateOf(false) }

                Text(
                    actor.biography,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 6,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )

                if (actor.biography.length > 300) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (isExpanded) "Show Less" else "Read More",
                        color = Color(0xFF9B5DE5),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Known For (Movies)
            if (movies.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Known For",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "${movies.size} Movies",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    items(movies) { movie ->
                        MovieCard(
                            movie = movie,
                            onClick = {
                                navController.navigate("details/${movie.id}")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MovieCard(
    movie: com.example.myapplication.data.remote.ActorMovie,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable { onClick() }
    ) {
        // Movie Poster
        Box {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(130.dp)
                    .height(195.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF4A3A64))
            )

            // Rating badge
            if (movie.vote_average > 0) {
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            String.format("%.1f", movie.vote_average),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Movie Title
        Text(
            movie.title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 18.sp
        )

        // Character name
        movie.character?.let { character ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "as $character",
                color = Color.Gray,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Release year
        movie.release_date?.let { date ->
            if (date.isNotEmpty()) {
                Text(
                    date.take(4),
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }
    }
}