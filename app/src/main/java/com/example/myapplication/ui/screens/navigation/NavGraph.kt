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
import com.example.myapplication.ui.screens.ProfileScreen
import com.example.myapplication.ui.screens.search.SearchScreen
import com.example.myapplication.ui.screens.splash.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onDestinationChanged: (String?) -> Unit = {} // ✅ callback لتتبع الشاشة الحالية
) {
    val context = LocalContext.current

    // ✅ تهيئة Repository و ViewModel
    val repository = FavoritesRepository(context)
    val factory = FavoritesViewModelFactory(repository)
    val favoritesViewModel: FavoritesViewModel = viewModel(factory = factory)

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        // ✅ شاشة البداية
        composable("splash") {
            onDestinationChanged("splash")
            SplashScreen(navController = navController)
        }

        // ✅ تسجيل الدخول
        composable("login") {
            onDestinationChanged("login")
            LoginScreen(navController = navController)
        }

        // ✅ إنشاء حساب
        composable("signup") {
            onDestinationChanged("signup")
            SignUpScreen(navController = navController)
        }

        // ✅ إعادة تعيين كلمة المرور
        composable("resetPassword") {
            onDestinationChanged("resetPassword")
            ResetPasswordScreen(navController = navController)
        }

        // ✅ الشاشة الرئيسية
        composable("HomeScreen") {
            onDestinationChanged("HomeScreen")
            HomeScreen(
                navController = navController,
                favoritesViewModel = favoritesViewModel
            )
        }

        // ✅ المفضلة
        composable("favorites") {
            onDestinationChanged("favorites")
            FavoritesScreen(
                navController = navController,
                viewModel = favoritesViewModel
            )
        }

        // ✅ البحث
        composable("search") {
            onDestinationChanged("search")
            SearchScreen(navController = navController)
        }

        // ✅ البروفايل
        composable("profile") {
            onDestinationChanged("profile")
            ProfileScreen(navController = navController)
        }

        // ✅ محادثات (Placeholder)
        composable("chats") {
            onDestinationChanged("chats")
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

        // ✅ تفاصيل الفيلم (باستخدام movieId)
        composable("details/{movieId}") { backStackEntry ->
            onDestinationChanged("details/{movieId}")
            val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull()

            if (movieId != null) {
                MovieDetailsScreen(
                    navController = navController,
                    movieId = movieId,
                    favoritesViewModel = favoritesViewModel
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Invalid movie ID",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
