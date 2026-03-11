package com.twomax.live.data.local.db.dao

import androidx.room.*
import com.twomax.live.data.local.db.entity.PublicEpgSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PublicEpgSourceDao {

    @Query("SELECT * FROM public_epg_sources ORDER BY region, label")
    fun getAll(): Flow<List<PublicEpgSourceEntity>>

    @Query("SELECT * FROM public_epg_sources WHERE isEnabled = 1")
    suspend fun getEnabled(): List<PublicEpgSourceEntity>

    @Query("UPDATE public_epg_sources SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(sources: List<PublicEpgSourceEntity>)

    @Delete
    suspend fun delete(source: PublicEpgSourceEntity)

    @Query("SELECT COUNT(*) FROM public_epg_sources")
    suspend fun count(): Int

    @Query("SELECT * FROM public_epg_sources WHERE id = :id")
    suspend fun getById(id: Long): PublicEpgSourceEntity?

    @Query("UPDATE public_epg_sources SET isEnabled = 1 WHERE isCurated = 1")
    suspend fun enableAllCurated()
}
