package com.twomax.live.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twomax.live.core.model.Provider
import com.twomax.live.core.repository.ProviderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val hasProviders: Boolean = false,
    val isLoading: Boolean = true,
    val activeProvider: Provider? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val providerRepository: ProviderRepository
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
            }
        }
    }
}
