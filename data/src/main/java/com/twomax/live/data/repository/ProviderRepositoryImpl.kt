package com.twomax.live.data.repository

import com.twomax.live.core.model.Provider
import com.twomax.live.core.repository.ProviderRepository
import com.twomax.live.data.local.db.dao.ProviderDao
import com.twomax.live.data.local.db.entity.toDomain
import com.twomax.live.data.local.db.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProviderRepositoryImpl @Inject constructor(
    private val providerDao: ProviderDao
) : ProviderRepository {

    override fun getAll(): Flow<List<Provider>> =
        providerDao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getById(id: Long): Flow<Provider?> =
        providerDao.getById(id).map { it?.toDomain() }

    override suspend fun insert(provider: Provider): Long =
        providerDao.insert(provider.toEntity())

    override suspend fun update(provider: Provider) =
        providerDao.update(provider.toEntity())

    override suspend fun delete(provider: Provider) =
        providerDao.delete(provider.toEntity())
}
