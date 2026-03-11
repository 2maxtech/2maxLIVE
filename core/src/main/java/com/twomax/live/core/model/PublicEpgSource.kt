package com.twomax.live.core.model

data class PublicEpgSource(
    val id: Long = 0,
    val label: String,
    val region: String,
    val url: String,
    val isCurated: Boolean = true,
    val isEnabled: Boolean = false
)
