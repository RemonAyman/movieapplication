package com.example.myapplication.data

import javax.inject.Inject

class MoviesRepository @Inject constructor(
    private val apiService: MovieApiService
) {
    // ✅ دالة بتجيب أحدث 100 فيلم عن طريق دمج أول 5 صفحات (كل صفحة فيها 20 فيلم)
    suspend fun getLatest100Movies(): List<Movie> {
        val allMovies = mutableListOf<Movie>()
        for (page in 1..5) {
            val response = apiService.getLatestMovies(page = page)
            allMovies.addAll(response.results)
        }
        return allMovies
    }
}
