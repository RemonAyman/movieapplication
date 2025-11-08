package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.ui.navigation.BottomNavigationBar
import com.example.myapplication.ui.navigation.NavGraph
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }

                // ✅ شاشة البداية (Splash)
                LaunchedEffect(Unit) {
                    delay(2000)
                    isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
                }

                when (isLoggedIn) {
                    null -> {
                        // ✅ Splash Screen
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF1A1A1A)), // خلفية ثابتة بديلة لـ MovitoBackground
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎬 Movito",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White
                            )
                        }
                    }

                    true -> {
                        val currentBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = currentBackStackEntry?.destination?.route

                        // ✅ الشاشات اللي يظهر فيها الـ Bottom Bar
                        val showBottomBar = currentDestination in listOf(
                            "HomeScreen",
                            "search",
                            "favorites",
                            "profile",
                            "chats"
                        )


                        Scaffold(
                            bottomBar = {
                                if (showBottomBar) BottomNavigationBar(navController)
                            }
                        ) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                NavGraph(navController = navController)
                            }
                        }
                    }

                    false -> {
                        // المستخدم مش عامل تسجيل دخول → يروح على صفحة اللوجين
                        NavGraph(navController = navController)
                    }
                }
            }
        }
    }

    // ================= MODELS =================
    data class MovieResponse(val results: List<Movie>)
    data class Movie(
        val id: Int,
        val title: String,
        val poster_path: String?,
        val overview: String,
        val vote_average: Double
    )

    data class VideoResponse(val results: List<Video>)
    data class Video(val key: String, val name: String, val site: String, val type: String)

    data class CreditsResponse(val cast: List<Cast>)
    data class Cast(val name: String, val character: String, val profile_path: String?)

    // ================= API =================
    interface MovieApi {
        @GET("movie/now_playing")
        suspend fun getLatestMovies(
            @Query("api_key") apiKey: String,
            @Query("language") language: String = "en-US",
            @Query("page") page: Int = 1
        ): MovieResponse

        @GET("search/movie")
        suspend fun searchMovies(
            @Query("api_key") apiKey: String,
            @Query("query") query: String
        ): MovieResponse

        @GET("movie/{movie_id}")
        suspend fun getMovieDetails(
            @Path("movie_id") movieId: Int,
            @Query("api_key") apiKey: String
        ): Movie

        @GET("movie/{movie_id}/videos")
        suspend fun getMovieVideos(
            @Path("movie_id") movieId: Int,
            @Query("api_key") apiKey: String
        ): VideoResponse

        @GET("movie/{movie_id}/credits")
        suspend fun getMovieCredits(
            @Path("movie_id") movieId: Int,
            @Query("api_key") apiKey: String
        ): CreditsResponse
    }

    // ================= RETROFIT =================
    object RetrofitClient {
        private val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        private val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        private val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api: MovieApi = retrofit.create(MovieApi::class.java)
    }

    // ================= MOVIE LIST SCREEN =================
    @Composable
    fun MovieListScreen(navController: androidx.navigation.NavHostController) {
        var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var query by remember { mutableStateOf("") }
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        // تحميل الأفلام أول ما الشاشة تفتح
        LaunchedEffect(Unit) {
            scope.launch {
                try {
                    val response = RetrofitClient.api.getLatestMovies(BuildConfig.TMDB_API_KEY, page = 1)
                    movies = response.results.take(100)
                } catch (e: Exception) {
                    errorMessage = e.message
                }
            }
        }

        val speechLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val spokenText = result.data
                    ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?.firstOrNull()
                if (!spokenText.isNullOrEmpty()) {
                    query = spokenText
                    scope.launch {
                        try {
                            val response =
                                RetrofitClient.api.searchMovies(BuildConfig.TMDB_API_KEY, spokenText)
                            movies = response.results
                            errorMessage = null
                        } catch (e: Exception) {
                            errorMessage = e.message
                            movies = emptyList()
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search for a movie...") },
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = {
                    Toast.makeText(context, "Listening...", Toast.LENGTH_SHORT).show()
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                    }
                    speechLauncher.launch(intent)
                }) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Search")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        try {
                            val response =
                                RetrofitClient.api.searchMovies(BuildConfig.TMDB_API_KEY, query)
                            movies = response.results
                            errorMessage = null
                        } catch (e: Exception) {
                            errorMessage = e.message
                            movies = emptyList()
                        }
                    }
                },
                enabled = query.isNotEmpty(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Search")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error loading movies 😢")
                }

                movies.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No movies available 😕")
                }

                else -> LazyColumn(contentPadding = PaddingValues(8.dp)) {
                    items(movies) { movie ->
                        MovieCard(movie) { navController.navigate("details/${movie.id}") }
                    }
                }
            }
        }
    }

    @Composable
    fun MovieCard(movie: Movie, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { onClick() },
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val imageUrl = movie.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = movie.title,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(movie.title, fontWeight = FontWeight.Bold)
                    Text("⭐ ${movie.vote_average}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    // ================= MOVIE DETAILS SCREEN =================
    @Composable
    fun MovieDetailScreen(navController: androidx.navigation.NavHostController, movieId: Int) {
        var movie by remember { mutableStateOf<Movie?>(null) }
        var trailerKey by remember { mutableStateOf<String?>(null) }
        var castList by remember { mutableStateOf<List<Cast>>(emptyList()) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        LaunchedEffect(movieId) {
            scope.launch {
                try {
                    movie = RetrofitClient.api.getMovieDetails(movieId, BuildConfig.TMDB_API_KEY)
                    val videos = RetrofitClient.api.getMovieVideos(movieId, BuildConfig.TMDB_API_KEY).results
                    val credits = RetrofitClient.api.getMovieCredits(movieId, BuildConfig.TMDB_API_KEY).cast

                    trailerKey = videos.firstOrNull { it.site == "YouTube" && it.type == "Trailer" }?.key
                    castList = credits.take(10)
                } catch (e: Exception) {
                    errorMessage = e.message
                }
            }
        }

        when {
            errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error loading movie details 😢")
            }

            movie == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            else -> {
                val m = movie!!
                val posterUrl = m.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        if (posterUrl != null) {
                            Image(
                                painter = rememberAsyncImagePainter(posterUrl),
                                contentDescription = m.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(400.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(m.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("⭐ Rating: ${m.vote_average}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(m.overview, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(20.dp))

                        if (trailerKey != null) {
                            Button(onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.youtube.com/watch?v=$trailerKey")
                                )
                                context.startActivity(intent)
                            }) {
                                Text("🎥 Watch Trailer on YouTube")
                            }
                        } else Text("No trailer available 😕")

                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Cast:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        LazyRow {
                            items(castList) { cast ->
                                Column(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val profileUrl =
                                        cast.profile_path?.let { "https://image.tmdb.org/t/p/w200$it" }
                                    Image(
                                        painter = rememberAsyncImagePainter(profileUrl),
                                        contentDescription = cast.name,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .padding(4.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(cast.name, fontWeight = FontWeight.Bold)
                                    Text(cast.character, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
