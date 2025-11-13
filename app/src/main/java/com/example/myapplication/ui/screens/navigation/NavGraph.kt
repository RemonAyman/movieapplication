package com.example.myapplication.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.LoginScreen
import com.example.myapplication.ResetPasswordScreen
import com.example.myapplication.SignUpScreen
import com.example.myapplication.data.FavoritesRepository
import com.example.myapplication.data.FavoritesScreen
import com.example.myapplication.data.FavoritesViewModel
import com.example.myapplication.data.FavoritesViewModelFactory
import com.example.myapplication.ui.details.MovieDetailsScreen
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.screens.editProfileScreen
import com.example.myapplication.ui.screens.search.SearchScreen
import com.example.myapplication.ui.screens.splash.SplashScreen
import com.example.myapplication.ui.screens.friends.FriendsScreen
import com.example.myapplication.ui.screens.friends.FriendsViewModel
import com.example.myapplication.ui.screens.friendDetail.FriendDetailScreen
import com.example.myapplication.ui.screens.friendsRequest.FriendRequestsScreen
import com.example.myapplication.ui.screens.profileMainScreen.ProfileMainScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onDestinationChanged: (String?) -> Unit = {}
) {
    val context = LocalContext.current

    val repository = FavoritesRepository(context)
    val factory = FavoritesViewModelFactory(repository)
    val favoritesViewModel: FavoritesViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = "splash", modifier = modifier) {

        // Splash Screen
        composable("splash") {
            onDestinationChanged("splash")
            SplashScreen(navController)
        }

        // Auth Screens
        composable("login") {
            onDestinationChanged("login")
            LoginScreen(navController)
        }
        composable("signup") {
            onDestinationChanged("signup")
            SignUpScreen(navController)
        }
        composable("resetPassword") {
            onDestinationChanged("resetPassword")
            ResetPasswordScreen(navController)
        }

        // Home
        composable("HomeScreen") {
            onDestinationChanged("HomeScreen")
            HomeScreen(navController, favoritesViewModel)
        }

        // Favorites
        composable("favorites") {
            onDestinationChanged("favorites")
            FavoritesScreen(navController = navController, viewModel = favoritesViewModel)
        }

        // Search
        composable("search") {
            onDestinationChanged("search")
            SearchScreen(navController)
        }

        // ProfileMainScreen
        composable("profile") {
            onDestinationChanged("profile")
            ProfileMainScreen(
                onEditProfile = { navController.navigate("profileEdit") },
                onFavoritesClick = { navController.navigate("favorites") },
                onFriendsClick = { navController.navigate("friends") },
                onRequestsClick = { navController.navigate("friendRequests") },
                onWatchlistClick = { navController.navigate("watchlist") }
            )
        }

        // Edit Profile
        composable("profileEdit") {
            onDestinationChanged("profileEdit")
            editProfileScreen(navController)
        }

        // Friends List
        composable("friends") {
            onDestinationChanged("friends")
            val friendsViewModel: FriendsViewModel = viewModel()
            FriendsScreen(
                viewModel = friendsViewModel,
                onFriendClick = { friendUid ->
                    navController.navigate("friendDetail/$friendUid")
                }
            )
        }

        // Friend Detail
        composable("friendDetail/{friendId}") { backStackEntry ->
            onDestinationChanged("friendDetail")
            val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
            val friendsViewModel: FriendsViewModel = viewModel()
            FriendDetailScreen(friendId = friendId, viewModel = friendsViewModel)
        }

        // Friend Requests
        composable("friendRequests") {
            onDestinationChanged("friendRequests")
            val friendsViewModel: FriendsViewModel = viewModel()
            FriendRequestsScreen(viewModel = friendsViewModel)
        }

        // Add Friend / Search Friend
        composable("addFriend") {
            onDestinationChanged("addFriend")
            val friendsViewModel: FriendsViewModel = viewModel()
            FriendsScreen(
                viewModel = friendsViewModel,
                onFriendClick = { friendUid ->
                    navController.navigate("friendDetail/$friendUid")
                },
                isSearchMode = true // صفحة البحث عن أصدقاء
            )
        }

        // Chats
        composable("chats") {
            onDestinationChanged("chats")
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chats Screen (placeholder)", color = Color.White, fontSize = 18.sp)
            }
        }

        // Movie Details
        composable("details/{movieId}") { backStackEntry ->
            onDestinationChanged("details/{movieId}")
            val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull()
            if (movieId != null) {
                MovieDetailsScreen(navController, movieId, favoritesViewModel)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Invalid movie ID", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}
