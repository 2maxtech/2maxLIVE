package com.twomax.live.data.local.db.dao

import androidx.room.*
import com.twomax.live.data.local.db.entity.SeriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeriesDao {

    @Query("SELECT * FROM series WHERE providerId = :providerId ORDER BY name")
    fun getByProvider(providerId: Long): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE id = :id")
    fun getById(id: Long): Flow<SeriesEntity?>

    @Query("SELECT * FROM series WHERE providerId = :providerId AND categoryId = :categoryId ORDER BY name")
    fun getByCategory(providerId: Long, categoryId: String): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE providerId = :providerId AND isFavorite = 1 ORDER BY name")
    fun getFavorites(providerId: Long): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE isFavorite = 1 ORDER BY name")
    fun getFavorites(): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE providerId = :providerId AND name LIKE '%' || :query || '%' ORDER BY name")
    fun search(query: String, providerId: Long): Flow<List<SeriesEntity>>

    @Query("SELECT * FROM series WHERE name LIKE '%' || :query || '%' ORDER BY name")
    fun search(query: String): Flow<List<SeriesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(series: List<SeriesEntity>)

    @Query("DELETE FROM series WHERE providerId = :providerId")
    suspend fun deleteByProvider(providerId: Long)

    @Query("UPDATE series SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)
}
