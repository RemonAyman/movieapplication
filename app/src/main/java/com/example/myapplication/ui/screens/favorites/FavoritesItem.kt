package com.example.myapplication.ui.screens.favorites

data class FavoritesItem(
    val movieId: String = "",
    val title: String = "",
    val poster: String = "",
    val rating: Int = 0,
    val vote_average: Int = 0
)