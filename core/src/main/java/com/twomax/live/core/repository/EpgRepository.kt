package com.twomax.live.core.repository

import com.twomax.live.core.model.EpgProgram
import kotlinx.coroutines.flow.Flow

interface EpgRepository {
    fun getProgramsForChannel(epgId: String, startTime: Long, endTime: Long): Flow<List<EpgProgram>>
    fun getCurrentProgram(epgId: String, currentTime: Long): Flow<EpgProgram?>
    fun getNextProgram(epgId: String, currentTime: Long): Flow<EpgProgram?>
    suspend fun getProgramsForChannels(channelEpgIds: List<String>, startTime: Long, endTime: Long): Map<String, List<EpgProgram>>
    suspend fun insertAll(programs: List<EpgProgram>)
    suspend fun deleteByChannel(epgId: String)
    suspend fun deleteOlderThan(timestamp: Long)
}
