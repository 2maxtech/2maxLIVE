package com.twomax.live.ui.screens.splash

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.tv.material3.*
import com.twomax.live.R
import com.twomax.live.data.remote.dto.PlaylistResponse
import com.twomax.live.data.sync.RemotePlaylistSync
import com.twomax.live.data.sync.SyncStatus
import com.twomax.live.ui.navigation.Screen
import com.twomax.live.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SplashScreen(
    navController: NavHostController,
    remotePlaylistSync: RemotePlaylistSync
) {
    val syncState by remotePlaylistSync.syncState.collectAsStateWithLifecycle()
    val availablePlaylists by remotePlaylistSync.availablePlaylists.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var hasNavigated by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }
    var selectedPlaylist by remember { mutableStateOf<PlaylistResponse?>(null) }

    // Start fetching playlists on first composition
    LaunchedEffect(Unit) {
        val playlists = remotePlaylistSync.fetchPlaylists()
        if (playlists.size <= 1) {
            // Auto-sync the single playlist (or handle empty case)
            if (playlists.size == 1) {
                remotePlaylistSync.syncSinglePlaylist(playlists[0])
            } else {
                // No playlists — go to Home anyway
                remotePlaylistSync.clearSyncState()
            }
        } else {
            // Show picker
            showPicker = true
        }
    }

    // Navigate to Home when sync completes successfully
    LaunchedEffect(syncState.status) {
        if (syncState.status == SyncStatus.SUCCESS && !hasNavigated) {
            hasNavigated = true
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        if (showPicker && availablePlaylists.size > 1 && syncState.status != SyncStatus.SYNCING) {
            // Playlist picker
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_2max),
                    contentDescription = "2maX player",
                    modifier = Modifier
                        .size(100.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Select a Playlist",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Choose which playlist to sync",
                    fontSize = 16.sp,
                    color = TextSecondary
                )

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(availablePlaylists, key = { it.id }) { playlist ->
                        val isSelected = selectedPlaylist?.id == playlist.id
                        Surface(
                            onClick = { selectedPlaylist = playlist },
                            modifier = Modifier.fillMaxWidth(),
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = if (isSelected) Primary.copy(alpha = 0.2f) else SurfaceElevated,
                                focusedContainerColor = if (isSelected) Primary.copy(alpha = 0.3f) else SurfaceHighest
                            ),
                            border = ClickableSurfaceDefaults.border(
                                border = if (isSelected) Border(BorderStroke(2.dp, Primary)) else Border.None,
                                focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Spacer(Modifier.size(24.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playlist.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (playlist.type == "XTREAM") "Xtream Codes" else "M3U",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Sync button
                Surface(
                    onClick = {
                        selectedPlaylist?.let { playlist ->
                            showPicker = false
                            coroutineScope.launch {
                                remotePlaylistSync.syncSinglePlaylist(playlist)
                            }
                        }
                    },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (selectedPlaylist != null) Primary else SurfaceElevated,
                        focusedContainerColor = if (selectedPlaylist != null) PrimaryVariant else SurfaceHighest
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sync Selected Playlist",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedPlaylist != null) TextPrimary else TextDisabled
                        )
                    }
                }
            }
        } else {
            // Loading / syncing state — show logo + progress
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_2max),
                    contentDescription = "2maX player",
                    modifier = Modifier
                        .size(160.dp),
                    contentScale = ContentScale.Fit
                )

                if (syncState.status == SyncStatus.ERROR) {
                    Text(
                        text = syncState.message,
                        fontSize = 14.sp,
                        color = Error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        onClick = {
                            hasNavigated = true
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        },
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Primary,
                            focusedContainerColor = PrimaryVariant
                        ),
                        border = ClickableSurfaceDefaults.border(
                            focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
                        )
                    ) {
                        Text(
                            text = "Continue Anyway",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        text = syncState.message.ifEmpty { "Loading..." },
                        fontSize = 14.sp,
                        color = TextSecondary
                    )

                    LinearProgressIndicator(
                        modifier = Modifier
                            .width(200.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Primary,
                        trackColor = SurfaceElevated
                    )
                }
            }
        }
    }
}
