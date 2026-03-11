package com.twomax.live.core.repository

import com.twomax.live.core.model.Channel
import kotlinx.coroutines.flow.Flow

interface ChannelRepository {
    fun getByProvider(providerId: Long): Flow<List<Channel>>
    fun getByCategory(providerId: Long, groupTitle: String): Flow<List<Channel>>
    fun getFavorites(): Flow<List<Channel>>
    fun search(query: String): Flow<List<Channel>>
    suspend fun updateFavorite(channelId: Long, isFavorite: Boolean)
    suspend fun insert(channel: Channel): Long
    suspend fun insertAll(channels: List<Channel>)
    suspend fun update(channel: Channel)
    suspend fun delete(channel: Channel)
    suspend fun deleteByProvider(providerId: Long)
}
