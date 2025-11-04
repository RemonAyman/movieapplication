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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.data.remote.MovieApiService
import com.example.myapplication.data.remote.MovieApiModel
import com.example.myapplication.ui.theme.MovitoBackground
import com.google.accompanist.flowlayout.FlowRow
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavHostController) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<MovieApiModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val recentSearches = remember { mutableStateListOf("Action", "Comedy", "Drama", "Sci-Fi") }

    val scope = rememberCoroutineScope()
    val apiService = MovieApiService.create()
    val gson = remember { Gson() }

    // 🎤 Voice recognition launcher
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrEmpty()) {
                query = spokenText
                scope.launch {
                    try {
                        isLoading = true
                        val response = apiService.searchMovies(spokenText)
                        searchResults = response.results
                        if (!recentSearches.contains(spokenText))
                            recentSearches.add(0, spokenText)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isLoading = false
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MovitoBackground)
            .padding(16.dp)
    ) {
        // ======= Top Bar =======
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.navigate("home") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "Search",
                color = Color.White,
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ======= Search Bar =======
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search for movies, shows, and more", color = Color.LightGray) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
            },
            trailingIcon = {
                IconButton(onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a movie name…")
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

        // ======= Search Button =======
        Button(
            onClick = {
                if (query.isNotEmpty()) {
                    scope.launch {
                        try {
                            isLoading = true
                            val response = apiService.searchMovies(query)
                            searchResults = response.results
                            if (!recentSearches.contains(query))
                                recentSearches.add(0, query)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
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

        // ======= Results / Loading / Recent Searches =======
        when {
            isLoading -> {
                Spacer(modifier = Modifier.height(50.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF9B5DE5))
                }
            }

            searchResults.isNotEmpty() -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(searchResults) { movie ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    navController.navigate("details/${movie.id}")
                                }

                        ) {
                            Image(
                                painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movie.poster_path}"),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Text(
                                    text = movie.title ?: "Unknown",
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
                }
            }

            else -> {
                Text("Recent Searches", color = Color.White, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(mainAxisSpacing = 12.dp, crossAxisSpacing = 12.dp) {
                    recentSearches.forEach { item ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF2A1B3D), RoundedCornerShape(20.dp))
                                .clickable {
                                    query = item
                                    scope.launch {
                                        try {
                                            isLoading = true
                                            val response = apiService.searchMovies(item)
                                            searchResults = response.results
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        } finally {
                                            isLoading = false
                                        }
                                    }
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
