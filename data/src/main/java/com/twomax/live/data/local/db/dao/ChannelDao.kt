package com.twomax.live.data.local.db.dao

import androidx.room.*
import com.twomax.live.data.local.db.entity.ChannelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {

    @Query("SELECT * FROM channels WHERE providerId = :providerId ORDER BY customOrder, channelNumber, name")
    fun getByProvider(providerId: Long): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE providerId = :providerId AND groupTitle = :groupTitle ORDER BY customOrder, channelNumber, name")
    fun getByProviderAndGroup(providerId: Long, groupTitle: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE providerId = :providerId AND isFavorite = 1 ORDER BY name")
    fun getFavorites(providerId: Long): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 ORDER BY name")
    fun getFavorites(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE providerId = :providerId AND name LIKE '%' || :query || '%' ORDER BY name")
    fun search(query: String, providerId: Long): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' ORDER BY name")
    fun search(query: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE epgChannelId = :epgChannelId LIMIT 1")
    suspend fun getByEpgId(epgChannelId: String): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(channels: List<ChannelEntity>)

    @Query("DELETE FROM channels WHERE providerId = :providerId")
    suspend fun deleteByProvider(providerId: Long)

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT DISTINCT groupTitle FROM channels WHERE providerId = :providerId ORDER BY groupTitle")
    fun getGroups(providerId: Long): Flow<List<String>>

    @Query("SELECT * FROM channels WHERE providerId = :providerId AND streamType = :streamType ORDER BY name")
    fun getByStreamType(providerId: Long, streamType: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE providerId = :providerId AND groupTitle = :groupTitle AND streamType = :streamType ORDER BY customOrder, channelNumber, name")
    fun getByGroupAndStreamType(providerId: Long, groupTitle: String, streamType: String): Flow<List<ChannelEntity>>

    @Query("SELECT DISTINCT groupTitle FROM channels WHERE providerId = :providerId AND streamType = :streamType AND groupTitle != '' ORDER BY groupTitle")
    fun getGroupsByStreamType(providerId: Long, streamType: String): Flow<List<String>>
}
