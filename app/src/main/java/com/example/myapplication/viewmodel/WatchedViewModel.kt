package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repositories.WatchedRepository
import com.example.myapplication.ui.screens.watched.WatchedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WatchedViewModel(
    private val repository: WatchedRepository = WatchedRepository(),
) : ViewModel() {
    private val _watched = MutableStateFlow<List<WatchedItem>>(emptyList())
    val watched: StateFlow<List<WatchedItem>> = _watched

    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    init {
        loadWatched()
    }

    fun loadWatched(userId: String? = null) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                _watched.value = repository.getWatched(userId)
                _errorState.value = null
            } catch (e: Exception) {
                _watched.value = emptyList()
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }

        }
    }

    fun addToWatched(item: WatchedItem) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                repository.addWatched(item)
                _watched.value += item
                _errorState.value = null
            } catch (e: Exception) {
                _watched.value = emptyList()
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }

    fun removeFromWatched(movieId: String) {
        viewModelScope.launch {
            _loadingState.value = true
            try {
                repository.removeWatched(movieId)
                _watched.value = _watched.value.filter { it.movieId != movieId }
                _errorState.value = null
            } catch (e: Exception) {
                _errorState.value = e.message
            } finally {
                _loadingState.value = false
            }
        }
    }

}
