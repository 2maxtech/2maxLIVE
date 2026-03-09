package com.twomax.live.ui.screens.movies

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twomax.live.core.model.Movie
import com.twomax.live.core.repository.MovieRepository
import com.twomax.live.core.repository.ProviderRepository
import com.twomax.live.data.local.db.dao.CategoryDao
import com.twomax.live.data.local.db.dao.MovieDao
import com.twomax.live.data.local.db.entity.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SortOrder { DEFAULT, A_Z, Z_A, RATING_HIGH, YEAR_NEW }

data class MoviesUiState(
    val groups: List<String> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val selectedGroup: String? = null,
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DEFAULT
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val movieDao: MovieDao,
    private val categoryDao: CategoryDao,
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    // Maps category name → categoryId for lookups
    private val categoryNameToId = mutableMapOf<String, String>()
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _sortOrder = MutableStateFlow(SortOrder.DEFAULT)

    init {
        loadData()
    }

    private fun loadData() {
        val activeProvider = providerRepository.getAll()
            .map { providers -> providers.firstOrNull { it.isActive } ?: providers.firstOrNull() }
            .onEach { provider -> if (provider == null) _uiState.update { it.copy(isLoading = false) } }
            .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

        // Load category groups
        viewModelScope.launch {
            activeProvider.flatMapLatest { provider ->
                if (provider == null) flowOf(emptyList())
                else categoryDao.getByProviderAndType(provider.id, "MOVIE")
            }.collect { categories ->
                categoryNameToId.clear()
                categories.forEach { categoryNameToId[it.name] = it.categoryId }
                val groups = categories.map { it.name }
                Log.d("MoviesVM", "Groups loaded: ${groups.size}")
                val currentName = _uiState.value.selectedGroup ?: groups.firstOrNull()
                val currentId = currentName?.let { categoryNameToId[it] }
                _uiState.update { it.copy(groups = groups, selectedGroup = currentName, isLoading = false) }
                if (_selectedCategoryId.value == null && currentId != null) {
                    _selectedCategoryId.value = currentId
                }
            }
        }

        // Load movies by category, combined with sort order
        viewModelScope.launch {
            combine(
                combine(activeProvider, _selectedCategoryId) { provider, categoryId -> Pair(provider, categoryId) }
                    .flatMapLatest { (provider, categoryId) ->
                        when {
                            provider == null -> flowOf(emptyList())
                            categoryId == null -> movieDao.getByProvider(provider.id)
                                .map { list -> list.map { it.toDomain() } }
                            else -> movieDao.getByCategory(provider.id, categoryId)
                                .map { list -> list.map { it.toDomain() } }
                        }
                    },
                _sortOrder
            ) { movies, sortOrder ->
                val sorted = when (sortOrder) {
                    SortOrder.A_Z -> movies.sortedBy { it.name.lowercase() }
                    SortOrder.Z_A -> movies.sortedByDescending { it.name.lowercase() }
                    SortOrder.RATING_HIGH -> movies.sortedByDescending { it.rating }
                    SortOrder.YEAR_NEW -> movies.sortedByDescending { it.year }
                    SortOrder.DEFAULT -> movies
                }
                sorted
            }.collect { movies ->
                Log.d("MoviesVM", "Movies loaded: ${movies.size}")
                _uiState.update { it.copy(movies = movies) }
            }
        }
    }

    fun selectGroup(group: String) {
        val categoryId = categoryNameToId[group]
        _selectedCategoryId.value = categoryId
        _uiState.update { it.copy(selectedGroup = group) }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        _uiState.update { it.copy(sortOrder = order) }
    }

    fun toggleFavorite(movie: Movie) {
        viewModelScope.launch {
            movieRepository.updateFavorite(movie.id, !movie.isFavorite)
        }
    }
}
