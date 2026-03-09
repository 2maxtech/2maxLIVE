package com.twomax.live.ui.screens.setup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.twomax.live.core.model.Provider
import com.twomax.live.core.model.ProviderType
import com.twomax.live.core.repository.ProviderRepository
import com.twomax.live.data.sync.EpgSyncWorker
import com.twomax.live.data.sync.SyncEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetupUiState(
    val setupType: SetupType = SetupType.XTREAM,
    val m3uUrl: String = "",
    val xtreamServer: String = "",
    val xtreamUsername: String = "",
    val xtreamPassword: String = "",
    val providerName: String = "",
    val epgUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val syncResult: SyncEngine.SyncResult? = null
)

enum class SetupType { M3U_URL, M3U_FILE, XTREAM }

@HiltViewModel
class SetupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val providerRepository: ProviderRepository,
    private val syncEngine: SyncEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState(
        providerName = "dream4k",
        xtreamServer = "http://1ere-serrvices.com:80",
        xtreamUsername = "zek7zyzuh5",
        xtreamPassword = "sktc8pux8l"
    ))
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun updateProviderName(name: String) { _uiState.update { it.copy(providerName = name) } }
    fun updateXtreamServer(server: String) { _uiState.update { it.copy(xtreamServer = server) } }
    fun updateXtreamUsername(username: String) { _uiState.update { it.copy(xtreamUsername = username) } }
    fun updateXtreamPassword(password: String) { _uiState.update { it.copy(xtreamPassword = password) } }

    fun showValidationError(type: SetupType) {
        _uiState.update { it.copy(error = "Please fill in Server URL, Username, and Password") }
    }

    fun addProvider() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isLoading = true, error = null, syncResult = null) }
            try {
                val provider = Provider(
                    name = state.providerName.ifEmpty { "Xtream Provider" },
                    type = ProviderType.XTREAM,
                    url = state.xtreamServer,
                    username = state.xtreamUsername,
                    password = state.xtreamPassword
                )

                val id = providerRepository.insert(provider)
                val savedProvider = provider.copy(id = id)
                val result = syncEngine.syncProvider(savedProvider)

                if (result is SyncEngine.SyncResult.Success) {
                    enqueueEpgSync()
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        syncResult = result,
                        error = if (result is SyncEngine.SyncResult.Error) result.message else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unexpected error") }
            }
        }
    }

    private fun enqueueEpgSync() {
        val request = OneTimeWorkRequestBuilder<EpgSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
