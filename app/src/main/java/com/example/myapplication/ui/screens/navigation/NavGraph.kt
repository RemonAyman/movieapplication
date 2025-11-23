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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.LoginScreen
import com.example.myapplication.ResetPasswordScreen
import com.example.myapplication.SignUpScreen
import com.example.myapplication.ui.details.MovieDetailsScreen
import com.example.myapplication.ui.favorites.FavoritesScreen
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.screens.editProfileScreen
import com.example.myapplication.ui.screens.search.SearchScreen
import com.example.myapplication.ui.screens.splash.SplashScreen
import com.example.myapplication.ui.screens.friends.FriendsScreen
import com.example.myapplication.ui.screens.friends.FriendsViewModel
import com.example.myapplication.ui.screens.friendDetail.FriendDetailScreen
import com.example.myapplication.ui.screens.friendsRequest.FriendRequestsScreen
import com.example.myapplication.ui.screens.profileMainScreen.ProfileMainScreen
import com.example.myapplication.ui.screens.chats.ChatsScreen
import com.example.myapplication.ui.screens.chats.ChatDetailScreen
import com.example.myapplication.ui.screens.chats.NewGroupScreen
import com.example.myapplication.ui.screens.chats.NewPrivateChatScreen
import com.example.myapplication.ui.screens.chats.PrivateChatDetailScreen
import com.example.myapplication.ui.watchlist.WatchlistScreen
import com.example.myapplication.ui.watchlist.WatchlistViewModel
import com.example.myapplication.ui.watchlist.WatchlistViewModelFactory
import com.example.myapplication.viewmodel.FavoritesViewModel
import com.example.myapplication.viewmodel.FavoritesViewModelFactory
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onDestinationChanged: (String?) -> Unit = {},
) {
    val context = LocalContext.current

    val favoritesViewModel: FavoritesViewModel = viewModel()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    NavHost(navController = navController, startDestination = "splash", modifier = modifier) {

        // Splash
        composable("splash") {
            onDestinationChanged("splash")
            SplashScreen(navController)
        }

        // Auth
        composable("login") {
            onDestinationChanged("login")
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate("HomeScreen") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
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
        // Favorites
        composable("favorites/{userId}") { backStackEntry ->

            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            // استدعاء ViewModel بالفكتوري
            val favoritesViewModel: FavoritesViewModel = viewModel(
                factory = FavoritesViewModelFactory(userId)
            )

            onDestinationChanged("favorites")

            FavoritesScreen(
                onBack = { navController.popBackStack() },
                onMovieClick = { movieId -> navController.navigate("details/$movieId") },
                viewModel = favoritesViewModel,
                userID = userId
            )
        }


        // Search
        composable("search") {
            onDestinationChanged("search")
            SearchScreen(navController)
        }

        // Profile Main (current user)
        composable("profile") {
            onDestinationChanged("profile")
            ProfileMainScreen(
                navController = navController,
                userId = currentUserId,
                onEditProfile = { navController.navigate("profileEdit") },
                onFavoritesClick = { navController.navigate("favorites/$currentUserId") },
                onFriendsClick = { navController.navigate("friends") },
                onRequestsClick = { navController.navigate("friendRequests") },
                onWatchlistClick = { navController.navigate("watchlist/$currentUserId") }
            )
        }

        // Profile Main (other users)
        composable("profileMainScreen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProfileMainScreen(
                navController = navController,
                userId = userId,
                onEditProfile = { navController.navigate("profileEdit") },
                onFavoritesClick = { navController.navigate("favorites/$currentUserId") },
                onFriendsClick = { navController.navigate("friends") },
                onRequestsClick = { navController.navigate("friendRequests") },
                onWatchlistClick = { navController.navigate("watchlist/$currentUserId") }
            )
        }

        // Edit Profile
        composable("profileEdit") {
            onDestinationChanged("profileEdit")
            editProfileScreen(navController)
        }

        // Friends
        composable("friends") {
            onDestinationChanged("friends")
            val friendsViewModel: FriendsViewModel = viewModel()
            FriendsScreen(
                viewModel = friendsViewModel,
                onFriendClick = { friendUid -> navController.navigate("friendDetail/$friendUid") }
            )
        }

        // Friend Detail
        composable("FriendDetailScreen/{friendId}") { backStackEntry ->
            onDestinationChanged("friendDetail")
            val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
            val friendsViewModel: FriendsViewModel = viewModel()
            FriendDetailScreen(
                UserId = friendId, viewModel = friendsViewModel,
                onLikesClick = {navController.navigate("favorites/$friendId")},
                onWatchedClick = {},
                onWatchListClick = { navController.navigate("watchlist/$friendId")},
                onRatingsClick = {  },
            )
        }

        // Friend Requests
        composable("friendRequests") {
            onDestinationChanged("friendRequests")
            val friendsViewModel: FriendsViewModel = viewModel()
            FriendRequestsScreen(viewModel = friendsViewModel)
        }

        // Add Friend
        composable("addFriend") {
            onDestinationChanged("addFriend")
            val friendsViewModel: FriendsViewModel = viewModel()
            FriendsScreen(
                viewModel = friendsViewModel,
                onFriendClick = { friendUid -> navController.navigate("friendDetail/$friendUid") },
                isSearchMode = true
            )
        }

        // Chats
        composable("chats") {
            onDestinationChanged("chats")
            ChatsScreen(navController)
        }

        // Chat Details
        composable("chatDetail/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatDetailScreen(navController, chatId)
        }

        // New Group Chat
        composable("newGroup") {
            onDestinationChanged("newGroup")
            NewGroupScreen(navController)
        }

        // Private Chat
        composable("newPrivateChat") {
            onDestinationChanged("newPrivateChat")
            NewPrivateChatScreen(navController)
        }

        // Private Chat Detail
        composable("privateChatDetail/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            PrivateChatDetailScreen(chatId, navController)
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

        // Watchlist
        composable(
            route = "watchlist/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->

            val userId = backStackEntry.arguments?.getString("userId")

            val watchlistViewModel: WatchlistViewModel = viewModel(
                factory = WatchlistViewModelFactory(userId)
            )

            WatchlistScreen(
                viewModel = watchlistViewModel,
                onBack = { navController.popBackStack() },
                onMovieClick = { movieId -> navController.navigate("details/$movieId") }
            )
        }

    }
}