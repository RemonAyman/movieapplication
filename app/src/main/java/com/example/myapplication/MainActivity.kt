package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.example.myapplication.ui.navigation.AuthNavGraph
import com.example.myapplication.ui.navigation.BottomNavigationBar
import com.example.myapplication.ui.navigation.NavGraph
import com.example.myapplication.ui.theme.MovitoBackground
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    // ✅ حفظ الـ NavController للاستخدام في onNewIntent
    private var mainNavController: NavHostController? = null

    // ✅ Request Permission Launcher للإشعارات
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "✅ Notification permission granted")
            updateFCMToken()
        } else {
            Log.w(TAG, "⚠️ Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // ⭐ Splash Screen - لازم يكون قبل super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // ✅ طلب إذن الإشعارات (Android 13+)
        requestNotificationPermission()

        // ✅ تحديث FCM Token عند فتح التطبيق
        updateFCMToken()

        setContent {
            // ⭐ قراءة isLoggedIn في background thread
            var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }

            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    val prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    isLoggedIn = prefs.getBoolean("isLoggedIn", false)
                }
            }

            // ✅ إنشاء NavController وحفظه
            val navController = rememberNavController()

            // ✅ حفظ NavController للاستخدام في onNewIntent
            DisposableEffect(navController) {
                mainNavController = navController
                onDispose {
                    mainNavController = null
                }
            }

            // ✅ معالجة الإشعارات عند فتح التطبيق
            LaunchedEffect(Unit) {
                handleNotificationIntent(intent, navController)
            }

            // انتظار حتى يتم تحميل الحالة
            if (isLoggedIn == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MovitoBackground),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
                return@setContent
            }

            MaterialTheme {
                if (isLoggedIn == true) {
                    val currentBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = currentBackStackEntry?.destination?.route

                    val showBottomBar = remember(currentDestination) {
                        currentDestination in listOf(
                            "HomeScreen",
                            "search",
                            "favorites",
                            "profile",
                            "chats",
                            "addFriend"
                        )
                    }

                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) BottomNavigationBar(navController)
                        }
                    ) { innerPadding ->
                        StatusBarBackground(MovitoBackground)

                        Box(modifier = Modifier.padding(innerPadding)) {
                            NavGraph(navController = navController)
                        }
                    }
                } else {
                    StatusBarBackground(MovitoBackground)
                    AuthNavGraph(navController = navController) { loginSuccess ->
                        if (loginSuccess) {
                            // ⭐ حفظ في background thread
                            kotlinx.coroutines.MainScope().launch {
                                withContext(Dispatchers.IO) {
                                    getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                                        .edit().putBoolean("isLoggedIn", true).apply()
                                }
                                isLoggedIn = true

                                // ✅ تحديث FCM Token بعد تسجيل الدخول
                                updateFCMToken()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * ✅ طلب إذن الإشعارات (Android 13+)
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "✅ Notification permission already granted")
                }
                else -> {
                    Log.d(TAG, "📱 Requesting notification permission...")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d(TAG, "📱 Android < 13, no permission needed")
        }
    }

    /**
     * ✅ تحديث FCM Token في Firestore
     */
    private fun updateFCMToken() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Log.w(TAG, "⚠️ No user logged in, skipping FCM token update")
            return
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "✅ FCM Token obtained: ${token.take(20)}...")

                // ✅ حفظ الـ Token في Firestore
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUserId)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ FCM Token saved to Firestore successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ Failed to save FCM Token to Firestore", e)

                        // ✅ إذا فشل التحديث، نحاول إنشاء الحقل
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(currentUserId)
                            .set(
                                hashMapOf("fcmToken" to token),
                                com.google.firebase.firestore.SetOptions.merge()
                            )
                            .addOnSuccessListener {
                                Log.d(TAG, "✅ FCM Token created in Firestore")
                            }
                            .addOnFailureListener { e2 ->
                                Log.e(TAG, "❌ Failed to create FCM Token field", e2)
                            }
                    }
            } else {
                Log.e(TAG, "❌ Failed to get FCM Token", task.exception)
            }
        }
    }

    /**
     * ✅ معالجة الـ Intent عند الضغط على الإشعار
     */
    private fun handleNotificationIntent(
        intent: Intent,
        navController: NavHostController
    ) {
        val openChat = intent.getBooleanExtra("openChat", false)
        if (openChat) {
            val chatId = intent.getStringExtra("chatId")
            val isGroup = intent.getBooleanExtra("isGroup", false)

            if (!chatId.isNullOrEmpty()) {
                Log.d(TAG, "📩 Opening chat from notification: $chatId (isGroup: $isGroup)")

                // الانتقال إلى الشات المناسب
                val route = if (isGroup) {
                    "chatDetail/$chatId"
                } else {
                    "privateChatDetail/$chatId"
                }

                // إزالة الـ Intent Extras لتجنب فتح الشات مرة أخرى
                intent.removeExtra("openChat")
                intent.removeExtra("chatId")
                intent.removeExtra("isGroup")

                // الانتقال إلى الشات
                navController.navigate(route) {
                    popUpTo("HomeScreen") { inclusive = false }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // ✅ معالجة الإشعارات عند فتح التطبيق من notification وهو شغال
        mainNavController?.let { navController ->
            handleNotificationIntent(intent, navController)
        }
    }
}

@Composable
fun StatusBarBackground(color: Color) {
    val height = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(color)
    )
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

// ================= RETROFIT (محسّن) =================
object RetrofitClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: MovieApi = retrofit.create(MovieApi::class.java)
}

// ================= MOVIE LIST SCREEN (محسّن) =================
@Composable
fun MovieListScreen(navController: androidx.navigation.NavHostController) {
    var movies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // تحميل الأفلام مرة واحدة فقط
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.api.getLatestMovies(BuildConfig.TMDB_API_KEY, page = 1)
            }
            movies = response.results.take(100)
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
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
                    isLoading = true
                    try {
                        val response = withContext(Dispatchers.IO) {
                            RetrofitClient.api.searchMovies(BuildConfig.TMDB_API_KEY, spokenText)
                        }
                        movies = response.results
                        errorMessage = null
                    } catch (e: Exception) {
                        errorMessage = e.message
                        movies = emptyList()
                    } finally {
                        isLoading = false
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
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            )

            IconButton(
                onClick = {
                    Toast.makeText(context, "Listening...", Toast.LENGTH_SHORT).show()
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                    }
                    speechLauncher.launch(intent)
                },
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voice Search")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        val response = withContext(Dispatchers.IO) {
                            RetrofitClient.api.searchMovies(BuildConfig.TMDB_API_KEY, query)
                        }
                        movies = response.results
                        errorMessage = null
                    } catch (e: Exception) {
                        errorMessage = e.message
                        movies = emptyList()
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = query.isNotEmpty() && !isLoading,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(if (isLoading) "Searching..." else "Search")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error loading movies")
            }

            movies.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No movies available")
            }

            else -> LazyColumn(contentPadding = PaddingValues(8.dp)) {
                items(movies, key = { it.id }) { movie ->
                    MovieCard(movie) { navController.navigate("details/${movie.id}") }
                }
            }
        }
    }
}

