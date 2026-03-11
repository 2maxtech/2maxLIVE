package com.twomax.live.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterDeviceRequest(
    val macAddress: String
)

@JsonClass(generateAdapter = true)
data class DeviceResponse(
    val id: String,
    val macAddress: String,
    val activated: Boolean
)

@JsonClass(generateAdapter = true)
data class DeviceStatusResponse(
    val activated: Boolean,
    val plan: String? = null,
    val expiresAt: String? = null,
    val playlistCount: Int = 0,
    val isLocked: Boolean = false
)

@JsonClass(generateAdapter = true)
data class PlaylistResponse(
    val id: String,
    val name: String,
    val type: String,
    val serverUrl: String? = null,
    val username: String? = null,
    val password: String? = null,
    val m3uUrl: String? = null,
    val sortOrder: Int = 0,
    val isActive: Boolean = true
)
