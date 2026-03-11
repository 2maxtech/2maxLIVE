package com.twomax.live.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.twomax.live.core.model.PublicEpgSource

@Entity(tableName = "public_epg_sources")
data class PublicEpgSourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val region: String,
    val url: String,
    val isCurated: Boolean = true,
    val isEnabled: Boolean = false
)

fun PublicEpgSourceEntity.toDomain() = PublicEpgSource(
    id = id,
    label = label,
    region = region,
    url = url,
    isCurated = isCurated,
    isEnabled = isEnabled
)

fun PublicEpgSource.toEntity() = PublicEpgSourceEntity(
    id = id,
    label = label,
    region = region,
    url = url,
    isCurated = isCurated,
    isEnabled = isEnabled
)
