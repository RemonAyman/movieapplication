package com.example.myapplication.ui.screens.search

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.ui.theme.MovitoBackground
import com.google.accompanist.flowlayout.FlowRow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchScreenViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initPreferences(context)
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrEmpty()) {
                viewModel.searchByVoice(spokenText)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MovitoBackground)
            .padding(16.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Search",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = uiState.query,
            onValueChange = { viewModel.updateQuery(it) },
            placeholder = { Text("Search...", color = Color.LightGray) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
            },
            trailingIcon = {
                IconButton(onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something…")
                    }
                    voiceLauncher.launch(intent)
                }) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = Color.White)
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color(0xFF2A1B3D),
                unfocusedContainerColor = Color(0xFF2A1B3D),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchCategory.values().forEach { category ->
                FilterChip(
                    selected = uiState.selectedCategory == category,
                    onClick = { viewModel.selectCategory(category) },
                    label = {
                        Text(
                            text = when (category) {
                                SearchCategory.MOVIES -> "Movies"
                                SearchCategory.TV_SHOWS -> "TV Shows"
                                SearchCategory.ACTORS -> "Actors"
                            },
                            fontSize = 14.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF9B5DE5),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF2A1B3D),
                        labelColor = Color.LightGray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.search() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B5DE5)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Search", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF9B5DE5))
                }
            }

            uiState.movieResults.isNotEmpty() || uiState.tvShowResults.isNotEmpty() || uiState.actorResults.isNotEmpty() -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Movies
                    items(uiState.movieResults) { movie ->
                        MovieResultItem(movie) {
                            navController.navigate("details/${movie.id}")
                        }
                    }

                    // TV Shows
                    items(uiState.tvShowResults) { show ->
                        TvShowResultItem(show) {
                            navController.navigate("tvShowDetails/${show.id}")
                        }
                    }

                    items(uiState.actorResults) { actor ->
                        ActorResultItem(actor) {
                            navController.navigate("actorDetails/${actor.id}")
                        }
                    }
                }
            }

            else -> {
                // Recent Searches
                if (uiState.recentSearches.isNotEmpty()) {
                    Text("Recent Searches", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(mainAxisSpacing = 12.dp, crossAxisSpacing = 12.dp) {
                        uiState.recentSearches.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF2A1B3D), RoundedCornerShape(20.dp))
                                    .clickable {
                                        viewModel.updateQuery(item)
                                        viewModel.search(item)
                                    }
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Text(item, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieResultItem(movie: com.example.myapplication.data.remote.MovieApiModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Image(
            painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movie.poster_path}"),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(
                text = movie.title,
                color = Color.White,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = movie.release_date ?: "",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun TvShowResultItem(show: com.example.myapplication.data.remote.TvShowApiModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Image(
            painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${show.poster_path}"),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(
                text = show.name,
                color = Color.White,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = show.first_air_date ?: "",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ActorResultItem(actor: com.example.myapplication.data.remote.ActorSearchResult, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        if (actor.profile_path != null) {
            Image(
                painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${actor.profile_path}"),
                contentDescription = null,
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
                    text = actor.name.firstOrNull()?.uppercase() ?: "?",
                    color = Color(0xFF9B5DE5),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(
                text = actor.name,
                color = Color.White,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = actor.known_for_department ?: "",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
    }
}