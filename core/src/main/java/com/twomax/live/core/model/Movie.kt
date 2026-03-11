package com.twomax.live.core.model

data class Movie(
    val id: Long = 0,
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
