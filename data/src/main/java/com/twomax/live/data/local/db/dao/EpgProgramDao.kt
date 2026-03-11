package com.twomax.live.data.local.db.dao

import androidx.room.*
import com.twomax.live.data.local.db.entity.EpgProgramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpgProgramDao {

    @Query("SELECT * FROM epg_programs WHERE channelEpgId = :channelEpgId AND startTime >= :startTime AND endTime <= :endTime ORDER BY startTime")
    fun getPrograms(channelEpgId: String, startTime: Long, endTime: Long): Flow<List<EpgProgramEntity>>

    @Query("SELECT * FROM epg_programs WHERE channelEpgId = :channelEpgId AND startTime <= :currentTime AND endTime > :currentTime LIMIT 1")
    suspend fun getCurrentProgram(channelEpgId: String, currentTime: Long): EpgProgramEntity?

    @Query("SELECT * FROM epg_programs WHERE channelEpgId = :channelEpgId AND startTime > :currentTime ORDER BY startTime LIMIT 1")
    suspend fun getNextProgram(channelEpgId: String, currentTime: Long): EpgProgramEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(programs: List<EpgProgramEntity>)

    @Query("DELETE FROM epg_programs WHERE endTime < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("DELETE FROM epg_programs WHERE channelEpgId = :channelEpgId")
    suspend fun deleteByChannel(channelEpgId: String)

    @Query("DELETE FROM epg_programs")
    suspend fun deleteAll()

    // Batch queries for displaying EPG in channel list
    @Query("SELECT * FROM epg_programs WHERE channelEpgId IN (:channelEpgIds) AND startTime <= :currentTime AND endTime > :currentTime")
    suspend fun getCurrentProgramsForChannels(channelEpgIds: List<String>, currentTime: Long): List<EpgProgramEntity>

    @Query("SELECT * FROM epg_programs WHERE channelEpgId IN (:channelEpgIds) AND startTime > :currentTime ORDER BY channelEpgId, startTime")
    suspend fun getUpcomingProgramsForChannels(channelEpgIds: List<String>, currentTime: Long): List<EpgProgramEntity>

    @Query("SELECT * FROM epg_programs WHERE channelEpgId IN (:channelEpgIds) AND startTime >= :startTime AND endTime <= :endTime ORDER BY channelEpgId, startTime")
    suspend fun getProgramsForChannels(channelEpgIds: List<String>, startTime: Long, endTime: Long): List<EpgProgramEntity>

    @Query("DELETE FROM epg_programs WHERE channelEpgId IN (:channelEpgIds)")
    suspend fun deleteByChannelIds(channelEpgIds: List<String>)
}
