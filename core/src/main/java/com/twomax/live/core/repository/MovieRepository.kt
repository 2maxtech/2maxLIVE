package com.twomax.live.core.repository

import com.twomax.live.core.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getByProvider(providerId: Long): Flow<List<Movie>>
    fun getByCategory(providerId: Long, categoryId: String): Flow<List<Movie>>
    fun getFavorites(): Flow<List<Movie>>
    fun search(query: String): Flow<List<Movie>>
    suspend fun updateFavorite(movieId: Long, isFavorite: Boolean)
    suspend fun insert(movie: Movie): Long
    suspend fun insertAll(movies: List<Movie>)
    suspend fun update(movie: Movie)
    suspend fun delete(movie: Movie)
    suspend fun deleteByProvider(providerId: Long)
}
