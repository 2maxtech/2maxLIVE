package com.twomax.live.data.repository

import com.twomax.live.core.model.EpgProgram
import com.twomax.live.core.repository.EpgRepository
import com.twomax.live.data.local.db.dao.EpgProgramDao
import com.twomax.live.data.local.db.entity.toDomain
import com.twomax.live.data.local.db.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EpgRepositoryImpl @Inject constructor(
    private val epgProgramDao: EpgProgramDao
) : EpgRepository {

    override fun getProgramsForChannel(epgId: String, startTime: Long, endTime: Long): Flow<List<EpgProgram>> =
        epgProgramDao.getPrograms(epgId, startTime, endTime).map { list -> list.map { it.toDomain() } }

    override fun getCurrentProgram(epgId: String, currentTime: Long): Flow<EpgProgram?> = flow {
        emit(epgProgramDao.getCurrentProgram(epgId, currentTime)?.toDomain())
    }

    override fun getNextProgram(epgId: String, currentTime: Long): Flow<EpgProgram?> = flow {
        emit(epgProgramDao.getNextProgram(epgId, currentTime)?.toDomain())
    }

    override suspend fun getProgramsForChannels(channelEpgIds: List<String>, startTime: Long, endTime: Long): Map<String, List<EpgProgram>> {
        if (channelEpgIds.isEmpty()) return emptyMap()
        // Room SQLite limits IN clause to 999 params, so chunk if needed
        return channelEpgIds.chunked(500).flatMap { chunk ->
            epgProgramDao.getProgramsForChannels(chunk, startTime, endTime)
        }.map { it.toDomain() }.groupBy { it.channelEpgId }
    }

    override suspend fun insertAll(programs: List<EpgProgram>) =
        epgProgramDao.insertAll(programs.map { it.toEntity() })

    override suspend fun deleteByChannel(epgId: String) =
        epgProgramDao.deleteByChannel(epgId)

    override suspend fun deleteOlderThan(timestamp: Long) =
        epgProgramDao.deleteOlderThan(timestamp)
}
