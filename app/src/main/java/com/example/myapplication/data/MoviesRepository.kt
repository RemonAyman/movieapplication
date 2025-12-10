package com.example.myapplication.data

import com.example.myapplication.data.remote.MovieApiService
import com.example.myapplication.data.remote.MovieApiModel
import com.example.myapplication.data.remote.MovieSearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class MoviesRepository(
    private val apiService: MovieApiService = MovieApiService.create()
) {

    suspend fun getPopular100Movies(): List<MovieApiModel> = withContext(Dispatchers.IO) {
        coroutineScope {
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
    }
    suspend fun getArabicMoviesForHome(): List<MovieApiModel> = withContext(Dispatchers.IO) {
        coroutineScope {
            try {
                val pages = (1..5).map { page ->
                    async { apiService.getArabicMovies(page = page).results }
                }
                val allMovies = pages.flatMap { it.await() }
                allMovies.filter { it.title.isNotEmpty() && it.poster_path != null }
                    .take(100)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getPopularMovies(page: Int = 1): MovieSearchResponse = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPopularMovies(page = page)
            MovieSearchResponse(
                page = page,
                results = response.results,
                total_pages = 500,
                total_results = response.results.size * 500
            )
        } catch (e: Exception) {
            MovieSearchResponse(page = page, results = emptyList(), total_pages = 0, total_results = 0)
        }
    }

    suspend fun getTop100Movies(): List<MovieApiModel> = withContext(Dispatchers.IO) {
        coroutineScope {
            try {
                val pages = (1..5).map { page ->
                    async { apiService.getTopRatedMovies(page = page).results }
                }
                val allMovies = pages.flatMap { it.await() }
                allMovies.filter { it.title.isNotEmpty() && it.poster_path != null }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getTopRatedMovies(page: Int = 1): MovieSearchResponse = withContext(Dispatchers.IO) {
        try {
            apiService.getTopRatedMovies(page = page)
        } catch (e: Exception) {
            MovieSearchResponse(page = page, results = emptyList(), total_pages = 0, total_results = 0)
        }
    }

    suspend fun getUpcomingMovies(): List<MovieApiModel> = withContext(Dispatchers.IO) {
        coroutineScope {
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

    suspend fun getUpcomingMovies(page: Int = 1): MovieSearchResponse = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUpcomingMovies(page = page)
            MovieSearchResponse(
                page = page,
                results = response.results,
                total_pages = 500,
                total_results = response.results.size * 500
            )
        } catch (e: Exception) {
            MovieSearchResponse(page = page, results = emptyList(), total_pages = 0, total_results = 0)
        }
    }

    suspend fun getNowPlayingMovies(page: Int = 1): MovieSearchResponse = withContext(Dispatchers.IO) {
        try {
            apiService.getNowPlayingMovies(page = page)
        } catch (e: Exception) {
            MovieSearchResponse(page = page, results = emptyList(), total_pages = 0, total_results = 0)
        }
    }

    suspend fun getMoviesByGenre(genreId: Int, page: Int = 1): MovieSearchResponse = withContext(Dispatchers.IO) {
        try {
            apiService.getMoviesByGenre(genreId = genreId, page = page)
        } catch (e: Exception) {
            MovieSearchResponse(page = page, results = emptyList(), total_pages = 0, total_results = 0)
        }
    }

    suspend fun getAnimeMovies(page: Int = 1): MovieSearchResponse = withContext(Dispatchers.IO) {
        try {
            apiService.getAnimeMovies(page = page)
        } catch (e: Exception) {
            MovieSearchResponse(page = page, results = emptyList(), total_pages = 0, total_results = 0)
        }
    }

    suspend fun getArabicMovies(page: Int = 1): MovieSearchResponse = withContext(Dispatchers.IO) {
        try {
            apiService.getArabicMovies(page = page)
        } catch (e: Exception) {
            MovieSearchResponse(page = page, results = emptyList(), total_pages = 0, total_results = 0)
        }
    }
}