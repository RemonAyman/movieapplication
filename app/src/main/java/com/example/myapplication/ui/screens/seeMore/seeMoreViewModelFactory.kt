package com.example.myapplication.ui.screens.seeMore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.MoviesRepository

class SeeMoreMoviesViewModelFactory(
    private val repository: MoviesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SeeMoreMoviesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SeeMoreMoviesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}