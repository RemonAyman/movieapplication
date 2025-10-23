package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.ui.screens.ProfileScreen
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

                NavHost(
                    navController = navController,
                    startDestination = if (isLoggedIn) "movies" else "login"
                ) {
                    composable("login") { LoginScreen(navController) }
                    composable("signup") { SignUpScreen(navController) }
                    composable("movies") { MovieListScreenWithProfile(navController) }

                    composable(
                        "details/{movieId}",
                        arguments = listOf(navArgument("movieId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
                        MovieDetailScreen(navController, movieId)
                    }

                    composable("profile") {
                        ProfileScreen(navToLogin = {
                            navController.navigate("login") {
                                popUpTo("movies") { inclusive = true }
                            }
                        })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreenWithProfile(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movies") },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            MovieListScreen(navController)
        }
    }
}

// ========== Models ==========
data class MovieResponse(val results: List<Movie>)
data class Movie(
    val id: Int,
    val title: String,
    val poster_path: String?,
    val overview: String,
    val vote_average: Double
)

data class VideoResponse(val results: List<Video>)
data class Video(
    val key: String,
    val name: String,
    val site: String,
    val type: String
)

data class CreditsResponse(val cast: List<Cast>)
data class Cast(
    val name: String,
    val character: String,
    val profile_path: String?
)

// ========== API Interface ==========
interface MovieApi {
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

// ========== Retrofit ==========
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

// ========== UI ==========
@Composable
fun MovieListScreen(navController: NavHostController) {
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search for a movie...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        val response = RetrofitClient.api.searchMovies(BuildConfig.API_KEY, query)
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

            movies.isEmpty() && query.isNotEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No movies found 😕")
            }

            movies.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Type a movie name to search 🎬")
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

// ========== Details ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(navController: NavHostController, movieId: Int) {
    var movie by remember { mutableStateOf<Movie?>(null) }
    var trailerKey by remember { mutableStateOf<String?>(null) }
    var castList by remember { mutableStateOf<List<Cast>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(movieId) {
        scope.launch {
            try {
                movie = RetrofitClient.api.getMovieDetails(movieId, BuildConfig.API_KEY)
                val videos = RetrofitClient.api.getMovieVideos(movieId, BuildConfig.API_KEY).results
                val credits = RetrofitClient.api.getMovieCredits(movieId, BuildConfig.API_KEY).cast
                trailerKey = videos.firstOrNull { it.site == "YouTube" && it.type == "Trailer" }?.key
                castList = credits.take(10) // نعرض أول 10 ممثلين
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
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$trailerKey"))
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
                                val profileUrl = cast.profile_path?.let { "https://image.tmdb.org/t/p/w200$it" }
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
