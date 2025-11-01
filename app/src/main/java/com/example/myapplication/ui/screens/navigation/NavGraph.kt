package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.data.FavoritesScreen
import com.example.myapplication.ui.screens.search.SearchScreen
import com.example.myapplication.ui.details.MovieDetailsScreen
import com.example.myapplication.data.FavoritesViewModel
import com.example.myapplication.data.remote.MovieApiModel

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    val favoritesViewModel: FavoritesViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {

        // ✅ Home Screen
        composable("home") {
            HomeScreen(
                navController = navController,
                favoritesViewModel = favoritesViewModel
            )
        }

        // ✅ Favorites Screen
        composable("favorites") {
            FavoritesScreen(
                navController = navController,
                viewModel = favoritesViewModel
            )
        }

        // ✅ Search Screen
        composable("search") {
            SearchScreen(navController = navController)
        }

        // ✅ Chats Screen (Placeholder for now)
        composable("chats") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Chats Screen (placeholder)",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }

        // ✅ Profile Screen (Placeholder)
        composable("profile") {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Profile Screen",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }

        // ✅ Movie Details Screen
        composable("details/{movieId}") { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull() ?: 0

            val movie: MovieApiModel? =
                favoritesViewModel.favorites.find { it.id == movieId }

            if (movie != null) {
                MovieDetailsScreen(
                    navController = navController,
                    movie = movie,
                    favoritesViewModel = favoritesViewModel
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Movie not found",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
