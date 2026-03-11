package com.twomax.live.core.repository

import com.twomax.live.core.model.Category
import com.twomax.live.core.model.StreamType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getByProviderAndType(providerId: Long, streamType: StreamType): Flow<List<Category>>
    suspend fun insertAll(categories: List<Category>)
    suspend fun deleteByProvider(providerId: Long)
}
