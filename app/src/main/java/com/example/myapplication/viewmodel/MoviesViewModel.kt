package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.MoviesRepository
import com.example.myapplication.data.remote.MovieApiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 🎬 ViewModel الخاص بالأفلام
// مسؤول عن جلب البيانات (أفلام شهيرة + قادمة) من Repository والتحكم في الحالة
class MoviesViewModel(
    private val repository: MoviesRepository
) : ViewModel() {

    // 🔹 قائمة الأفلام الشهيرة
    private val _movies = MutableStateFlow<List<MovieApiModel>>(emptyList())
    val movies: StateFlow<List<MovieApiModel>> = _movies

    // 🔹 قائمة الأفلام القادمة (Coming Soon)
    private val _upcomingMovies = MutableStateFlow<List<MovieApiModel>>(emptyList())
    val upcomingMovies: StateFlow<List<MovieApiModel>> = _upcomingMovies

    // 🔹 حالة التحميل
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 🔹 رسالة الخطأ لو حصلت مشكلة
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // 🔹 تحميل الأفلام الشهيرة
    fun loadPopularMovies() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                val moviesList = repository.getPopular100Movies()
                _movies.value = moviesList
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔹 تحميل الأفلام القادمة
    fun loadUpcomingMovies() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                val upcomingList = repository.getUpcomingMovies()
                _upcomingMovies.value = upcomingList
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
