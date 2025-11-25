package com.example.myapplication.ui.screens.ratings

data class RatingItem(
    val movieId: String = "",
    val title: String = "",
    val poster: String = "",
    val rating: Float = 0f,
    val review: String = "",
    val ratedDate: Long = System.currentTimeMillis(),
    val vote_average: Double = 0.0
)