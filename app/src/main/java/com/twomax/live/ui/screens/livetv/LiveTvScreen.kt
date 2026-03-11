package com.twomax.live.ui.screens.livetv

import android.net.Uri
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
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

    // BackHandler: clear selection instead of navigating away
    BackHandler(enabled = uiState.selectedChannel != null) {
        viewModel.clearSelectedChannel()
    }

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
        val hasSelection = uiState.selectedChannel != null
        val channelListWeight = if (hasSelection) 0.45f else 1f

        if (uiState.channels.isEmpty()) {
            Box(Modifier.weight(channelListWeight).fillMaxHeight(), contentAlignment = Alignment.Center) {
                Text("No channels", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            }
        } else {
            Column(modifier = Modifier.weight(channelListWeight).fillMaxHeight()) {
                // Sort bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
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
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = TextSecondary
                            )
                            Text(
                                "Search",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.channels, key = { it.id }) { channel ->
                        ChannelCard(
                            channel = channel,
                            isSelected = channel.id == uiState.selectedChannel?.id,
                            currentProgram = uiState.currentPrograms[channel.epgChannelId],
                            nextProgram = uiState.nextPrograms[channel.epgChannelId],
                            onClick = { viewModel.selectChannel(channel) },
                            onFavoriteToggle = { viewModel.toggleFavorite(channel) }
                        )
                    }
                }
            }
        }

        // Preview panel
        if (hasSelection) {
            val selectedChannel = uiState.selectedChannel!!
            key(selectedChannel.id) {
                ChannelPreviewPanel(
                    channel = selectedChannel,
                    schedule = uiState.selectedChannelSchedule,
                    isScheduleLoading = uiState.isScheduleLoading,
                    onWatch = { navController.navigate(Screen.Player.createRoute(selectedChannel.streamUrl, selectedChannel.name)) },
                    onClose = { viewModel.clearSelectedChannel() },
                    modifier = Modifier.weight(0.55f).fillMaxHeight()
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ChannelPreviewPanel(
    channel: Channel,
    schedule: List<EpgProgram>,
    isScheduleLoading: Boolean,
    onWatch: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(true) }
    var videoWidth by remember { mutableIntStateOf(0) }
    var videoHeight by remember { mutableIntStateOf(0) }
    var videoCodec by remember { mutableStateOf("") }
    var audioCodec by remember { mutableStateOf("") }

    // ExoPlayer — parent key(channel.id) handles teardown/recreation
    val exoPlayer = remember {
        val httpDataSource = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 9; TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.164 Mobile Safari/537.36")
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(20000)
            .setAllowCrossProtocolRedirects(true)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(5000, 15000, 1000, 2000)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            .setEnableDecoderFallback(true)

        ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(DefaultMediaSourceFactory(httpDataSource))
            .setLoadControl(loadControl)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(channel.streamUrl)))
                volume = 0f
                playWhenReady = true
                prepare()
            }
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(size: VideoSize) {
                videoWidth = size.width
                videoHeight = size.height
            }
            override fun onTracksChanged(tracks: Tracks) {
                tracks.groups.forEach { group ->
                    for (i in 0 until group.length) {
                        val format = group.getTrackFormat(i)
                        if (group.type == C.TRACK_TYPE_VIDEO && format.codecs != null) {
                            videoCodec = format.codecs?.substringBefore(".") ?: ""
                        }
                        if (group.type == C.TRACK_TYPE_AUDIO && format.codecs != null) {
                            audioCodec = format.codecs?.substringBefore(".") ?: ""
                        }
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Column(
        modifier = modifier
            .background(Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header: logo + name + channel number + close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceHighest),
                contentAlignment = Alignment.Center
            ) {
                if (channel.logoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(channel.name.take(2).uppercase(), style = MaterialTheme.typography.labelLarge, color = Primary)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = channel.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (channel.channelNumber > 0) {
                        Text(
                            text = "#${channel.channelNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextDisabled
                        )
                    }
                }
                Text(
                    text = "Group: ${channel.groupTitle}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
            Surface(
                onClick = onClose,
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = SurfaceElevated,
                    focusedContainerColor = Primary
                ),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.padding(8.dp).size(20.dp),
                    tint = TextPrimary
                )
            }
        }

        // Embedded ExoPlayer preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    androidx.media3.ui.PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        setKeepScreenOn(true)
                    }
                },
                modifier = Modifier.fillMaxSize().focusable(false)
            )
            // Mute/unmute toggle
            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
                Surface(
                    onClick = {
                        isMuted = !isMuted
                        exoPlayer.volume = if (isMuted) 0f else 1f
                    },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        focusedContainerColor = Primary
                    ),
                    border = ClickableSurfaceDefaults.border(
                        focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
                    )
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isMuted) "Unmute" else "Mute",
                        modifier = Modifier.padding(8.dp).size(20.dp),
                        tint = TextPrimary
                    )
                }
            }
        }

        // Stream info
        if (videoWidth > 0) {
            val infoText = buildString {
                append("${videoWidth}x${videoHeight}")
                if (videoCodec.isNotEmpty()) append(" \u00B7 $videoCodec")
                if (audioCodec.isNotEmpty()) append(" \u00B7 $audioCodec")
            }
            Text(
                text = infoText,
                style = MaterialTheme.typography.labelSmall,
                color = TextDisabled
            )
        }

        // Watch Full Screen button
        Surface(
            onClick = onWatch,
            modifier = Modifier.fillMaxWidth(),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Primary,
                focusedContainerColor = PrimaryVariant
            ),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(BorderStroke(2.dp, Glow))
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Watch Full Screen",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
            }
        }

        // Schedule section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = TextPrimary
            )
            Text(
                text = "Schedule",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
        }

        if (isScheduleLoading) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary, modifier = Modifier.size(32.dp))
            }
        } else if (schedule.isEmpty()) {
            Text(
                text = "No schedule available",
                style = MaterialTheme.typography.bodySmall,
                color = TextDisabled,
                modifier = Modifier.weight(1f)
            )
        } else {
            val now = System.currentTimeMillis()
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(schedule, key = { it.id }) { program ->
                    val isNow = now in program.startTime..program.endTime
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isNow) Primary.copy(alpha = 0.15f) else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${timeFormat.format(Date(program.startTime))}-${timeFormat.format(Date(program.endTime))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isNow) Primary else TextSecondary,
                            modifier = Modifier.width(90.dp)
                        )
                        Text(
                            text = program.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isNow) TextPrimary else TextSecondary,
                            fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (isNow) {
                            Text(
                                text = "NOW",
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ChannelCard(
    channel: Channel,
    isSelected: Boolean,
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
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) Primary.copy(alpha = 0.2f) else CardBackground,
            focusedContainerColor = SurfaceElevated
        ),
        border = ClickableSurfaceDefaults.border(
            border = if (isSelected) Border(BorderStroke(2.dp, Primary)) else Border.None,
            focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
        )
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
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Primary
                        )
                        Text(
                            text = "${timeFormat.format(Date(currentProgram.startTime))}\u2013${timeFormat.format(Date(currentProgram.endTime))}",
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
                        modifier = Modifier.fillMaxWidth().height(4.dp)
                            .clip(RoundedCornerShape(2.dp)).background(SurfaceHighest)
                    ) {
                        Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(Primary))
                    }

                    if (nextProgram != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = TextSecondary
                            )
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
            Icon(
                imageVector = if (channel.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = if (channel.isFavorite) "Favorited" else "Not favorited",
                modifier = Modifier.padding(8.dp).size(24.dp),
                tint = if (channel.isFavorite) Warning else TextSecondary
            )
        }
    }
}
