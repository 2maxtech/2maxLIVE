package com.twomax.live.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.tv.material3.*
import com.twomax.live.ui.theme.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp).clipToBounds(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineLarge)
        }

        // EPG Settings section
        item {
            Text("EPG", style = MaterialTheme.typography.titleLarge, color = Primary)
            Spacer(Modifier.height(8.dp))
        }

        // Auto sync toggle
        item {
            Surface(
                onClick = { viewModel.toggleEpgAutoSync(!state.epgAutoSync) },
                modifier = Modifier.fillMaxWidth(),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
                colors = ClickableSurfaceDefaults.colors(containerColor = CardBackground, focusedContainerColor = SurfaceElevated),
                border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, FocusBorder))),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Auto-sync EPG", style = MaterialTheme.typography.bodyLarge)
                        Text("Automatically update EPG data", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                    Text(
                        text = if (state.epgAutoSync) "ON" else "OFF",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (state.epgAutoSync) Success else TextSecondary
                    )
                }
            }
        }

        // Sync interval
        item {
            Text("Sync Interval", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3, 6, 12, 24).forEach { hours ->
                    val isSelected = state.epgSyncIntervalHours == hours
                    Surface(
                        onClick = { viewModel.updateEpgSyncInterval(hours) },
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (isSelected) Primary else SurfaceElevated,
                            focusedContainerColor = if (isSelected) PrimaryVariant else SurfaceHighest
                        ),
                        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, FocusBorder)))
                    ) {
                        Text(
                            text = "${hours}h",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Device section
        item {
            Spacer(Modifier.height(16.dp))
            Text("DEVICE", style = MaterialTheme.typography.titleLarge, color = Primary)
            Spacer(Modifier.height(8.dp))
            Surface(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
                colors = ClickableSurfaceDefaults.colors(containerColor = CardBackground, focusedContainerColor = SurfaceElevated),
                border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, FocusBorder))),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("Device ID", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text(
                            text = state.deviceMac,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            ),
                            color = TextPrimary
                        )
                    }
                    Column {
                        Text("Manage Playlists", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("Visit player.2max.tech to manage your playlists", style = MaterialTheme.typography.bodyMedium, color = Primary)
                    }
                }
            }
        }

        // About section
        item {
            Spacer(Modifier.height(16.dp))
            Text("About", style = MaterialTheme.typography.titleLarge, color = Primary)
            Spacer(Modifier.height(8.dp))
            Surface(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
                colors = ClickableSurfaceDefaults.colors(containerColor = CardBackground, focusedContainerColor = SurfaceElevated),
                border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, FocusBorder))),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1f)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("2maX player", style = MaterialTheme.typography.titleMedium, color = Primary)
                    Text("IPTV Player for Android TV", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text("Version 1.0.0", style = MaterialTheme.typography.labelMedium, color = TextDisabled)
                }
            }
        }
    }
}
