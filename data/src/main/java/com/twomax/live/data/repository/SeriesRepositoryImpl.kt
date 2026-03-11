package com.twomax.live.data.repository

import com.twomax.live.core.model.Episode
import com.twomax.live.core.model.Series
import com.twomax.live.core.repository.SeriesRepository
import com.twomax.live.data.local.db.dao.EpisodeDao
import com.twomax.live.data.local.db.dao.SeriesDao
import com.twomax.live.data.local.db.entity.toDomain
import com.twomax.live.data.local.db.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SeriesRepositoryImpl @Inject constructor(
    private val seriesDao: SeriesDao,
    private val episodeDao: EpisodeDao
) : SeriesRepository {

    override fun getByProvider(providerId: Long): Flow<List<Series>> =
        seriesDao.getByProvider(providerId).map { list -> list.map { it.toDomain() } }

    override fun getByCategory(providerId: Long, categoryId: String): Flow<List<Series>> =
        seriesDao.getByCategory(providerId, categoryId).map { list -> list.map { it.toDomain() } }

    override fun getSeriesDetail(seriesId: Long): Flow<Series?> =
        seriesDao.getById(seriesId).map { it?.toDomain() }

    override fun getEpisodes(seriesId: Long): Flow<List<Episode>> =
        episodeDao.getBySeriesId(seriesId).map { list -> list.map { it.toDomain() } }

    override fun getEpisodesBySeason(seriesId: Long, seasonNumber: Int): Flow<List<Episode>> =
        episodeDao.getBySeason(seriesId, seasonNumber).map { list -> list.map { it.toDomain() } }

    override fun getFavorites(): Flow<List<Series>> =
        seriesDao.getFavorites().map { list -> list.map { it.toDomain() } }

    override fun search(query: String): Flow<List<Series>> =
        seriesDao.search(query).map { list -> list.map { it.toDomain() } }

    override suspend fun updateFavorite(seriesId: Long, isFavorite: Boolean) =
        seriesDao.updateFavorite(seriesId, isFavorite)

    override suspend fun insert(series: Series): Long {
        seriesDao.insertAll(listOf(series.toEntity()))
        return series.id
    }

    override suspend fun insertAll(seriesList: List<Series>) =
        seriesDao.insertAll(seriesList.map { it.toEntity() })

    override suspend fun insertEpisodes(episodes: List<Episode>) =
        episodeDao.insertAll(episodes.map { it.toEntity() })

    override suspend fun delete(series: Series) =
        seriesDao.deleteByProvider(series.providerId)

    override suspend fun deleteByProvider(providerId: Long) =
        seriesDao.deleteByProvider(providerId)
}
