package com.example.myapplication.data

import com.example.myapplication.data.remote.MovieApiService
import com.example.myapplication.data.remote.MovieApiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class MoviesRepository(
    private val apiService: MovieApiService = MovieApiService.create()
) {
    // 🎬 جلب أول 100 فيلم شعبي
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

    suspend fun getTop100Movies(): List<MovieApiModel> =withContext(Dispatchers.IO){
        coroutineScope {
            try {
                val pages =(1..5).map{ page->
                    async { apiService.getTopRatedMovies(page = page).results }
                }
                val allMovies =pages.flatMap { it.await() }
                allMovies.filter { it.title.isNotEmpty() && it.poster_path !=null }
            }catch (e: Exception){
                emptyList()
            }
        }
    }

    // 🔹 جلب أول 100 فيلم قادم (Upcoming)
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
}
//جلب اعلى الافلام تقييما