package com.twomax.live.data.repository

import com.twomax.live.core.model.Category
import com.twomax.live.core.model.StreamType
import com.twomax.live.core.repository.CategoryRepository
import com.twomax.live.data.local.db.dao.CategoryDao
import com.twomax.live.data.local.db.entity.toDomain
import com.twomax.live.data.local.db.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getByProviderAndType(providerId: Long, streamType: StreamType): Flow<List<Category>> =
        categoryDao.getByProviderAndType(providerId, streamType.name).map { list -> list.map { it.toDomain() } }

    override suspend fun insertAll(categories: List<Category>) =
        categoryDao.insertAll(categories.map { it.toEntity() })

    override suspend fun deleteByProvider(providerId: Long) =
        categoryDao.deleteByProvider(providerId)
}
