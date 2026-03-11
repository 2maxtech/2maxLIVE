package com.twomax.live.core.repository

import com.twomax.live.core.model.Episode
import com.twomax.live.core.model.Series
import kotlinx.coroutines.flow.Flow

interface SeriesRepository {
    fun getByProvider(providerId: Long): Flow<List<Series>>
    fun getByCategory(providerId: Long, categoryId: String): Flow<List<Series>>
    fun getSeriesDetail(seriesId: Long): Flow<Series?>
    fun getEpisodes(seriesId: Long): Flow<List<Episode>>
    fun getEpisodesBySeason(seriesId: Long, seasonNumber: Int): Flow<List<Episode>>
    fun getFavorites(): Flow<List<Series>>
    fun search(query: String): Flow<List<Series>>
    suspend fun updateFavorite(seriesId: Long, isFavorite: Boolean)
    suspend fun insert(series: Series): Long
    suspend fun insertAll(seriesList: List<Series>)
    suspend fun insertEpisodes(episodes: List<Episode>)
    suspend fun delete(series: Series)
    suspend fun deleteByProvider(providerId: Long)
}
