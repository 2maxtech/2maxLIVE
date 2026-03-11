package com.twomax.live.core.model

data class Channel(
    val id: Long = 0,
    val providerId: Long,
    val name: String,
    val logoUrl: String = "",
    val groupTitle: String = "",
    val streamUrl: String,
    val streamType: StreamType = StreamType.LIVE,
    val epgChannelId: String = "",
    val channelNumber: Int = 0,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val customOrder: Int = 0
)

enum class StreamType {
    LIVE, MOVIE, SERIES
}
