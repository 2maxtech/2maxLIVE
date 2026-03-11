package com.twomax.live.data.local.db.entity

import androidx.room.*

@Entity(
    tableName = "movies",
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
data class MovieEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val providerId: Long,
    val vodId: String = "",
    val name: String,
    val streamUrl: String,
    val coverUrl: String = "",
    val plot: String = "",
    val genre: String = "",
    val rating: Float = 0f,
    val duration: String = "",
    val year: String = "",
    val categoryId: String = "",
    val containerExtension: String = "",
    val isFavorite: Boolean = false
)
