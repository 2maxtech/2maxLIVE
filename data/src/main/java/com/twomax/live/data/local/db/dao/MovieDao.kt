package com.twomax.live.data.local.db.dao

import androidx.room.*
import com.twomax.live.data.local.db.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("SELECT * FROM movies WHERE id = :id")
    fun getById(id: Long): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE providerId = :providerId ORDER BY name")
    fun getByProvider(providerId: Long): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE providerId = :providerId AND categoryId = :categoryId ORDER BY name")
    fun getByCategory(providerId: Long, categoryId: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE providerId = :providerId AND isFavorite = 1 ORDER BY name")
    fun getFavorites(providerId: Long): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isFavorite = 1 ORDER BY name")
    fun getFavorites(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE providerId = :providerId AND name LIKE '%' || :query || '%' ORDER BY name")
    fun search(query: String, providerId: Long): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE name LIKE '%' || :query || '%' ORDER BY name")
    fun search(query: String): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<MovieEntity>)

    @Query("DELETE FROM movies WHERE providerId = :providerId")
    suspend fun deleteByProvider(providerId: Long)

    @Query("UPDATE movies SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)
}
