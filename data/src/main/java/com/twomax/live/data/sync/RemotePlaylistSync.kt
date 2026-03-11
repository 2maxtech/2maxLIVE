package com.twomax.live.data.sync

import android.content.Context
import android.util.Log
import com.twomax.live.core.model.Provider
import com.twomax.live.core.model.ProviderType
import com.twomax.live.data.device.DeviceIdProvider
import com.twomax.live.data.local.db.dao.ProviderDao
import com.twomax.live.data.local.db.entity.ProviderEntity
import com.twomax.live.data.remote.api.ActivationApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR,
    AWAITING_SELECTION
}

data class SyncState(
    val status: SyncStatus = SyncStatus.IDLE,
    val message: String = ""
)

@Singleton
class RemotePlaylistSync @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceIdProvider: DeviceIdProvider,
    private val activationApi: ActivationApi,
    private val providerDao: ProviderDao,
    private val syncEngine: SyncEngine
) {
    private val prefs = context.getSharedPreferences("activation", Context.MODE_PRIVATE)
    private val syncMutex = Mutex()

    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _availablePlaylists = MutableStateFlow<List<com.twomax.live.data.remote.dto.PlaylistResponse>>(emptyList())
    val availablePlaylists: StateFlow<List<com.twomax.live.data.remote.dto.PlaylistResponse>> = _availablePlaylists.asStateFlow()

    fun isActivated(): Boolean = prefs.getBoolean("is_activated", false)

    fun clearSyncState() {
        _syncState.value = SyncState()
    }

    suspend fun fetchPlaylists(): List<com.twomax.live.data.remote.dto.PlaylistResponse> = withContext(Dispatchers.IO) {
        _syncState.value = SyncState(SyncStatus.SYNCING, "Fetching playlists...")
        try {
            val mac = deviceIdProvider.getMacAddress()
            Log.d("RemoteSync", "Fetching playlists for $mac")
            val remotePlaylists = activationApi.getPlaylists(mac).filter { it.isActive }
            Log.d("RemoteSync", "Got ${remotePlaylists.size} active remote playlists")
            _availablePlaylists.value = remotePlaylists
            if (remotePlaylists.size > 1) {
                _syncState.value = SyncState(SyncStatus.AWAITING_SELECTION, "Select a playlist")
            }
            remotePlaylists
        } catch (e: Exception) {
            Log.e("RemoteSync", "Fetch playlists failed", e)
            _syncState.value = SyncState(SyncStatus.ERROR, "Failed to fetch playlists: ${e.message}")
            emptyList()
        }
    }

    suspend fun syncSinglePlaylist(playlist: com.twomax.live.data.remote.dto.PlaylistResponse) = withContext(Dispatchers.IO) {
        if (!syncMutex.tryLock()) {
            Log.d("RemoteSync", "Sync already in progress, skipping")
            return@withContext
        }
        try {
            _syncState.value = SyncState(SyncStatus.SYNCING, "Syncing ${playlist.name}...")

            val typeStr = if (playlist.type == "XTREAM") "XTREAM" else "M3U_URL"
            val providerType = if (playlist.type == "XTREAM") ProviderType.XTREAM else ProviderType.M3U_URL
            val url = when (providerType) {
                ProviderType.XTREAM -> playlist.serverUrl ?: ""
                ProviderType.M3U_URL -> playlist.m3uUrl ?: ""
                else -> ""
            }

            val existingProviders = providerDao.getAllList()

            // Remove all existing providers (we only keep the selected one)
            for (existing in existingProviders) {
                if (existing.name != playlist.name) {
                    Log.d("RemoteSync", "Removing provider '${existing.name}' (not selected)")
                    providerDao.delete(existing)
                }
            }

            val existing = existingProviders.find { it.name == playlist.name }

            if (existing != null) {
                val needsUpdate = existing.url != url ||
                    existing.username != (playlist.username ?: "") ||
                    existing.password != (playlist.password ?: "")

                if (needsUpdate) {
                    providerDao.update(
                        existing.copy(
                            url = url,
                            username = playlist.username ?: "",
                            password = playlist.password ?: "",
                            type = typeStr
                        )
                    )
                }

                val provider = Provider(
                    id = existing.id,
                    name = playlist.name,
                    type = providerType,
                    url = url,
                    username = playlist.username ?: "",
                    password = playlist.password ?: ""
                )
                val result = syncEngine.syncProvider(provider)
                Log.d("RemoteSync", "Synced '${playlist.name}': $result")
            } else {
                val entity = ProviderEntity(
                    name = playlist.name,
                    type = typeStr,
                    url = url,
                    username = playlist.username ?: "",
                    password = playlist.password ?: ""
                )
                val id = providerDao.insert(entity)
                val provider = Provider(
                    id = id,
                    name = playlist.name,
                    type = providerType,
                    url = url,
                    username = playlist.username ?: "",
                    password = playlist.password ?: ""
                )
                val result = syncEngine.syncProvider(provider)
                Log.d("RemoteSync", "Synced new '${playlist.name}': $result")
            }

            _syncState.value = SyncState(SyncStatus.SUCCESS, "Playlists synced successfully")
        } catch (e: Exception) {
            Log.e("RemoteSync", "Sync failed", e)
            _syncState.value = SyncState(SyncStatus.ERROR, "Sync failed: ${e.message}")
        } finally {
            syncMutex.unlock()
        }
    }

    suspend fun sync() = withContext(Dispatchers.IO) {
        if (!syncMutex.tryLock()) {
            Log.d("RemoteSync", "Sync already in progress, skipping")
            return@withContext
        }
        try {
        _syncState.value = SyncState(SyncStatus.SYNCING, "Syncing playlists...")
        try {
            val mac = deviceIdProvider.getMacAddress()
            Log.d("RemoteSync", "Fetching playlists for $mac")

            val remotePlaylists = activationApi.getPlaylists(mac)
            Log.d("RemoteSync", "Got ${remotePlaylists.size} remote playlists")

            val existingProviders = providerDao.getAllList()

            val remoteNames = mutableSetOf<String>()

            for (remote in remotePlaylists) {
                if (!remote.isActive) continue

                val typeStr = if (remote.type == "XTREAM") "XTREAM" else "M3U_URL"
                val providerType = if (remote.type == "XTREAM") ProviderType.XTREAM else ProviderType.M3U_URL

                val url = when (providerType) {
                    ProviderType.XTREAM -> remote.serverUrl ?: ""
                    ProviderType.M3U_URL -> remote.m3uUrl ?: ""
                    else -> ""
                }

                remoteNames.add(remote.name)

                val existing = existingProviders.find { it.name == remote.name }

                if (existing != null) {
                    val needsUpdate = existing.url != url ||
                        existing.username != (remote.username ?: "") ||
                        existing.password != (remote.password ?: "")

                    if (needsUpdate) {
                        Log.d("RemoteSync", "Updating provider '${remote.name}'")
                        providerDao.update(
                            existing.copy(
                                url = url,
                                username = remote.username ?: "",
                                password = remote.password ?: "",
                                type = typeStr
                            )
                        )
                    }

                    // Always re-sync content on launch
                    val provider = Provider(
                        id = existing.id,
                        name = remote.name,
                        type = providerType,
                        url = url,
                        username = remote.username ?: "",
                        password = remote.password ?: ""
                    )
                    val result = syncEngine.syncProvider(provider)
                    Log.d("RemoteSync", "Synced '${remote.name}': $result")
                } else {
                    Log.d("RemoteSync", "Creating provider '${remote.name}'")
                    val entity = ProviderEntity(
                        name = remote.name,
                        type = typeStr,
                        url = url,
                        username = remote.username ?: "",
                        password = remote.password ?: ""
                    )
                    val id = providerDao.insert(entity)
                    val provider = Provider(
                        id = id,
                        name = remote.name,
                        type = providerType,
                        url = url,
                        username = remote.username ?: "",
                        password = remote.password ?: ""
                    )
                    val result = syncEngine.syncProvider(provider)
                    Log.d("RemoteSync", "Synced new '${remote.name}': $result")
                }
            }

            // Remove providers that were remotely managed but no longer exist
            for (existing in existingProviders) {
                if (remoteNames.isNotEmpty() && !remoteNames.contains(existing.name)) {
                    // Only auto-remove if we got at least 1 remote playlist
                    // (to avoid wiping everything on network error)
                    Log.d("RemoteSync", "Removing stale provider '${existing.name}'")
                    providerDao.delete(existing)
                }
            }

            Log.d("RemoteSync", "Sync complete")
            _syncState.value = SyncState(SyncStatus.SUCCESS, "Playlists synced successfully")
        } catch (e: Exception) {
            Log.e("RemoteSync", "Sync failed", e)
            _syncState.value = SyncState(SyncStatus.ERROR, "Sync failed: ${e.message}")
        }
        } finally {
            syncMutex.unlock()
        }
    }
}
