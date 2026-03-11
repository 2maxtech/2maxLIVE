package com.twomax.live.data.local.db.entity

import androidx.room.*

@Entity(
    tableName = "episodes",
    indices = [
        Index("seriesId"),
        Index("seriesId", "seasonNumber")
    ],
    foreignKeys = [ForeignKey(
        entity = SeriesEntity::class,
        parentColumns = ["id"],
        childColumns = ["seriesId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seriesId: Long,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val title: String = "",
    val streamUrl: String,
    val coverUrl: String = "",
    val plot: String = "",
    val duration: String = "",
    val containerExtension: String = ""
)
