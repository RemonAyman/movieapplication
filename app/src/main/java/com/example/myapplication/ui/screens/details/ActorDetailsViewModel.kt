package com.example.myapplication.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.remote.ActorDetailsApiModel
import com.example.myapplication.data.remote.ActorMovie
import com.example.myapplication.data.remote.MovieApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ActorDetailsUiState {
    object Loading : ActorDetailsUiState()
    data class Success(
        val actorDetails: ActorDetailsApiModel,
        val movies: List<ActorMovie>
    ) : ActorDetailsUiState()
    data class Error(val message: String) : ActorDetailsUiState()
}

class ActorDetailsViewModel(
    private val apiService: MovieApiService = MovieApiService.create()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ActorDetailsUiState>(ActorDetailsUiState.Loading)
    val uiState: StateFlow<ActorDetailsUiState> = _uiState.asStateFlow()

    fun loadActorDetails(actorId: Int) {
        viewModelScope.launch {
            _uiState.value = ActorDetailsUiState.Loading

            try {

                val actorDetails = apiService.getActorDetails(actorId)

                val movies = actorDetails.movie_credits?.cast
                    ?.filter { it.poster_path != null }
                    ?.sortedByDescending { it.vote_average }
                    ?.take(20)
                    ?: emptyList()

                _uiState.value = ActorDetailsUiState.Success(
                    actorDetails = actorDetails,
                    movies = movies
                )
            } catch (e: Exception) {
                _uiState.value = ActorDetailsUiState.Error(
                    message = e.message ?: "Failed to load actor details"
                )
            }
        }
    }

    fun calculateAge(birthday: String?): String {
        if (birthday.isNullOrEmpty()) return "Unknown"

        return try {
            val birthYear = birthday.take(4).toInt()
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            val age = currentYear - birthYear
            "$age years old"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}