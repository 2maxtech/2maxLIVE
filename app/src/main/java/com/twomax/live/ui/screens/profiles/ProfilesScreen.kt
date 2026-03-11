package com.twomax.live.ui.screens.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.twomax.live.core.model.Provider
import com.twomax.live.core.model.ProviderType
import com.twomax.live.data.sync.SyncEngine
import com.twomax.live.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ProfilesScreen(
    navController: NavHostController,
    viewModel: ProfilesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Playlists",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
            Text(
                text = "Manage playlists at player.2max.tech",
                style = MaterialTheme.typography.bodySmall,
                color = TextDisabled
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sync result message
        uiState.syncResult?.let { result ->
            val (text, color) = when (result) {
                is SyncEngine.SyncResult.Success -> Pair(
                    "Sync complete: ${result.channelCount} channels, ${result.movieCount} movies, ${result.seriesCount} series",
                    Success
                )
                is SyncEngine.SyncResult.Error -> Pair(
                    "Sync failed: ${result.message}",
                    Error
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.small)
                    .background(SurfaceElevated)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = text, color = color, style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = { viewModel.clearSyncResult() },
                        colors = ButtonDefaults.colors(
                            containerColor = Surface,
                            contentColor = TextSecondary,
                            focusedContainerColor = SurfaceHighest,
                            focusedContentColor = TextPrimary
                        )
                    ) {
                        Text("Dismiss", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.providers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No playlists found",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add playlists at player.2max.tech",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextDisabled
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Restart the app after adding",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDisabled
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.providers, key = { it.id }) { provider ->
                    ProviderCard(
                        provider = provider,
                        isSyncing = uiState.isSyncing && uiState.syncingProviderId == provider.id,
                        onSync = { viewModel.sync(provider) },
                        onSetActive = { viewModel.setActive(provider) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ProviderCard(
    provider: Provider,
    isSyncing: Boolean,
    onSync: () -> Unit,
    onSetActive: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(if (provider.isActive) SurfaceHighest else SurfaceElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkmark for active provider
            if (provider.isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Active",
                    tint = Success,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Spacer(modifier = Modifier.width(40.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (provider.isActive) TextPrimary else TextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = providerTypeLabel(provider.type),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDisabled
                )

                if (provider.lastSynced > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Last synced: ${formatTimestamp(provider.lastSynced)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDisabled
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!provider.isActive) {
                    Button(
                        onClick = onSetActive,
                        colors = ButtonDefaults.colors(
                            containerColor = Primary,
                            contentColor = TextPrimary,
                            focusedContainerColor = PrimaryVariant,
                            focusedContentColor = TextPrimary
                        )
                    ) {
                        Text(
                            text = "Select",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Button(
                    onClick = onSync,
                    enabled = !isSyncing,
                    colors = ButtonDefaults.colors(
                        containerColor = Secondary,
                        contentColor = TextPrimary,
                        focusedContainerColor = SecondaryVariant,
                        focusedContentColor = TextPrimary
                    )
                ) {
                    if (isSyncing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = TextPrimary,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Syncing",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Sync",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun providerTypeLabel(type: ProviderType): String = when (type) {
    ProviderType.M3U_URL -> "M3U URL"
    ProviderType.M3U_FILE -> "M3U File"
    ProviderType.XTREAM -> "Xtream Codes"
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return "Never"
    val sdf = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
