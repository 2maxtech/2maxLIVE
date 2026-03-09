package com.twomax.live.ui.screens.epg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twomax.live.core.model.Channel
import com.twomax.live.core.model.EpgProgram
import com.twomax.live.core.model.StreamType
import com.twomax.live.core.repository.ChannelRepository
import com.twomax.live.core.repository.EpgRepository
import com.twomax.live.core.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EpgUiState(
    val channels: List<Channel> = emptyList(),
    val programs: Map<String, List<EpgProgram>> = emptyMap(),
    val currentTimeMillis: Long = System.currentTimeMillis(),
    val startTimeMillis: Long = System.currentTimeMillis() - 3600000, // 1h ago
    val endTimeMillis: Long = System.currentTimeMillis() + 7200000,   // 2h ahead
    val isLoading: Boolean = true
)

@HiltViewModel
class EpgViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val epgRepository: EpgRepository,
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EpgUiState())
    val uiState: StateFlow<EpgUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            providerRepository.getAll().collect { providers ->
                val active = providers.firstOrNull { it.isActive } ?: providers.firstOrNull()
                if (active != null) {
                    channelRepository.getByProvider(active.id).collect { allChannels ->
                        val liveChannels = allChannels.filter { it.streamType == StreamType.LIVE && it.epgChannelId.isNotEmpty() }
                        _uiState.update { it.copy(channels = liveChannels, isLoading = false) }
                        loadPrograms(liveChannels)
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun loadPrograms(channels: List<Channel>) {
        val state = _uiState.value
        viewModelScope.launch {
            val programsMap = mutableMapOf<String, List<EpgProgram>>()
            channels.forEach { channel ->
                epgRepository.getProgramsForChannel(channel.epgChannelId, state.startTimeMillis, state.endTimeMillis)
                    .firstOrNull()?.let { programs ->
                        programsMap[channel.epgChannelId] = programs
                    }
            }
            _uiState.update { it.copy(programs = programsMap) }
        }
    }

    fun shiftTime(hoursOffset: Int) {
        val offset = hoursOffset * 3600000L
        _uiState.update {
            it.copy(
                startTimeMillis = it.startTimeMillis + offset,
                endTimeMillis = it.endTimeMillis + offset
            )
        }
        loadPrograms(_uiState.value.channels)
    }
}
