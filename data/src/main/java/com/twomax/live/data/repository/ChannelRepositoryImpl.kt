package com.twomax.live.data.repository

import com.twomax.live.core.model.Channel
import com.twomax.live.core.repository.ChannelRepository
import com.twomax.live.data.local.db.dao.ChannelDao
import com.twomax.live.data.local.db.entity.toDomain
import com.twomax.live.data.local.db.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChannelRepositoryImpl @Inject constructor(
    private val channelDao: ChannelDao
) : ChannelRepository {

    override fun getByProvider(providerId: Long): Flow<List<Channel>> =
        channelDao.getByProvider(providerId).map { list -> list.map { it.toDomain() } }

    override fun getByCategory(providerId: Long, groupTitle: String): Flow<List<Channel>> =
        channelDao.getByProviderAndGroup(providerId, groupTitle).map { list -> list.map { it.toDomain() } }

    override fun getFavorites(): Flow<List<Channel>> =
        channelDao.getFavorites().map { list -> list.map { it.toDomain() } }

    override fun search(query: String): Flow<List<Channel>> =
        channelDao.search(query).map { list -> list.map { it.toDomain() } }

    override suspend fun updateFavorite(channelId: Long, isFavorite: Boolean) =
        channelDao.updateFavorite(channelId, isFavorite)

    override suspend fun insert(channel: Channel): Long {
        channelDao.insertAll(listOf(channel.toEntity()))
        return channel.id
    }

    override suspend fun insertAll(channels: List<Channel>) =
        channelDao.insertAll(channels.map { it.toEntity() })

    override suspend fun update(channel: Channel) =
        channelDao.insertAll(listOf(channel.toEntity()))

    override suspend fun delete(channel: Channel) =
        channelDao.deleteByProvider(channel.providerId)

    override suspend fun deleteByProvider(providerId: Long) =
        channelDao.deleteByProvider(providerId)
}
