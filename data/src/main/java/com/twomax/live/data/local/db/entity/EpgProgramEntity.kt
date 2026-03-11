package com.twomax.live.data.local.db.entity

import androidx.room.*

@Entity(
    tableName = "epg_programs",
    indices = [
        Index("channelEpgId"),
        Index("startTime"),
        Index("endTime")
    ]
)
data class EpgProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelEpgId: String,
    val title: String,
    val description: String = "",
    val startTime: Long,
    val endTime: Long,
    val lang: String = ""
)
