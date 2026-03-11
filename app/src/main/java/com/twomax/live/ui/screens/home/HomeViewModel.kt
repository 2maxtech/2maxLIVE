package com.twomax.live.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twomax.live.core.model.Movie
import com.twomax.live.core.model.Provider
import com.twomax.live.core.model.Series
import com.twomax.live.core.repository.MovieRepository
import com.twomax.live.core.repository.ProviderRepository
import com.twomax.live.core.repository.SeriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val hasProviders: Boolean = false,
    val isLoading: Boolean = true,
    val activeProvider: Provider? = null,
    val movies: List<Movie> = emptyList(),
    val series: List<Series> = emptyList(),
    val favoriteMovies: List<Movie> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            providerRepository.getAll().collect { providers ->
                val active = providers.firstOrNull { it.isActive } ?: providers.firstOrNull()
                _uiState.update {
                    it.copy(
                        activeProvider = active,
                        hasProviders = providers.isNotEmpty(),
                        isLoading = false
                    )
                }
                if (active != null) {
                    loadContent(active.id)
                }
            }
        }
    }

    private fun loadContent(providerId: Long) {
        viewModelScope.launch {
            movieRepository.getByProvider(providerId)
                .map { it.shuffled().take(20) }
                .collect { movies ->
                    _uiState.update { it.copy(movies = movies) }
                }
        }
        viewModelScope.launch {
            seriesRepository.getByProvider(providerId)
                .map { it.shuffled().take(20) }
                .collect { series ->
                    _uiState.update { it.copy(series = series) }
                }
        }
        viewModelScope.launch {
            movieRepository.getFavorites()
                .map { it.take(20) }
                .collect { favorites ->
                    _uiState.update { it.copy(favoriteMovies = favorites) }
                }
        }
    }
}
