package com.twomax.live.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twomax.live.core.model.Channel
import com.twomax.live.core.model.Movie
import com.twomax.live.core.model.Series
import com.twomax.live.core.repository.ChannelRepository
import com.twomax.live.core.repository.MovieRepository
import com.twomax.live.core.repository.SeriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val channels: List<Channel> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val series: List<Series> = emptyList(),
    val isSearching: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val movieRepository: MovieRepository,
    private val seriesRepository: SeriesRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        val debouncedQuery = _query
            .debounce(300)
            .distinctUntilChanged()
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

        combine(
            debouncedQuery.flatMapLatest { q ->
                if (q.isBlank()) flowOf(emptyList()) else channelRepository.search(q)
            },
            debouncedQuery.flatMapLatest { q ->
                if (q.isBlank()) flowOf(emptyList()) else movieRepository.search(q)
            },
            debouncedQuery.flatMapLatest { q ->
                if (q.isBlank()) flowOf(emptyList()) else seriesRepository.search(q)
            }
        ) { channels, movies, series ->
            Triple(channels, movies, series)
        }.onEach { (channels, movies, series) ->
            _uiState.update { it.copy(channels = channels, movies = movies, series = series, isSearching = false) }
        }.launchIn(viewModelScope)
    }

    fun onQueryChange(q: String) {
        _query.value = q
        _uiState.update { it.copy(query = q, isSearching = q.isNotBlank()) }
    }
}
