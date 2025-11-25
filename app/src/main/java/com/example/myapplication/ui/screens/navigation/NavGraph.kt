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
import com.example.myapplication.ui.watched.WatchedScreen
import com.example.myapplication.ui.screens.ratings.RatingsScreen
import com.example.myapplication.viewmodel.FavoritesViewModel
import com.example.myapplication.viewmodel.FavoritesViewModelFactory
import com.example.myapplication.viewmodel.WatchedViewModel
import com.example.myapplication.viewmodel.WatchedViewModelFactory
import com.example.myapplication.viewmodel.RatingViewModel
import com.example.myapplication.viewmodel.RatingViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screens.details.ActorDetailsScreen

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

        composable("splash") {
            onDestinationChanged("splash")
            SplashScreen(navController)
        }

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

        composable("HomeScreen") {
            onDestinationChanged("HomeScreen")
            HomeScreen(navController, favoritesViewModel)
        }

        composable("favorites/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val favVM: FavoritesViewModel = viewModel(factory = FavoritesViewModelFactory(userId))
            val ratingVM: RatingViewModel = viewModel(
                factory = RatingViewModelFactory(userId)
            )
            FavoritesScreen(
                onBack = { navController.popBackStack() },
                onMovieClick = { id -> navController.navigate("details/$id") },
                viewModel = favVM,
                userID = userId,
                ratingViewModel = ratingVM
            )
        }

        composable("search") {
            onDestinationChanged("search")
            SearchScreen(navController)
        }

        composable("profile") {
            onDestinationChanged("profile")
            ProfileMainScreen(
                navController = navController,
                userId = currentUserId,
                onEditProfile = { navController.navigate("profileEdit") },
                onFavoritesClick = { navController.navigate("favorites/$currentUserId") },
                onFriendsClick = { navController.navigate("friends") },
                onRequestsClick = { navController.navigate("friendRequests") },
                onWatchlistClick = { navController.navigate("watchlist/$currentUserId") },
                onWatchedClick = { navController.navigate("watched/$currentUserId") },
                onRatingsClick = { navController.navigate("ratings/$currentUserId") }
            )
        }

        composable("profileMainScreen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            ProfileMainScreen(
                navController = navController,
                userId = userId,
                onEditProfile = { navController.navigate("profileEdit") },
                onFavoritesClick = { navController.navigate("favorites/$userId") },
                onFriendsClick = { navController.navigate("friends") },
                onRequestsClick = { navController.navigate("friendRequests") },
                onWatchlistClick = { navController.navigate("watchlist/$userId") },
                onWatchedClick = { navController.navigate("watched/$userId") },
                onRatingsClick = { navController.navigate("ratings/$userId") }
            )
        }

        composable("profileEdit") {
            onDestinationChanged("profileEdit")
            editProfileScreen(navController)
        }

        // Friends
        composable("friends") {
            onDestinationChanged("friends")
            val friendsVM: FriendsViewModel = viewModel()
            FriendsScreen(
                viewModel = friendsVM,
                navController = navController,
                onFriendClick = { uid -> navController.navigate("friendDetail/$uid") },
                isSearchMode = false,
                onBack = { navController.navigate("profile") }
            )
        }

        // Friend Detail Screen
        composable(
            route = "friendDetail/{friendId}",
            arguments = listOf(
                navArgument("friendId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            onDestinationChanged("friendDetail")
            val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
            val friendsVM: FriendsViewModel = viewModel()
            FriendDetailScreen(
                UserId = friendId,
                viewModel = friendsVM,
                onBack = { navController.popBackStack() },
                onLikesClick = { navController.navigate("favorites/$friendId") },
                onWatchedClick = { navController.navigate("watched/$friendId") },
                onWatchListClick = { navController.navigate("watchlist/$friendId") },
                onRatingsClick = { navController.navigate("ratings/$friendId") }
            )
        }

        composable("friendRequests") {
            onDestinationChanged("friendRequests")
            val friendsVM: FriendsViewModel = viewModel()
            FriendRequestsScreen(
                viewModel = friendsVM,
                navController = navController
            )
        }

        // Add Friend (search mode)
        composable("addFriend") {
            onDestinationChanged("addFriend")
            val friendsVM: FriendsViewModel = viewModel()
            FriendsScreen(
                viewModel = friendsVM,
                navController = navController,
                onFriendClick = { uid -> navController.navigate("friendDetail/$uid") },
                isSearchMode = true,
                onBack = { navController.navigate("profile") }
            )
        }

        composable("chats") {
            onDestinationChanged("chats")
            ChatsScreen(navController)
        }

        composable("chatDetail/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatDetailScreen(navController, chatId)
        }

        composable("newGroup") {
            onDestinationChanged("newGroup")
            NewGroupScreen(navController)
        }

        composable("newPrivateChat") {
            onDestinationChanged("newPrivateChat")
            NewPrivateChatScreen(navController)
        }

        composable("privateChatDetail/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            PrivateChatDetailScreen(chatId, navController)
        }

        // Movie Details Screen
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

        // Actor Details Screen
        composable(
            route = "actorDetails/{actorId}",
            arguments = listOf(
                navArgument("actorId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            onDestinationChanged("actorDetails")
            val actorId = backStackEntry.arguments?.getInt("actorId") ?: 0
            ActorDetailsScreen(
                navController = navController,
                actorId = actorId
            )
        }

        // Watchlist Screen
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
            val watchlistVM: WatchlistViewModel = viewModel(factory = WatchlistViewModelFactory(userId))
            WatchlistScreen(
                viewModel = watchlistVM,
                onBack = { navController.popBackStack() },
                onMovieClick = { id -> navController.navigate("details/$id") }
            )
        }

        // Watched Screen
        composable(
            route = "watched/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            onDestinationChanged("watched")
            val userId = backStackEntry.arguments?.getString("userId")

            val watchedVM: WatchedViewModel = viewModel(
                factory = WatchedViewModelFactory(userId)
            )
            val favoritesVM: FavoritesViewModel = viewModel(
                factory = FavoritesViewModelFactory(userId)
            )
            val ratingVM: RatingViewModel = viewModel(
                factory = RatingViewModelFactory(userId)
            )

            WatchedScreen(
                onBack = { navController.popBackStack() },
                onMovieClick = { id -> navController.navigate("details/$id") },
                watchedViewModel = watchedVM,
                favoritesViewModel = favoritesVM,
                userId = userId,
                ratingViewModel = ratingVM
            )
        }

        // ✅ Ratings Screen - NEW
        composable(
            route = "ratings/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            onDestinationChanged("ratings")
            val userId = backStackEntry.arguments?.getString("userId")

            val ratingVM: RatingViewModel = viewModel(
                factory = RatingViewModelFactory(userId)
            )

            RatingsScreen(
                onBack = { navController.popBackStack() },
                onMovieClick = { id -> navController.navigate("details/$id") },
                ratingViewModel = ratingVM,
                userId = userId,
                favoritesViewModel = favoritesViewModel
            )
        }
    }
}