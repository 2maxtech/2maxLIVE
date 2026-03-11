package com.twomax.live.core.model

data class Category(
    val id: Long = 0,
    val categoryId: String,
    val name: String,
    val streamType: StreamType,
    val providerId: Long,
    val parentId: String = ""
)
