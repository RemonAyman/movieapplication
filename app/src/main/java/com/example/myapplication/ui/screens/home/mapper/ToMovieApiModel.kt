package com.example.myapplication.ui.screens.home.mapper

import com.example.myapplication.data.remote.MovieApiModel
import com.example.myapplication.ui.watchlist.WatchlistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

suspend fun Flow<List<WatchlistItem>>.toMovieApiModel(): List<MovieApiModel> {
    val list = this.first()
    return list.map { item ->
        MovieApiModel(
            id = item.movieId.toIntOrNull() ?: 0,
            title = item.title,
            overview = "",
            poster_path = item.poster,
            backdrop_path = null,
            release_date = null,
            vote_average = 0.0,
            genre_ids = emptyList(),
            original_language = "en"
        )
    }
}
