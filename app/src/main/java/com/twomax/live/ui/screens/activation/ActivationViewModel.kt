package com.twomax.live.ui.screens.activation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twomax.live.data.device.DeviceIdProvider
import com.twomax.live.data.remote.api.ActivationApi
import com.twomax.live.data.remote.dto.RegisterDeviceRequest
import com.twomax.live.data.sync.RemotePlaylistSync
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivationUiState(
    val macAddress: String = "",
    val isActivated: Boolean = false,
    val isChecking: Boolean = false,
    val error: String? = null,
    val isSyncing: Boolean = false
)

@HiltViewModel
class ActivationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceIdProvider: DeviceIdProvider,
    private val activationApi: ActivationApi,
    private val remotePlaylistSync: RemotePlaylistSync
) : ViewModel() {

    private val prefs = context.getSharedPreferences("activation", Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(ActivationUiState())
    val uiState: StateFlow<ActivationUiState> = _uiState.asStateFlow()

    init {
        val mac = deviceIdProvider.getMacAddress()
        val cachedActivated = prefs.getBoolean("is_activated", false)
        _uiState.update { it.copy(macAddress = mac, isActivated = cachedActivated) }
        Log.d("Activation", "init: mac=$mac, cachedActivated=$cachedActivated")

        if (!cachedActivated) {
            registerAndCheck()
        } else {
            viewModelScope.launch { syncRemotePlaylists() }
        }
    }

    private fun registerAndCheck() {
        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true, error = null) }
            try {
                val mac = _uiState.value.macAddress
                activationApi.registerDevice(RegisterDeviceRequest(mac))
                Log.d("Activation", "Device registered")
                checkActivationInternal()
            } catch (e: Exception) {
                Log.e("Activation", "registerAndCheck failed", e)
                _uiState.update { it.copy(isChecking = false, error = "Server error: ${e.message}") }
            }
        }
    }

    fun checkActivation() {
        viewModelScope.launch {
            checkActivationInternal()
        }
    }

    private suspend fun checkActivationInternal() {
        _uiState.update { it.copy(isChecking = true, error = null) }
        try {
            val status = activationApi.checkStatus(_uiState.value.macAddress)
            Log.d("Activation", "Status: activated=${status.activated}")
            val activated = status.activated
            if (activated) {
                prefs.edit().putBoolean("is_activated", true).apply()
            }
            _uiState.update {
                it.copy(isActivated = activated, isChecking = false)
            }
            if (activated) {
                syncRemotePlaylists()
            }
        } catch (e: Exception) {
            Log.e("Activation", "checkActivation failed", e)
            _uiState.update { it.copy(isChecking = false, error = "Check failed: ${e.message}") }
        }
    }

    fun startPolling() {
        viewModelScope.launch {
            while (!_uiState.value.isActivated) {
                delay(15_000)
                if (!_uiState.value.isChecking) {
                    checkActivationInternal()
                }
            }
        }
    }

    private suspend fun syncRemotePlaylists() {
        _uiState.update { it.copy(isSyncing = true) }
        try {
            Log.d("Activation", "Starting remote playlist sync via RemotePlaylistSync")
            remotePlaylistSync.sync()
            Log.d("Activation", "Remote playlist sync complete")
        } catch (e: Exception) {
            Log.e("Activation", "syncRemotePlaylists failed", e)
        } finally {
            _uiState.update { it.copy(isSyncing = false) }
        }
    }
}
