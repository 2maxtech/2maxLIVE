package com.twomax.live.core.model

data class Series(
    val id: Long = 0,
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

data class Episode(
    val id: Long = 0,
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
