package com.example.myapplication.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.material3.Text
import kotlinx.coroutines.delay
import com.example.myapplication.R

@Composable
fun SplashScreen(navController: NavHostController) {

    val iconScale = remember { Animatable(0.05f) }
    val iconAlpha = remember { Animatable(0f) }

    val appName = "Movito"
    var visibleLetters by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {

        // Fade-in
        iconAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400)
        )

        // Bounce scale
        iconScale.animateTo(
            targetValue = 1.15f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
        iconScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
        )

        // Typewriter title
        for (i in 1..appName.length) {
            visibleLetters = i
            delay(200)
        }

        delay(600)

        navController.navigate("HomeScreen") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020617),
                        Color(0xFF020A2A),
                        Color(0xFF040320)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Image(
                painter = painterResource(id = R.drawable.movito_logo),
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(160.dp)
                    .scale(iconScale.value)
                    .alpha(iconAlpha.value),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = appName.take(visibleLetters),
                fontSize = 46.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFC7BCE8),
                            Color(0xFF9A58FF),
                            Color(0xFF3A1A73)
                        )
                    )
                )
            )
        }
    }
}
