package com.twomax.live.ui.screens.livetv

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twomax.live.core.model.Channel
import com.twomax.live.core.model.EpgProgram
import com.twomax.live.core.model.StreamType
import com.twomax.live.core.repository.ChannelRepository
import com.twomax.live.core.repository.ProviderRepository
import com.twomax.live.data.local.db.dao.ChannelDao
import com.twomax.live.data.local.db.dao.EpgProgramDao
import com.twomax.live.data.local.db.entity.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LiveTvSortOrder { DEFAULT, A_Z, Z_A }

data class LiveTvUiState(
    val groups: List<String> = emptyList(),
    val channels: List<Channel> = emptyList(),
    val selectedGroup: String? = null,
    val currentPrograms: Map<String, EpgProgram> = emptyMap(),
    val nextPrograms: Map<String, EpgProgram> = emptyMap(),
    val isLoading: Boolean = true,
    val sortOrder: LiveTvSortOrder = LiveTvSortOrder.DEFAULT
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LiveTvViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val channelDao: ChannelDao,
    private val epgProgramDao: EpgProgramDao,
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveTvUiState())
    val uiState: StateFlow<LiveTvUiState> = _uiState.asStateFlow()

    private val _selectedGroup = MutableStateFlow<String?>(null)
    private val _sortOrder = MutableStateFlow(LiveTvSortOrder.DEFAULT)

    init {
        loadData()
        startEpgRefresh()
    }

    private fun loadData() {
        val activeProvider = providerRepository.getAll()
            .map { providers -> providers.firstOrNull { it.isActive } ?: providers.firstOrNull() }
            .onEach { provider ->
                Log.d("LiveTvVM", "Active provider: ${provider?.id} ${provider?.name}")
                if (provider == null) _uiState.update { it.copy(isLoading = false) }
            }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

        // Load groups for sidebar
        viewModelScope.launch {
            activeProvider.flatMapLatest { provider ->
                if (provider == null) flowOf(emptyList())
                else channelDao.getGroupsByStreamType(provider.id, StreamType.LIVE.name)
            }.collect { groups ->
                Log.d("LiveTvVM", "Groups loaded: ${groups.size} -> $groups")
                val current = _selectedGroup.value ?: groups.firstOrNull()
                _uiState.update { it.copy(groups = groups, selectedGroup = current, isLoading = false) }
                if (_selectedGroup.value == null && current != null) _selectedGroup.value = current
            }
        }

        // Load channels: when group is null, load ALL channels of this type
        viewModelScope.launch {
            combine(
                combine(activeProvider, _selectedGroup) { provider, group -> Pair(provider, group) }
                    .flatMapLatest { (provider, group) ->
                        when {
                            provider == null -> flowOf(emptyList())
                            group == null -> channelDao.getByStreamType(provider.id, StreamType.LIVE.name)
                                .map { list -> list.map { it.toDomain() } }
                            else -> channelDao.getByGroupAndStreamType(provider.id, group, StreamType.LIVE.name)
                                .map { list -> list.map { it.toDomain() } }
                        }
                    },
                _sortOrder
            ) { channels, sortOrder ->
                when (sortOrder) {
                    LiveTvSortOrder.A_Z -> channels.sortedBy { it.name.lowercase() }
                    LiveTvSortOrder.Z_A -> channels.sortedByDescending { it.name.lowercase() }
                    LiveTvSortOrder.DEFAULT -> channels
                }
            }.collect { channels ->
                Log.d("LiveTvVM", "Channels loaded: ${channels.size}")
                _uiState.update { it.copy(channels = channels) }
                loadEpgForChannels(channels)
            }
        }
    }

    private fun startEpgRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                loadEpgForChannels(_uiState.value.channels)
            }
        }
    }

    private suspend fun loadEpgForChannels(channels: List<Channel>) {
        val epgIds = channels.map { it.epgChannelId }.filter { it.isNotEmpty() }
        if (epgIds.isEmpty()) return

        val now = System.currentTimeMillis()
        val currentEntities = epgProgramDao.getCurrentProgramsForChannels(epgIds, now)
        val upcomingEntities = epgProgramDao.getUpcomingProgramsForChannels(epgIds, now)

        val currentMap = currentEntities.associate { it.channelEpgId to it.toDomain() }
        // Take the first upcoming program per channel
        val nextMap = upcomingEntities
            .groupBy { it.channelEpgId }
            .mapValues { (_, programs) -> programs.first().toDomain() }

        _uiState.update { it.copy(currentPrograms = currentMap, nextPrograms = nextMap) }
    }

    fun selectGroup(group: String) {
        _selectedGroup.value = group
        _uiState.update { it.copy(selectedGroup = group) }
    }

    fun setSortOrder(order: LiveTvSortOrder) {
        _sortOrder.value = order
        _uiState.update { it.copy(sortOrder = order) }
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            channelRepository.updateFavorite(channel.id, !channel.isFavorite)
        }
    }
}
