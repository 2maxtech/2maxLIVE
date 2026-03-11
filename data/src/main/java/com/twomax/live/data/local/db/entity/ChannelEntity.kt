package com.twomax.live.data.local.db.entity

import androidx.room.*

@Entity(
    tableName = "channels",
    indices = [
        Index("providerId"),
        Index("groupTitle"),
        Index("epgChannelId"),
        Index("streamType")
    ],
    foreignKeys = [ForeignKey(
        entity = ProviderEntity::class,
        parentColumns = ["id"],
        childColumns = ["providerId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val providerId: Long,
    val name: String,
    val logoUrl: String = "",
    val groupTitle: String = "",
    val streamUrl: String,
    val streamType: String = "LIVE",
    val epgChannelId: String = "",
    val channelNumber: Int = 0,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val customOrder: Int = 0
)
