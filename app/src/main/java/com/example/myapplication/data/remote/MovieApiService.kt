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
    val backdrop_path: String?,
    val release_date: String?,
    val vote_average: Double,
    val genre_ids: List<Int> = emptyList(),
    val original_language: String
)

data class MovieDetailsApiModel(
    val id: Int,
    val title: String,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val release_date: String?,
    val vote_average: Double?,
    val vote_count: Int?,
    val runtime: Int?,
    val original_language: String?,
    val genres: List<Genre>?,
    val production_companies: List<ProductionCompany>?,
    val credits: Credits?,
    val videos: VideoResponse?,
    val similar: MovieResponse?
)

data class MovieSearchResponse(
    val page: Int,
    val results: List<MovieApiModel>,
    val total_pages: Int,
    val total_results: Int
)

data class MovieResponse(
    val results: List<MovieApiModel>
)


// ===== Actor Models =====
data class CastMember(
    val id: Int,
    val name: String,
    val profile_path: String?,
    val character: String? = null
)

data class Cast(
    val id: Int,
    val name: String,
    val profile_path: String?,
    val character: String?
)

data class Crew(
    val id: Int,
    val name: String,
    val profile_path: String?,
    val job: String?
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

data class ActorDetailsApiModel(
    val id: Int,
    val name: String,
    val biography: String?,
    val birthday: String?,
    val place_of_birth: String?,
    val profile_path: String?,
    val known_for_department: String?,
    val popularity: Double?,
    val movie_credits: ActorMovieCredits?,
    val tv_credits: ActorTvCredits?
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

data class ActorTvCredits(
    val cast: List<ActorTvShow>
)

data class ActorTvShow(
    val id: Int,
    val name: String,
    val poster_path: String?,
    val first_air_date: String?,
    val vote_average: Double,
    val character: String?
)

data class ActorSearchResponse(
    val results: List<ActorSearchResult>
)

data class ActorSearchResult(
    val id: Int,
    val name: String,
    val profile_path: String?,
    val known_for_department: String?
)

// ===== Shared Models =====
data class Genre(
    val id: Int,
    val name: String
)

data class ProductionCompany(
    val id: Int,
    val name: String,
    val logo_path: String?,
    val origin_country: String?
)

data class Credits(
    val cast: List<Cast>?,
    val crew: List<Crew>?
)

data class CreditsResponse(
    val cast: List<CastMember>
)

data class VideoResponse(
    val results: List<VideoResult>
)

data class VideoResult(
    val key: String,
    val name: String,
    val site: String,
    val type: String
)

// ===== API Service =====
interface MovieApiService {

    // ============ Movies APIs ============
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

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieSearchResponse

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieSearchResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieSearchResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US",
        @Query("append_to_response") appendToResponse: String = "credits,videos,similar"
    ): MovieDetailsApiModel

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

    // ============ TV Shows APIs ============
    @GET("tv/popular")
    suspend fun getPopularTvShows(
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TvShowSearchResponse

    @GET("tv/top_rated")
    suspend fun getTopRatedTvShows(
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TvShowSearchResponse

    @GET("tv/on_the_air")
    suspend fun getOnTheAirTvShows(
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TvShowSearchResponse

    @GET("search/tv")
    suspend fun searchTvShows(
        @Query("query") query: String,
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TvShowSearchResponse

    @GET("tv/{tv_id}")
    suspend fun getTvShowDetails(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US",
        @Query("append_to_response") appendToResponse: String = "credits,videos,similar"
    ): TvShowDetailsApiModel

    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getSeasonDetails(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US"
    ): SeasonDetailsApiModel

    // ============ Actor APIs ============
    @GET("search/person")
    suspend fun searchActors(
        @Query("query") query: String,
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): ActorSearchResponse

    @GET("person/{person_id}")
    suspend fun getActorDetails(
        @Path("person_id") personId: Int,
        @Query("api_key") apiKey: String = "2f13b4fd29b3109c92837f91bdc86c24",
        @Query("language") language: String = "en-US",
        @Query("append_to_response") appendToResponse: String = "movie_credits,tv_credits"
    ): ActorDetailsApiModel

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