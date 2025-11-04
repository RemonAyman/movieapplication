data class MovieApiModel(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String?,
    val release_date: String?,
    val vote_average: Double,
    val genre_ids: List<Int> = emptyList() ,
    val original_language: String
)
