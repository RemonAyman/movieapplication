package com.example.myapplication.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.myapplication.data.MoviesRepository
import com.example.myapplication.data.WatchlistRepository
import com.example.myapplication.data.remote.MovieApiService
import com.example.myapplication.ui.screens.details.MovieDetailsScreen
import com.example.myapplication.ui.screens.favorites.FavoritesScreen
import com.example.myapplication.ui.screens.favorites.FavoritesScreenViewModel
import com.example.myapplication.ui.screens.favorites.FavoritesScreenViewModelFactory
import com.example.myapplication.ui.screens.home.HomeScreen
import com.example.myapplication.ui.screens.home.HomeScreenViewModel
import com.example.myapplication.ui.screens.home.HomeScreenViewModelFactory
import com.example.myapplication.ui.screens.editProfileScreen
import com.example.myapplication.ui.screens.search.SearchScreen
import com.example.myapplication.ui.screens.search.SearchScreenViewModel
import com.example.myapplication.ui.screens.splash.SplashScreen
import com.example.myapplication.ui.screens.friends.FriendsScreen
import com.example.myapplication.ui.screens.friends.FriendsViewModel
import com.example.myapplication.ui.screens.friendDetail.FriendDetailScreen
import com.example.myapplication.ui.screens.friendDetail.FriendDetailScreenViewModel
import com.example.myapplication.ui.screens.friendDetail.FriendDetailScreenViewModelFactory
import com.example.myapplication.ui.screens.friendsRequest.FriendRequestsScreen
import com.example.myapplication.ui.screens.friendsRequest.FriendRequestsScreenViewModel
import com.example.myapplication.ui.screens.profileMainScreen.ProfileMainScreen
import com.example.myapplication.ui.screens.profileMainScreen.ProfileScreenViewModel
import com.example.myapplication.ui.screens.profileMainScreen.ProfileScreenViewModelFactory
import com.example.myapplication.ui.screens.chats.ChatsScreen
import com.example.myapplication.ui.screens.chats.ChatDetailScreen
import com.example.myapplication.ui.screens.chats.NewGroupScreen
import com.example.myapplication.ui.screens.chats.NewPrivateChatScreen
import com.example.myapplication.ui.screens.chats.PrivateChatDetailScreen
import com.example.myapplication.ui.screens.watchlist.WatchlistScreen
import com.example.myapplication.ui.screens.watchlist.WatchlistScreenViewModel
import com.example.myapplication.ui.screens.watchlist.WatchlistScreenViewModelFactory
import com.example.myapplication.ui.screens.watched.WatchedScreen
import com.example.myapplication.ui.screens.watched.WatchedScreenViewModel
import com.example.myapplication.ui.screens.watched.WatchedScreenViewModelFactory
import com.example.myapplication.ui.screens.ratings.RatingsScreen
import com.example.myapplication.ui.screens.ratings.RatingsScreenViewModel
import com.example.myapplication.ui.screens.ratings.RatingsScreenViewModelFactory
import com.example.myapplication.ui.screens.details.MovieDetailsScreenViewModel
import com.example.myapplication.ui.screens.details.MovieDetailsScreenViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.example.myapplication.ui.screens.details.ActorDetailsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onDestinationChanged: (String?) -> Unit = {},
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val apiService = MovieApiService.create()
    val moviesRepository = MoviesRepository(apiService)

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
            val homeVM: HomeScreenViewModel = viewModel(
                factory = HomeScreenViewModelFactory(moviesRepository)
            )
            HomeScreen(navController, homeVM)
        }

        composable("favorites/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val favoritesScreenVM: FavoritesScreenViewModel = viewModel(
                factory = FavoritesScreenViewModelFactory(userId)
            )
            FavoritesScreen(
                onBack = { navController.popBackStack() },
                onMovieClick = { id -> navController.navigate("details/$id") },
                viewModel = favoritesScreenVM
            )
        }

        composable("search") {
            onDestinationChanged("search")
            val searchVM: SearchScreenViewModel = viewModel()
            SearchScreen(navController, searchVM)
        }

        composable("profile") {
            onDestinationChanged("profile")
            val profileVM: ProfileScreenViewModel = viewModel(
                factory = ProfileScreenViewModelFactory(currentUserId)
            )
            ProfileMainScreen(
                navController = navController,
                viewModel = profileVM,
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
            val profileVM: ProfileScreenViewModel = viewModel(
                factory = ProfileScreenViewModelFactory(userId)
            )
            ProfileMainScreen(
                navController = navController,
                viewModel = profileVM,
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
            val friendDetailVM: FriendDetailScreenViewModel = viewModel(
                factory = FriendDetailScreenViewModelFactory(friendId)
            )
            FriendDetailScreen(
                viewModel = friendDetailVM,
                onBack = { navController.popBackStack() },
                onLikesClick = { navController.navigate("favorites/$friendId") },
                onWatchedClick = { navController.navigate("watched/$friendId") },
                onWatchListClick = { navController.navigate("watchlist/$friendId") },
                onRatingsClick = { navController.navigate("ratings/$friendId") }
            )
        }

        composable("friendRequests") {
            onDestinationChanged("friendRequests")
            val friendRequestsVM: FriendRequestsScreenViewModel = viewModel()
            FriendRequestsScreen(
                viewModel = friendRequestsVM,
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
                val detailsVM: MovieDetailsScreenViewModel = viewModel(
                    factory = MovieDetailsScreenViewModelFactory(movieId)
                )
                MovieDetailsScreen(navController, detailsVM)
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
            val watchlistRepository = WatchlistRepository()

            val watchlistScreenVM: WatchlistScreenViewModel = viewModel(
                factory = WatchlistScreenViewModelFactory(watchlistRepository,userId)
            )
            WatchlistScreen(
                viewModel = watchlistScreenVM,
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
            val watchedScreenVM: WatchedScreenViewModel = viewModel(
                factory = WatchedScreenViewModelFactory(userId)
            )
            WatchedScreen(
                onBack = { navController.popBackStack() },
                onMovieClick = { id -> navController.navigate("details/$id") },
                viewModel = watchedScreenVM
            )
        }

        // Ratings Screen
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
            val ratingsScreenVM: RatingsScreenViewModel = viewModel(
                factory = RatingsScreenViewModelFactory(userId)
            )
            RatingsScreen(
                onBack = { navController.popBackStack() },
                onMovieClick = { id -> navController.navigate("details/$id") },
                viewModel = ratingsScreenVM
            )
        }
    }
}
