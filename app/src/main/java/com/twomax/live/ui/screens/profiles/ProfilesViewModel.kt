package com.twomax.live.ui.screens.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twomax.live.core.model.Provider
import com.twomax.live.core.repository.ProviderRepository
import com.twomax.live.data.sync.SyncEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfilesUiState(
    val providers: List<Provider> = emptyList(),
    val isSyncing: Boolean = false,
    val syncingProviderId: Long? = null,
    val syncResult: SyncEngine.SyncResult? = null
)

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val syncEngine: SyncEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfilesUiState())
    val uiState: StateFlow<ProfilesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            providerRepository.getAll().collect { providers ->
                _uiState.update { it.copy(providers = providers) }
            }
        }
    }

    fun delete(provider: Provider) {
        viewModelScope.launch {
            providerRepository.delete(provider)
        }
    }

    fun setActive(provider: Provider) {
        viewModelScope.launch {
            // Deactivate all other providers, activate this one
            _uiState.value.providers.forEach { p ->
                if (p.isActive && p.id != provider.id) {
                    providerRepository.update(p.copy(isActive = false))
                }
            }
            providerRepository.update(provider.copy(isActive = true))
        }
    }

    fun sync(provider: Provider) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncingProviderId = provider.id, syncResult = null) }
            val result = syncEngine.syncProvider(provider)
            _uiState.update { it.copy(isSyncing = false, syncingProviderId = null, syncResult = result) }
        }
    }

    fun clearSyncResult() {
        _uiState.update { it.copy(syncResult = null) }
    }
}
