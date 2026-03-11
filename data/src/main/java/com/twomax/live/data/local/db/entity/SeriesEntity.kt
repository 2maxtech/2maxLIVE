package com.twomax.live.data.local.db.entity

import androidx.room.*

@Entity(
    tableName = "series",
    indices = [
        Index("providerId"),
        Index("categoryId")
    ],
    foreignKeys = [ForeignKey(
        entity = ProviderEntity::class,
        parentColumns = ["id"],
        childColumns = ["providerId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class SeriesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val providerId: Long,
    val seriesId: String = "",
    val name: String,
    val coverUrl: String = "",
    val plot: String = "",
    val genre: String = "",
    val rating: Float = 0f,
    val year: String = "",
    val categoryId: String = "",
    val isFavorite: Boolean = false
)
