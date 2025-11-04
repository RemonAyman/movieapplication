package com.example.myapplication.data

import com.example.myapplication.data.remote.MovieApiService
import com.example.myapplication.data.remote.MovieApiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MoviesRepository(
    private val apiService: MovieApiService
) {
    // 🎬 جلب أول 100 فيلم شعبي
    suspend fun getPopular100Movies(): List<MovieApiModel> = coroutineScope {
        try {
            val pages = (1..5).map { page ->
                async { apiService.getPopularMovies(page = page).results }
            }

            val allMovies = pages.flatMap { it.await() }

            allMovies.filter { it.title.isNotEmpty() && it.poster_path != null }
                .take(100)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 🔹 جلب أول 100 فيلم قادم (Upcoming)
    suspend fun getUpcomingMovies(): List<MovieApiModel> = coroutineScope {
        try {
            val pages = (1..5).map { page ->
                async { apiService.getUpcomingMovies(page = page).results }
            }

            val allMovies = pages.flatMap { it.await() }

            allMovies.filter { it.title.isNotEmpty() && it.poster_path != null }
                .take(100)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
