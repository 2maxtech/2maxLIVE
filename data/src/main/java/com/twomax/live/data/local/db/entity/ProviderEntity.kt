package com.twomax.live.data.local.db.entity

import androidx.room.*

@Entity(tableName = "providers", indices = [Index("isActive")])
data class ProviderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // M3U_URL, M3U_FILE, XTREAM
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val epgUrl: String = "",
    val isActive: Boolean = true,
    val lastSynced: Long = 0
)
