package com.example.myapplication.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApiService {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse
}

data class MovieResponse(
    val results: List<MovieApiModel>
)
