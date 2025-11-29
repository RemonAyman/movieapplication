package com.example.myapplication.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.data.remote.MovieApiModel
import com.example.myapplication.ui.theme.MovitoBackground
import com.example.myapplication.ui.theme.SurfaceDark

// Premium Color Palette
private val PrimaryPurple = Color(0xFF9B5DE5)
private val SecondaryPurple = Color(0xFF6C3FB5)
private val DarkPurple = Color(0xFF2A1B3D)
private val AccentOrange = Color(0xFFFF6B35)
private val AccentYellow = Color(0xFFF7B731)
private val CardBackground = Color(0xFF0F1729)
private val GlassBackground = Color(0x30FFFFFF)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeScreenViewModel,
    userId : String

) {
    val uiState by viewModel.uiState.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh() }
    )

    Scaffold(
        containerColor = MovitoBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = paddingValues.calculateBottomPadding() + 16.dp)
            ) {
                // Premium Header
                PremiumHeader()

                Spacer(modifier = Modifier.height(24.dp))

                // Hero Featured Movie
                if (uiState.popularMovies.isNotEmpty()) {
                    FeaturedMovieSection(
                        movie = uiState.popularMovies.first(),
                        navController = navController,
                        isLoading = uiState.isLoading
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                }

                // Coming Soon Section
                PremiumSectionTitle(
                    title = "Coming Soon",
                    subtitle = "Get ready for these releases",
                    onSeeMoreClick = {
                        navController.navigate("seeMore/upcoming/Coming Soon/false")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                when {
                    uiState.isLoading && uiState.upcomingMovies.isEmpty() -> ShimmerMovieRow()
                    uiState.error != null && uiState.upcomingMovies.isEmpty() -> PremiumErrorCard(
                        message = "Failed to load upcoming movies",
                        onRetry = { viewModel.loadUpcomingMovies() }
                    )

                    else -> PremiumMovieRow(
                        uiState.upcomingMovies,
                        navController,
                        uiState.isLoading
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                //From yor watchlist section
                PremiumSectionTitle(
                    title = "From Your WatchList",
                    subtitle = "Your Watchlist Just Got Exciting",
                    onSeeMoreClick = {
                        navController.navigate("watchlist/$userId")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                when {
                    uiState.isLoading && uiState.fromYourWatchListMovies.isEmpty() -> ShimmerMovieRow()
                    uiState.error != null && uiState.fromYourWatchListMovies.isEmpty() -> PremiumErrorCard(
                        message = "Failed to load watchlist movies",
                        onRetry = { viewModel.loadWatchlistMovies() }
                    )

                    else -> PremiumMovieRow(
                        uiState.fromYourWatchListMovies,
                        navController,
                        uiState.isLoading
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Trending Now Section
                PremiumSectionTitle(
                    title = "Trending Now",
                    subtitle = "What everyone's watching",
                    onSeeMoreClick = {
                        navController.navigate("seeMore/popular/Trending Now/false")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                when {
                    uiState.isLoading && uiState.popularMovies.isEmpty() -> ShimmerMovieRow()
                    uiState.error != null && uiState.popularMovies.isEmpty() -> PremiumErrorCard(
                        message = "No internet connection. Pull down to refresh.",
                        onRetry = { viewModel.loadPopularMovies() }
                    )

                    else -> PremiumMovieRow(uiState.popularMovies, navController, uiState.isLoading)
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Top Rated Section


                PremiumSectionTitle(
                    title = "Top Rated",
                    subtitle = "Top rated of all time",
                    onSeeMoreClick = {
                        navController.navigate("seeMore/topRated/Top Rated/true")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                when {
                    uiState.isLoading&&uiState.topRatedMovies.isEmpty() ->ShimmerMovieRow()
                    uiState.error != null && uiState.topRatedMovies.isEmpty() -> PremiumErrorCard(
                        message = "No internet connection. Pull down to refresh.",
                        onRetry = {viewModel.loadTopRatedMovies()}
                    )
                    else -> PremiumMovieRow(uiState.topRatedMovies,navController,uiState.isLoading,showRank = true)

                }

                Spacer(modifier = Modifier.height(40.dp))


                // Action Section
                if (uiState.actionMovies.isNotEmpty()) {
                    PremiumSectionTitle(
                        title = "Action & Adventure",
                        subtitle = "Explosive entertainment for thrill seekers",
                        onSeeMoreClick = {
                            navController.navigate("seeMore/action/Action & Adventure/false")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumMovieRow(uiState.actionMovies, navController, uiState.isLoading)
                    Spacer(modifier = Modifier.height(40.dp))
                }

                // Comedy Section
                if (uiState.comedyMovies.isNotEmpty()) {
                    PremiumSectionTitle(
                        title = "Comedy",
                        subtitle = "Laugh out loud with these comedies",
                        onSeeMoreClick = {
                            navController.navigate("seeMore/comedy/Comedy/false")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumMovieRow(uiState.comedyMovies, navController, uiState.isLoading)
                    Spacer(modifier = Modifier.height(40.dp))
                }

                // Romance Section
                if (uiState.romanceMovies.isNotEmpty()) {
                    PremiumSectionTitle(
                        title = "Romance",
                        subtitle = "Heartwarming love stories",
                        onSeeMoreClick = {
                            navController.navigate("seeMore/romance/Romance/false")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumMovieRow(uiState.romanceMovies, navController, uiState.isLoading)
                    Spacer(modifier = Modifier.height(40.dp))
                }

                // Animation Section
                if (uiState.cartoonMovies.isNotEmpty()) {
                    PremiumSectionTitle(
                        title = "Animation",
                        subtitle = "Animated masterpieces for all ages",
                        onSeeMoreClick = {
                            navController.navigate("seeMore/animation/Animation/false")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumMovieRow(uiState.cartoonMovies, navController, uiState.isLoading)
                    Spacer(modifier = Modifier.height(40.dp))
                }

                // anime section
                if (uiState.animeMovies.isNotEmpty()) {
                    PremiumSectionTitle(
                        title = "Anime",
                        subtitle = "Timeless anime adventures for everyone",
                        onSeeMoreClick = {
                            navController.navigate("seeMore/anime/Anime/false")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumMovieRow(uiState.animeMovies, navController, uiState.isLoading)
                    Spacer(modifier = Modifier.height(40.dp))
                }

                // Arabic Section
                if (uiState.arabicMovies.isNotEmpty()) {
                    PremiumSectionTitle(
                        title = "Arabic Cinema",
                        subtitle = "أفضل الأفلام العربية",
                        onSeeMoreClick = {
                            navController.navigate("seeMore/arabic/Arabic Cinema/false")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumMovieRow(uiState.arabicMovies, navController, uiState.isLoading)
                    Spacer(modifier = Modifier.height(40.dp))
                }

                Spacer(modifier = Modifier.height(60.dp))
            }

            // Pull to Refresh Indicator
            PullRefreshIndicator(
                refreshing = uiState.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = PrimaryPurple,
                contentColor = Color.White
            )
        }
    }
}

@Composable
fun PremiumHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        // Animated Background Gradient
        val infiniteTransition = rememberInfiniteTransition()
        val gradientOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1a0933),
                            Color(0xFF0d1b3d),
                            MovitoBackground
                        )
                    )
                )
        )

        // Animated circles in background
        Box(
            modifier = Modifier
                .offset(x = (100 + gradientOffset / 10).dp, y = (-50).dp)
                .size(200.dp)
                .alpha(0.1f)
                .blur(80.dp)
                .background(PrimaryPurple, shape = RoundedCornerShape(100.dp))
        )

        Box(
            modifier = Modifier
                .offset(x = (200 - gradientOffset / 15).dp, y = 100.dp)
                .size(150.dp)
                .alpha(0.08f)
                .blur(70.dp)
                .background(AccentOrange, shape = RoundedCornerShape(75.dp))
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Logo/Brand
            Text(
                text = "MOVITO",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 3.sp,
                style = MaterialTheme.typography.displayMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline with gradient
            Text(
                text = "Your Premium Entertainment Hub",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Decorative line
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                PrimaryPurple,
                                AccentOrange,
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun FeaturedMovieSection(
    movie: MovieApiModel,
    navController: NavHostController,
    isLoading: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp)
            .padding(horizontal = 16.dp)
    ) {
        // Glass morphism card + clickable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CardBackground.copy(alpha = 0.3f),
                            CardBackground.copy(alpha = 0.7f)
                        )
                    )
                )
                .clickable { navController.navigate("details/${movie.id}") }
        ) {
            // Background Poster with subtle blur
            AsyncImage(
                model = "https://image.tmdb.org/t/p/original${movie.poster_path}",
                contentDescription = movie.title,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(6.dp)
                    .alpha(0.25f),
                contentScale = ContentScale.Crop
            )

            // Main Poster Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/original${movie.poster_path}",
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Play Button Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(70.dp)
                        .background(
                            Color.White.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(35.dp)
                        )
                        .clickable { navController.navigate("details/${movie.id}") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Featured Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PrimaryPurple
                    ) {
                        Text(
                            text = "FEATURED",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }

            // Info Overlay at Bottom (Gradient)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                CardBackground.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    // Title
                    Text(
                        text = movie.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.shadow(2.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Rating + Year
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "",
                            tint = AccentYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", movie.vote_average),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.shadow(1.dp)
                        )

                        movie.release_date?.let { date ->
                            if (date.length >= 4) {
                                Text(
                                    text = " • ${date.substring(0, 4)}",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description / Overview
                    Text(
                        text = movie.overview,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 18.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.shadow(1.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Watch Now Button
                    Button(
                        onClick = { navController.navigate("details/${movie.id}") },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(46.dp)
                            .fillMaxWidth(0.65f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Watch Now",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumSectionTitle(
    title: String,
    subtitle: String,
    onSeeMoreClick: (() -> Unit)? = null // إضافي: لو مش null يبقى نعرض الزر
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // زر "See More" إذا ال callback مش null
        if (onSeeMoreClick != null) {
            Text(
                text = "See More",
                color = PrimaryPurple,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onSeeMoreClick() }
                    .padding(4.dp)
            )
        }
    }
}


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
fun PremiumMovieCard(
    movie: MovieApiModel,
    navController: NavHostController,
    rank: Int? = null
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "scale"
    )

    Column(
        modifier = Modifier
            .width(170.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Box(
            modifier = Modifier
                .width(170.dp)
                .height(250.dp)
                .shadow(12.dp, RoundedCornerShape(18.dp))
                .border(
                    width = 1.dp,
                    color = GlassBackground.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(18.dp)
                )
                .clip(RoundedCornerShape(18.dp))
                .background(CardBackground)
                .clickable {
                    isPressed = true
                    navController.navigate("details/${movie.id}")
                }
        ) {
            // Movie Poster
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top gradient
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MovitoBackground.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Rating Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.Black.copy(alpha = 0.75f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = AccentYellow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", movie.vote_average),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bottom Gradient
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
            )
        }
        // Ranking
        if (rank != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .offset(y = (-15).dp)
                    .size(35.dp)
                    .border(
                        width = 4.dp,
                        color = MovitoBackground,
                        shape = CircleShape
                    )
                    .background(
                        color = PrimaryPurple,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

       Spacer(modifier = Modifier.height(10.dp))

        // Movie Title
        Text(
            text = movie.title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 19.sp,
            letterSpacing = 0.2.sp
        )

        // Release Year
        movie.release_date?.let { date ->
            if (date.length >= 4) {
                Text(
                    text = date.substring(0, 4),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
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

@Composable
fun ShimmerMovieCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")

    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "shimmerAlpha"
    )

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -400f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shimmerOffset"
    )

    Column(modifier = Modifier.width(170.dp)) {
        Box(
            modifier = Modifier
                .width(170.dp)
                .height(250.dp)
                .shadow(12.dp, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(CardBackground)
        ) {
            // Shimmer effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = shimmerAlpha * 0.2f),
                                Color.Transparent
                            ),
                            startX = shimmerOffset - 200f,
                            endX = shimmerOffset + 200f
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Title shimmer
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(CardBackground.copy(alpha = shimmerAlpha * 0.4f))
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Year shimmer
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(CardBackground.copy(alpha = shimmerAlpha * 0.3f))
        )
    }
}

@Composable
fun PremiumErrorCard(
    message: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(20.dp),
            color = CardBackground.copy(alpha = 0.5f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                DarkPurple.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Error Icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                DarkPurple.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(30.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "⚠️",
                            fontSize = 32.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        message,
                        color = Color.White,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryPurple
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(
                            "Try Again",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}