@Composable
fun MovieCard(movie: Movie, onClick: () -> Unit) {
    val context = LocalContext.current

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
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .scale(Scale.FILL)
                        .build()
                ),
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

// ================= MOVIE DETAILS SCREEN (محسّن) =================
@Composable
fun MovieDetailScreen(navController: androidx.navigation.NavHostController, movieId: Int) {
    var movie by remember { mutableStateOf<Movie?>(null) }
    var trailerKey by remember { mutableStateOf<String?>(null) }
    var castList by remember { mutableStateOf<List<Cast>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(movieId) {
        isLoading = true
        try {
            val movieData = withContext(Dispatchers.IO) {
                RetrofitClient.api.getMovieDetails(movieId, BuildConfig.TMDB_API_KEY)
            }
            movie = movieData

            val videos = withContext(Dispatchers.IO) {
                RetrofitClient.api.getMovieVideos(movieId, BuildConfig.TMDB_API_KEY).results
            }

            val credits = withContext(Dispatchers.IO) {
                RetrofitClient.api.getMovieCredits(movieId, BuildConfig.TMDB_API_KEY).cast
            }

            trailerKey = videos.firstOrNull { it.site == "YouTube" && it.type == "Trailer" }?.key
            castList = credits.take(10)
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error loading movie details")
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
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(context)
                                    .data(posterUrl)
                                    .crossfade(true)
                                    .scale(Scale.FILL)
                                    .build()
                            ),
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
                    } else Text("No trailer available")

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
                                    painter = rememberAsyncImagePainter(
                                        ImageRequest.Builder(context)
                                            .data(profileUrl)
                                            .crossfade(true)
                                            .scale(Scale.FILL)
                                            .build()
                                    ),
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