package com.twomax.live.core.repository

import com.twomax.live.core.model.PublicEpgSource
import kotlinx.coroutines.flow.Flow

interface PublicEpgSourceRepository {
    fun getAll(): Flow<List<PublicEpgSource>>
    suspend fun toggle(id: Long, enabled: Boolean)
    suspend fun addCustom(label: String, url: String)
    suspend fun remove(id: Long)
    suspend fun count(): Int
    suspend fun insertAll(sources: List<PublicEpgSource>)
}
