package com.example.myapplication.data

import com.example.myapplication.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ✅ Data Class بتمثل كل فيلم
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String?
)

// ✅ Data Class بتمثل استجابة TMDB (اللي فيها قائمة الأفلام)
data class MovieResponse(
    val results: List<Movie>
)

// ✅ Interface للـ Retrofit (الاتصال بالـ API)
interface MovieApiService {

    // ✅ دالة بتجيب أحدث الأفلام مرتبة حسب تاريخ الإصدار
    @GET("discover/movie")
    suspend fun getLatestMovies(
        @Query("api_key") apiKey: String = BuildConfig.TMDB_API_KEY,
        @Query("language") language: String = "en-US",
        @Query("sort_by") sortBy: String = "release_date.desc",
        @Query("page") page: Int = 1
    ): MovieResponse

    companion object {
        private const val BASE_URL = "https://api.themoviedb.org/3/"

        // ✅ إنشاء Retrofit instance
        fun create(): MovieApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MovieApiService::class.java)
        }
    }
}
