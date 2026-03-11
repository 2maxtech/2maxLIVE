package com.twomax.live.data.repository

import com.twomax.live.core.model.Movie
import com.twomax.live.core.repository.MovieRepository
import com.twomax.live.data.local.db.dao.MovieDao
import com.twomax.live.data.local.db.entity.toDomain
import com.twomax.live.data.local.db.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val movieDao: MovieDao
) : MovieRepository {

    override fun getByProvider(providerId: Long): Flow<List<Movie>> =
        movieDao.getByProvider(providerId).map { list -> list.map { it.toDomain() } }

    override fun getByCategory(providerId: Long, categoryId: String): Flow<List<Movie>> =
        movieDao.getByCategory(providerId, categoryId).map { list -> list.map { it.toDomain() } }

    override fun getFavorites(): Flow<List<Movie>> =
        movieDao.getFavorites().map { list -> list.map { it.toDomain() } }

    override fun search(query: String): Flow<List<Movie>> =
        movieDao.search(query).map { list -> list.map { it.toDomain() } }

    override suspend fun updateFavorite(movieId: Long, isFavorite: Boolean) =
        movieDao.updateFavorite(movieId, isFavorite)

    override suspend fun insert(movie: Movie): Long {
        movieDao.insertAll(listOf(movie.toEntity()))
        return movie.id
    }

    override suspend fun insertAll(movies: List<Movie>) =
        movieDao.insertAll(movies.map { it.toEntity() })

    override suspend fun update(movie: Movie) =
        movieDao.insertAll(listOf(movie.toEntity()))

    override suspend fun delete(movie: Movie) =
        movieDao.deleteByProvider(movie.providerId)

    override suspend fun deleteByProvider(providerId: Long) =
        movieDao.deleteByProvider(providerId)
}
