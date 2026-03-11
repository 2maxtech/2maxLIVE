package com.twomax.live.data.local.db.dao

import androidx.room.*
import com.twomax.live.data.local.db.entity.ProviderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderDao {

    @Query("SELECT * FROM providers ORDER BY name")
    fun getAll(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers ORDER BY name")
    suspend fun getAllList(): List<ProviderEntity>

    @Query("SELECT * FROM providers WHERE id = :id")
    fun getById(id: Long): Flow<ProviderEntity?>

    @Query("SELECT * FROM providers WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): ProviderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(provider: ProviderEntity): Long

    @Update
    suspend fun update(provider: ProviderEntity)

    @Delete
    suspend fun delete(provider: ProviderEntity)

    @Query("DELETE FROM providers WHERE id = :id")
    suspend fun deleteById(id: Long)
}
