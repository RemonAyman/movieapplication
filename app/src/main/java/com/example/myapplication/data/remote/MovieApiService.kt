package com.example.myapplication.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// ===== Movie Models =====
data class MovieApiModel(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String?,
    val release_date: String?,
    val vote_average: Double,
    val genre_ids: List<Int> = emptyList(),
    val original_language: String
)

// ===== Actor Models =====
data class CastMember(
    val id: Int,  // ✅ مهم للـ navigation
    val name: String,
    val profile_path: String?,
    val character: String? = null
)

data class ActorDetails(
    val id: Int,
    val name: String,
    val biography: String?,
    val birthday: String?,
    val place_of_birth: String?,
    val profile_path: String?,
    val known_for_department: String?,
    val popularity: Double?
)

data class ActorMovieCredits(
    val cast: List<ActorMovie>
)

data class ActorMovie(
    val id: Int,
    val title: String,
    val poster_path: String?,
    val release_date: String?,
    val vote_average: Double,
    val character: String?
)

// ===== Response Models =====
data class MovieResponse(val results: List<MovieApiModel>)
data class VideoResponse(val results: List<VideoResult>)
data class VideoResult(val key: String, val name: String, val site: String, val type: String)
data class CreditsResponse(val cast: List<CastMember>)

data class MovieDetailsResponse(
    val id: Int,
    val title: String,
    val overview: String,
    val release_date: String?,
    val poster_path: String?,
    val vote_average: Double,
    val runtime: Int?,
    val original_language: String
)

// ===== API Service =====
interface MovieApiService {

    // Movies APIs
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US"
    ): MovieDetailsResponse

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US"
    ): VideoResponse

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US"
    ): CreditsResponse

    // ===== Actor APIs =====
    @GET("person/{person_id}")
    suspend fun getActorDetails(
        @Path("person_id") personId: Int,
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US"
    ): ActorDetails

    @GET("person/{person_id}/movie_credits")
    suspend fun getActorMovieCredits(
        @Path("person_id") personId: Int,
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US"
    ): ActorMovieCredits

    companion object {
        fun create(): MovieApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(MovieApiService::class.java)
        }
    }
}