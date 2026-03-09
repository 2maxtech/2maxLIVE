package com.twomax.live.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twomax.live.core.model.Channel
import com.twomax.live.core.model.Movie
import com.twomax.live.core.repository.ChannelRepository
import com.twomax.live.core.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val channels: List<Channel> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            channelRepository.getFavorites().collect { channels ->
                _uiState.update { it.copy(channels = channels, isLoading = false) }
            }
        }
        viewModelScope.launch {
            movieRepository.getFavorites().collect { movies ->
                _uiState.update { it.copy(movies = movies) }
            }
        }
    }

    fun removeChannelFavorite(channel: Channel) {
        viewModelScope.launch {
            channelRepository.updateFavorite(channel.id, false)
        }
    }

    fun removeMovieFavorite(movie: Movie) {
        viewModelScope.launch {
            movieRepository.updateFavorite(movie.id, false)
        }
    }
}
