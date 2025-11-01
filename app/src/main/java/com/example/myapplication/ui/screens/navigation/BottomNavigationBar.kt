package com.example.myapplication.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    var selectedIndex by remember { mutableStateOf(0) }

    // ✅ Replace "shows" with "favorites"
    val items = listOf("home", "favorites", "search", "chats", "profile")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Favorite,
        Icons.Default.Search,
        Icons.Default.Chat,
        Icons.Default.Person
    )

    NavigationBar(containerColor = Color(0xFF1A1A1A)) {
        items.forEachIndexed { index, route ->
            val isSelected = selectedIndex == index
            val scale by animateFloatAsState(targetValue = if (isSelected) 1.35f else 1f, label = "")

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
                        selectedIndex = index
                        navController.navigate(route)
                    },
                    modifier = Modifier.scale(scale)
                ) {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = route,
                        tint = if (isSelected) Color.White else Color.LightGray
                    )
                }
            }
        }
    }
}