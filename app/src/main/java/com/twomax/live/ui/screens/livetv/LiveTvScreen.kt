package com.twomax.live.ui.screens.livetv

import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import com.twomax.live.core.model.Channel
import com.twomax.live.core.model.EpgProgram
import com.twomax.live.ui.navigation.Screen
import com.twomax.live.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun LiveTvSortChip(
    label: String,
    order: LiveTvSortOrder,
    currentOrder: LiveTvSortOrder,
    onClick: (LiveTvSortOrder) -> Unit
) {
    val isSelected = order == currentOrder
    Surface(
        onClick = { onClick(order) },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) Primary else SurfaceElevated,
            focusedContainerColor = if (isSelected) PrimaryVariant else SurfaceHighest
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) TextPrimary else TextSecondary
        )
    }
}

private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LiveTvScreen(
    navController: NavHostController,
    viewModel: LiveTvViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
            Text("Loading...", style = MaterialTheme.typography.titleLarge, color = TextSecondary)
        }
        return
    }

    if (uiState.groups.isEmpty() && uiState.channels.isEmpty()) {
        Box(Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
            Text("No channels found. Sync a provider first.", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
        }
        return
    }

    Row(Modifier.fillMaxSize().background(Background)) {
        // Sidebar - Groups
        LazyColumn(
            modifier = Modifier.width(200.dp).fillMaxHeight().background(Surface).padding(vertical = 8.dp)
        ) {
            items(uiState.groups) { group ->
                val isSelected = group == uiState.selectedGroup
                Surface(
                    onClick = { viewModel.selectGroup(group) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (isSelected) Primary else Color.Transparent,
                        focusedContainerColor = if (isSelected) PrimaryVariant else SurfaceElevated
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
                    )
                ) {
                    Text(
                        text = group,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }

        // Channel list
        if (uiState.channels.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                Text("No channels", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            }
        } else {
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                // Sort bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LiveTvSortChip("Default", LiveTvSortOrder.DEFAULT, uiState.sortOrder) { viewModel.setSortOrder(it) }
                    LiveTvSortChip("A-Z", LiveTvSortOrder.A_Z, uiState.sortOrder) { viewModel.setSortOrder(it) }
                    LiveTvSortChip("Z-A", LiveTvSortOrder.Z_A, uiState.sortOrder) { viewModel.setSortOrder(it) }
                    Surface(
                        onClick = { navController.navigate(Screen.Search.route) },
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = SurfaceElevated,
                            focusedContainerColor = Primary
                        ),
                        border = ClickableSurfaceDefaults.border(
                            focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
                        )
                    ) {
                        Text(
                            "\uD83D\uDD0D Search",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(uiState.channels, key = { it.id }) { channel ->
                    ChannelCard(
                        channel = channel,
                        currentProgram = uiState.currentPrograms[channel.epgChannelId],
                        nextProgram = uiState.nextPrograms[channel.epgChannelId],
                        onClick = { navController.navigate(Screen.Player.createRoute(channel.streamUrl, channel.name)) },
                        onFavoriteToggle = { viewModel.toggleFavorite(channel) }
                    )
                }
            }
            } // close Column wrapping sort bar + list
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ChannelCard(
    channel: Channel,
    currentProgram: EpgProgram?,
    nextProgram: EpgProgram?,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    var longPressTriggered by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .onPreviewKeyEvent { event ->
                val code = event.nativeKeyEvent.keyCode
                if (code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER) {
                    when {
                        event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                        event.nativeKeyEvent.repeatCount > 0 -> {
                            if (!longPressTriggered) {
                                longPressTriggered = true
                                onFavoriteToggle()
                            }
                            true
                        }
                        event.nativeKeyEvent.action == KeyEvent.ACTION_UP && longPressTriggered -> {
                            longPressTriggered = false
                            true
                        }
                        else -> false
                    }
                } else false
            },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = CardBackground, focusedContainerColor = SurfaceElevated),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, FocusBorder)))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Logo + optional channel number
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceHighest),
                    contentAlignment = Alignment.Center
                ) {
                    if (channel.logoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = channel.logoUrl,
                            contentDescription = channel.name,
                            modifier = Modifier.size(44.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(channel.name.take(2).uppercase(), style = MaterialTheme.typography.labelLarge, color = Primary)
                    }
                }
                if (channel.channelNumber > 0) {
                    Text(
                        text = "${channel.channelNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDisabled
                    )
                }
            }

            // Center: name + EPG info
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (currentProgram != null) {
                    val progress = remember(currentProgram) {
                        val now = System.currentTimeMillis()
                        val duration = currentProgram.endTime - currentProgram.startTime
                        if (duration > 0) ((now - currentProgram.startTime).toFloat() / duration).coerceIn(0f, 1f) else 0f
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("▶", style = MaterialTheme.typography.labelSmall, color = Primary)
                        Text(
                            text = "${timeFormat.format(Date(currentProgram.startTime))}–${timeFormat.format(Date(currentProgram.endTime))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = currentProgram.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth().height(3.dp)
                            .clip(RoundedCornerShape(2.dp)).background(SurfaceHighest)
                    ) {
                        Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(Primary))
                    }

                    if (nextProgram != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("↪", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                text = timeFormat.format(Date(nextProgram.startTime)),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = nextProgram.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else if (isFocused) {
                    Text(
                        text = "No schedule available",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDisabled
                    )
                }
            }

            // Right: favorite status indicator (toggle via long-press ENTER)
            Text(
                text = if (channel.isFavorite) "\u2605" else "\u2606",
                color = if (channel.isFavorite) Warning else TextSecondary,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
