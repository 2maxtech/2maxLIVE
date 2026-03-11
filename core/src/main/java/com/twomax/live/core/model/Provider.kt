package com.twomax.live.core.model

data class Provider(
    val id: Long = 0,
    val name: String,
    val type: ProviderType,
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val epgUrl: String = "",
    val isActive: Boolean = true,
    val lastSynced: Long = 0
)

enum class ProviderType {
    M3U_URL, M3U_FILE, XTREAM
}
