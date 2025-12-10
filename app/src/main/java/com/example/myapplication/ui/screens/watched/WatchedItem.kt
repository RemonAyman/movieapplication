package com.example.myapplication.ui.screens.watched

data class WatchedItem(
    val movieId: String = "",
    val title: String = "",
    val poster: String = "",
    val rating: Int = 0,
    val vote_average: Int = 0,
    val duration: Int = 0
)
