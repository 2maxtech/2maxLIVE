package com.twomax.live.ui.screens.series

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twomax.live.core.model.Channel
import com.twomax.live.core.model.StreamType
import com.twomax.live.core.repository.ChannelRepository
import com.twomax.live.core.repository.ProviderRepository
import com.twomax.live.data.local.db.dao.ChannelDao
import com.twomax.live.data.local.db.entity.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SeriesSortOrder { DEFAULT, A_Z, Z_A }

data class SeriesUiState(
    val groups: List<String> = emptyList(),
    val seriesList: List<Channel> = emptyList(),
    val selectedGroup: String? = null,
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val sortOrder: SeriesSortOrder = SeriesSortOrder.DEFAULT
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SeriesViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val channelDao: ChannelDao,
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeriesUiState())
    val uiState: StateFlow<SeriesUiState> = _uiState.asStateFlow()

    private val _selectedGroup = MutableStateFlow<String?>(null)
    private val _sortOrder = MutableStateFlow(SeriesSortOrder.DEFAULT)

    init {
        loadData()
    }

    private fun loadData() {
        val activeProvider = providerRepository.getAll()
            .map { providers -> providers.firstOrNull { it.isActive } ?: providers.firstOrNull() }
            .onEach { provider -> if (provider == null) _uiState.update { it.copy(isLoading = false) } }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

        viewModelScope.launch {
            activeProvider.flatMapLatest { provider ->
                if (provider == null) flowOf(emptyList())
                else channelDao.getGroupsByStreamType(provider.id, StreamType.SERIES.name)
            }.collect { groups ->
                Log.d("SeriesVM", "Groups loaded: ${groups.size}")
                val current = _selectedGroup.value ?: groups.firstOrNull()
                _uiState.update { it.copy(groups = groups, selectedGroup = current, isLoading = false) }
                if (_selectedGroup.value == null && current != null) _selectedGroup.value = current
            }
        }

        viewModelScope.launch {
            combine(
                combine(activeProvider, _selectedGroup) { provider, group -> Pair(provider, group) }
                    .flatMapLatest { (provider, group) ->
                        when {
                            provider == null -> flowOf(emptyList())
                            group == null -> channelDao.getByStreamType(provider.id, StreamType.SERIES.name)
                                .map { list -> list.map { it.toDomain() } }
                            else -> channelDao.getByGroupAndStreamType(provider.id, group, StreamType.SERIES.name)
                                .map { list -> list.map { it.toDomain() } }
                        }
                    },
                _sortOrder
            ) { series, sortOrder ->
                when (sortOrder) {
                    SeriesSortOrder.A_Z -> series.sortedBy { it.name.lowercase() }
                    SeriesSortOrder.Z_A -> series.sortedByDescending { it.name.lowercase() }
                    SeriesSortOrder.DEFAULT -> series
                }
            }.collect { series ->
                Log.d("SeriesVM", "Series loaded: ${series.size}")
                _uiState.update { it.copy(seriesList = series) }
            }
        }
    }

    fun selectGroup(group: String) {
        _selectedGroup.value = group
        _uiState.update { it.copy(selectedGroup = group) }
    }

    fun setSortOrder(order: SeriesSortOrder) {
        _sortOrder.value = order
        _uiState.update { it.copy(sortOrder = order) }
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            channelRepository.updateFavorite(channel.id, !channel.isFavorite)
        }
    }
}
