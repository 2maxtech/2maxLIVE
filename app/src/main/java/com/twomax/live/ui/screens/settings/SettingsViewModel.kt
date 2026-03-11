package com.twomax.live.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.twomax.live.data.sync.EpgSyncWorker
import com.twomax.live.data.device.DeviceIdProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SettingsUiState(
    val epgSyncIntervalHours: Int = 6,
    val epgAutoSync: Boolean = true,
    val deviceMac: String = "",
    val activationStatus: String = "Unknown"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceIdProvider: DeviceIdProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Load device MAC
        _uiState.update { it.copy(deviceMac = deviceIdProvider.getMacAddress()) }
    }

    fun updateEpgSyncInterval(hours: Int) {
        _uiState.update { it.copy(epgSyncIntervalHours = hours) }
        if (_uiState.value.epgAutoSync) {
            scheduleEpgSync(hours)
        }
    }

    fun toggleEpgAutoSync(enabled: Boolean) {
        _uiState.update { it.copy(epgAutoSync = enabled) }
        if (enabled) {
            scheduleEpgSync(_uiState.value.epgSyncIntervalHours)
        } else {
            WorkManager.getInstance(context).cancelUniqueWork("epg_sync")
        }
    }

    private fun scheduleEpgSync(intervalHours: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<EpgSyncWorker>(
            intervalHours.toLong(), TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "epg_sync",
                ExistingPeriodicWorkPolicy.UPDATE,
                syncRequest
            )
    }
}
