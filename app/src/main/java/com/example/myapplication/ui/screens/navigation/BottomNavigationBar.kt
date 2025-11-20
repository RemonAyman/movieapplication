package com.example.myapplication.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@Composable
fun BottomNavigationBar(navController: NavHostController) {

    val items = listOf(
        BottomNavItem("HomeScreen", Icons.Default.Home),
        BottomNavItem("search", Icons.Default.Search),
        BottomNavItem("chats", Icons.Default.Chat),
        BottomNavItem("addFriend", Icons.Default.PersonAdd),
        BottomNavItem("profile", Icons.Default.Person)
    )

    var currentRoute by remember { mutableStateOf(navController.currentDestination?.route ?: "") }

    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { entry ->
            currentRoute = entry.destination.route ?: ""
        }
    }

    NavigationBar(containerColor = Color(0xFF1A1A1A)) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.35f else 1f,
                label = ""
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .background(Color(0xFF9B5DE5), CircleShape)
                    )
                }

                IconButton(
                    onClick = {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            restoreState = true
                        }
                    },
                    modifier = Modifier.scale(scale)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.route,
                        tint = if (isSelected) Color.White else Color.LightGray
                    )
                }
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)