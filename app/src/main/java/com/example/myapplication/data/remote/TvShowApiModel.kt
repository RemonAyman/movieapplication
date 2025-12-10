package com.example.myapplication.data.remote

data class TvShowApiModel(
    val id: Int,
    val name: String,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val first_air_date: String?,
    val vote_average: Double?,
    val vote_count: Int?,
    val popularity: Double?,
    val genre_ids: List<Int>?,
    val origin_country: List<String>?
)

// ✅ Response للبحث والقوائم
data class TvShowSearchResponse(
    val page: Int,
    val results: List<TvShowApiModel>,
    val total_pages: Int,
    val total_results: Int
)

// ✅ Response بسيط (للـ Similar TV Shows)
data class TvShowResponse(
    val results: List<TvShowApiModel>
)

data class TvShowDetailsApiModel(
    val id: Int,
    val name: String,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val first_air_date: String?,
    val last_air_date: String?,
    val vote_average: Double?,
    val vote_count: Int?,
    val popularity: Double?,
    val status: String?,
    val type: String?,
    val number_of_seasons: Int?,
    val number_of_episodes: Int?,
    val episode_run_time: List<Int>?,
    val genres: List<Genre>?,
    val networks: List<Network>?,
    val production_companies: List<ProductionCompany>?,
    val created_by: List<Creator>?,
    val seasons: List<Season>?,
    val credits: Credits?,
    val videos: VideoResponse?,
    val similar: TvShowResponse?
)

data class Season(
    val id: Int,
    val name: String,
    val overview: String?,
    val poster_path: String?,
    val season_number: Int,
    val episode_count: Int,
    val air_date: String?
)

data class SeasonDetailsApiModel(
    val id: Int,
    val name: String,
    val overview: String?,
    val poster_path: String?,
    val season_number: Int,
    val air_date: String?,
    val episodes: List<Episode>?
)

data class Episode(
    val id: Int,
    val name: String,
    val overview: String?,
    val still_path: String?,
    val episode_number: Int,
    val season_number: Int,
    val air_date: String?,
    val vote_average: Double?,
    val vote_count: Int?,
    val runtime: Int?
)

data class Network(
    val id: Int,
    val name: String,
    val logo_path: String?,
    val origin_country: String?
)

data class Creator(
    val id: Int,
    val name: String,
    val profile_path: String?,
    val credit_id: String?
)