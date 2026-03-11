package com.twomax.live.ui.screens.movies

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twomax.live.core.model.Movie
import com.twomax.live.core.repository.ProviderRepository
import com.twomax.live.data.local.db.dao.MovieDao
import com.twomax.live.data.local.db.entity.toDomain
import com.twomax.live.data.remote.api.OmdbApi
import com.twomax.live.data.remote.api.XtreamApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MovieDetailUiState(
    val movie: Movie? = null,
    val plot: String = "",
    val director: String = "",
    val cast: String = "",
    val genre: String = "",
    val duration: String = "",
    val year: String = "",
    val country: String = "",
    val coverBig: String = "",
    val imdbRating: String = "",
    val rtRating: String = "",
    val rating5based: Float = 0f,
    val isLoading: Boolean = true
)

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val movieDao: MovieDao,
    private val xtreamApi: XtreamApi,
    private val omdbApi: OmdbApi,
    private val providerRepository: ProviderRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val movieId: Long = savedStateHandle.get<Long>("movieId") ?: 0L
    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    init {
        loadMovieDetail()
    }

    private fun loadMovieDetail() {
        viewModelScope.launch {
            // Load movie from DB
            val movie = movieDao.getById(movieId).firstOrNull() ?: run {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            val domainMovie = movie.toDomain()
            _uiState.update {
                it.copy(
                    movie = domainMovie,
                    plot = domainMovie.plot,
                    genre = domainMovie.genre,
                    duration = domainMovie.duration,
                    year = domainMovie.year,
                    rating5based = domainMovie.rating,
                    isLoading = false
                )
            }

            // Get active provider for Xtream API calls
            val provider = providerRepository.getAll()
                .map { providers -> providers.firstOrNull { it.isActive } ?: providers.firstOrNull() }
                .firstOrNull() ?: return@launch

            // Fetch VOD info from Xtream API
            if (provider.url.isNotEmpty() && domainMovie.vodId.isNotEmpty()) {
                try {
                    val apiUrl = "${provider.url}/player_api.php"
                    val vodInfo = xtreamApi.getVodInfo(
                        url = apiUrl,
                        username = provider.username,
                        password = provider.password,
                        action = "get_vod_info",
                        vodId = domainMovie.vodId
                    )
                    val detail = vodInfo.info
                    if (detail != null) {
                        _uiState.update { state ->
                            state.copy(
                                plot = detail.plot ?: detail.description ?: state.plot,
                                director = detail.director ?: "",
                                cast = detail.cast ?: detail.actors ?: "",
                                genre = detail.genre ?: state.genre,
                                duration = detail.duration ?: state.duration,
                                year = detail.releaseDate?.take(4) ?: state.year,
                                country = detail.country ?: "",
                                coverBig = detail.coverBig ?: detail.movieImage ?: "",
                                rating5based = detail.rating5based ?: state.rating5based
                            )
                        }
                    }
                } catch (_: Exception) {
                    // Xtream VOD info may not be available for all providers
                }
            }

            // Fetch OMDB ratings
            val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            val omdbKey = prefs.getString("omdb_api_key", "") ?: ""
            if (omdbKey.isNotEmpty()) {
                try {
                    val yearParam = _uiState.value.year.takeIf { it.isNotEmpty() }
                    val omdbResponse = omdbApi.searchByTitle(
                        apiKey = omdbKey,
                        title = domainMovie.name,
                        year = yearParam
                    )
                    if (omdbResponse.response == "True") {
                        val rtRating = omdbResponse.ratings
                            ?.firstOrNull { it.source == "Rotten Tomatoes" }
                            ?.value ?: ""
                        _uiState.update { state ->
                            state.copy(
                                imdbRating = omdbResponse.imdbRating ?: "",
                                rtRating = rtRating,
                                // Fill in missing fields from OMDB
                                plot = state.plot.ifEmpty { omdbResponse.plot ?: "" },
                                director = state.director.ifEmpty { omdbResponse.director ?: "" },
                                cast = state.cast.ifEmpty { omdbResponse.actors ?: "" },
                                genre = state.genre.ifEmpty { omdbResponse.genre ?: "" },
                                duration = state.duration.ifEmpty { omdbResponse.runtime ?: "" }
                            )
                        }
                    }
                } catch (_: Exception) {
                    // OMDB API may fail or key may be invalid
                }
            }
        }
    }
}
