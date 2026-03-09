package com.twomax.live.ui.screens.series

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twomax.live.core.model.Episode
import com.twomax.live.core.model.Series
import com.twomax.live.core.repository.SeriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeriesDetailUiState(
    val series: Series? = null,
    val episodes: List<Episode> = emptyList(),
    val seasons: List<Int> = emptyList(),
    val selectedSeason: Int = 1,
    val isLoading: Boolean = true
)

@HiltViewModel
class SeriesDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val seriesRepository: SeriesRepository
) : ViewModel() {

    private val seriesId: Long = savedStateHandle.get<Long>("seriesId") ?: 0L
    private val _uiState = MutableStateFlow(SeriesDetailUiState())
    val uiState: StateFlow<SeriesDetailUiState> = _uiState.asStateFlow()

    init {
        loadSeriesDetail()
    }

    private fun loadSeriesDetail() {
        viewModelScope.launch {
            seriesRepository.getSeriesDetail(seriesId).collect { series ->
                if (series != null) {
                    _uiState.update { it.copy(series = series) }
                }
            }
        }
        viewModelScope.launch {
            seriesRepository.getEpisodes(seriesId).collect { episodes ->
                val seasons = episodes.map { it.seasonNumber }.distinct().sorted()
                _uiState.update {
                    it.copy(
                        episodes = episodes,
                        seasons = seasons,
                        selectedSeason = seasons.firstOrNull() ?: 1,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectSeason(season: Int) {
        _uiState.update { it.copy(selectedSeason = season) }
    }
}
