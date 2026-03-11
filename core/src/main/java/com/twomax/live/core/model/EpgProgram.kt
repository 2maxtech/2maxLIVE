package com.twomax.live.core.model

data class EpgProgram(
    val id: Long = 0,
    val channelEpgId: String,
    val title: String,
    val description: String = "",
    val startTime: Long,
    val endTime: Long,
    val lang: String = ""
)
