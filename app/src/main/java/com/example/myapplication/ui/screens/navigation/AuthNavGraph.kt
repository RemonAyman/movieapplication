package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.LoginScreen
import com.example.myapplication.SignUpScreen
import com.example.myapplication.ResetPasswordScreen
@Composable
fun AuthNavGraph(
    navController: NavHostController,
    onLoginSuccess: (Boolean) -> Unit
) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController) {
                // عند تسجيل الدخول بنجاح
                onLoginSuccess(true)
            }
        }
        composable("signup") { SignUpScreen(navController) }
        composable("resetPassword") { ResetPasswordScreen(navController) }
    }
}