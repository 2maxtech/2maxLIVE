package com.twomax.live.data.local.db.entity

import androidx.room.*

@Entity(
    tableName = "categories",
    indices = [
        Index("providerId"),
        Index("streamType")
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: String,
    val name: String,
    val streamType: String,
    val providerId: Long,
    val parentId: String = ""
)
