package com.twomax.live.core.repository

import com.twomax.live.core.model.Provider
import kotlinx.coroutines.flow.Flow

interface ProviderRepository {
    fun getAll(): Flow<List<Provider>>
    fun getById(id: Long): Flow<Provider?>
    suspend fun insert(provider: Provider): Long
    suspend fun update(provider: Provider)
    suspend fun delete(provider: Provider)
}
