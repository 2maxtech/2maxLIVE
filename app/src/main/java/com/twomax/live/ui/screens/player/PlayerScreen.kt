package com.twomax.live.ui.screens.player

import android.net.Uri
import android.util.Log
import androidx.compose.material3.CircularProgressIndicator
import android.view.KeyEvent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.navigation.NavHostController
import androidx.tv.material3.*
import com.twomax.live.ui.theme.*
import kotlinx.coroutines.delay

data class TrackOption(val label: String, val groupIndex: Int, val trackIndex: Int)

enum class ResolutionMode(val label: String, val aspectRatio: Float?, val resizeMode: Int) {
    AUTO("Auto", null, androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT),
    RATIO_16_9("16:9", 16f / 9f, androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT),
    RATIO_16_10("16:10", 16f / 10f, androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT),
    RATIO_4_3("4:3", 4f / 3f, androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT),
    FILL("Fill", null, androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL)
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerScreen(
    streamUrl: String,
    streamTitle: String,
    navController: NavHostController
) {
    val context = LocalContext.current
    var showOverlay by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var playerError by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val playPauseFocusRequester = remember { FocusRequester() }

    // Progress tracking
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }

    // Track panel state
    var showSubtitlePanel by remember { mutableStateOf(false) }
    var showQualityPanel by remember { mutableStateOf(false) }
    var panelSelectedIndex by remember { mutableStateOf(0) }
    var subtitleTracks by remember { mutableStateOf<List<TrackOption>>(emptyList()) }
    var selectedSubtitle by remember { mutableStateOf(-1) } // groupIndex, -1 = off

    // Resolution state
    val resolutionModes = ResolutionMode.entries
    var selectedResolution by remember { mutableStateOf(ResolutionMode.AUTO) }
    var playerViewRef by remember { mutableStateOf<androidx.media3.ui.PlayerView?>(null) }

    val trackSelector = remember { DefaultTrackSelector(context) }

    val exoPlayer = remember {
        val httpDataSource = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 9; TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.164 Mobile Safari/537.36")
            .setConnectTimeoutMs(30000)
            .setReadTimeoutMs(60000)
            .setAllowCrossProtocolRedirects(true)

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                30_000,   // min buffer (30s for 4K)
                120_000,  // max buffer (2 min)
                3_000,    // buffer for playback start
                5_000     // buffer for playback after rebuffer
            )
            .setPrioritizeTimeOverSizeThresholds(false)
            .setBackBuffer(30_000, true)
            .build()

        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
            .setEnableDecoderFallback(true)

        ExoPlayer.Builder(context, renderersFactory)
            .setMediaSourceFactory(DefaultMediaSourceFactory(httpDataSource))
            .setLoadControl(loadControl)
            .setTrackSelector(trackSelector)
            .build()
            .apply {
                Log.d("PlayerScreen", "Playing: $streamUrl")
                val mediaItem = MediaItem.fromUri(Uri.parse(streamUrl))
                setMediaItem(mediaItem)
                playWhenReady = true
                setHandleAudioBecomingNoisy(true)
                setWakeMode(C.WAKE_MODE_NETWORK)
                prepare()
            }
    }

    // Auto-hide overlay after 10 seconds
    LaunchedEffect(showOverlay) {
        if (showOverlay) {
            delay(10000)
            showOverlay = false
        }
    }

    // Poll playback position while overlay is visible
    LaunchedEffect(showOverlay) {
        while (showOverlay) {
            currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
            duration = exoPlayer.duration.takeIf { it > 0 && it != Long.MIN_VALUE } ?: 0L
            delay(500)
        }
    }

    // Focus Play/Pause button when overlay appears
    LaunchedEffect(showOverlay) {
        if (showOverlay) {
            try { playPauseFocusRequester.requestFocus() } catch (_: Exception) {}
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Listen for playback state, errors, and track changes
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING || state == Player.STATE_IDLE
                Log.d("PlayerScreen", "State: $state isPlaying=$isPlaying")
            }
            override fun onPlayerError(error: PlaybackException) {
                Log.e("PlayerScreen", "Playback error: ${error.message} cause=${error.cause?.message}", error)
                playerError = error.message ?: "Unknown playback error"
                isBuffering = false
            }
            override fun onTracksChanged(tracks: Tracks) {
                // Collect subtitle tracks
                val subs = mutableListOf<TrackOption>()
                subs.add(TrackOption("Off", -1, -1))
                tracks.groups.forEachIndexed { groupIdx, group ->
                    if (group.type == C.TRACK_TYPE_TEXT) {
                        for (trackIdx in 0 until group.length) {
                            val format = group.getTrackFormat(trackIdx)
                            val lang = format.language?.uppercase() ?: "Sub ${groupIdx + 1}"
                            val label = if (format.label != null) "$lang – ${format.label}" else lang
                            subs.add(TrackOption(label, groupIdx, trackIdx))
                        }
                    }
                }
                subtitleTracks = subs
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                    if (showQualityPanel) {
                        val maxIndex = resolutionModes.size - 1
                        when (event.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_UP -> {
                                panelSelectedIndex = (panelSelectedIndex - 1).coerceAtLeast(0)
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                panelSelectedIndex = (panelSelectedIndex + 1).coerceAtMost(maxIndex)
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                                val mode = resolutionModes[panelSelectedIndex]
                                selectedResolution = mode
                                showQualityPanel = false
                                true
                            }
                            KeyEvent.KEYCODE_BACK -> {
                                showQualityPanel = false
                                true
                            }
                            else -> false
                        }
                    } else {
                        when (event.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                                if (!showOverlay) {
                                    showOverlay = true
                                    true  // consume only when overlay is hidden (to show it)
                                } else {
                                    false  // let the focused Surface button handle ENTER
                                }
                            }
                            KeyEvent.KEYCODE_BACK -> {
                                when {
                                    showOverlay -> {
                                        showOverlay = false
                                        true
                                    }
                                    else -> {
                                        navController.popBackStack()
                                        true
                                    }
                                }
                            }
                            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN,
                            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                if (!showOverlay) {
                                    // Show overlay on any DPAD press while hidden
                                    showOverlay = true
                                    true
                                } else {
                                    // Let Compose focus system handle DPAD navigation between buttons
                                    false
                                }
                            }
                            else -> false
                        }
                    }
                } else false
            }
    ) {
        // Video surface
        val aspectRatio = selectedResolution.aspectRatio
        val videoModifier = when {
            aspectRatio != null ->
                Modifier.fillMaxHeight().aspectRatio(aspectRatio)
            else ->
                Modifier.fillMaxSize()
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AndroidView(
                factory = { ctx ->
                    androidx.media3.ui.PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        setKeepScreenOn(true)
                    }.also { playerViewRef = it }
                },
                update = { view ->
                    view.setResizeMode(selectedResolution.resizeMode)
                },
                modifier = videoModifier
            )
        }

        // Buffering indicator
        if (isBuffering) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary, modifier = Modifier.size(64.dp))
            }
        }

        // Error display
        if (playerError != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.tv.material3.Text(
                    text = "Playback error: $playerError",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = androidx.compose.ui.Modifier.padding(32.dp)
                )
            }
        }

        // Overlay
        AnimatedVisibility(
            visible = showOverlay,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(Modifier.fillMaxSize()) {
                // Top gradient with title
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Text(
                        text = streamTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                }

                // Bottom gradient with controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Progress bar (only for non-live content)
                    if (duration > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                formatTime(currentPosition),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Text(
                                formatTime(duration),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                        val progress = (currentPosition.toFloat() / duration).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(SurfaceHighest)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxHeight()
                                    .background(Primary)
                            )
                        }
                    }

                    // Unified controls row: [-10s] [CC] [Play/Pause] [Resolution] [+10s]
                    val hasSubs = subtitleTracks.size > 1
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rewind
                        Surface(
                            onClick = { exoPlayer.seekTo(maxOf(0, exoPlayer.currentPosition - 10000)) },
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = SurfaceElevated.copy(alpha = 0.7f),
                                focusedContainerColor = Primary
                            ),
                            border = ClickableSurfaceDefaults.border(
                                focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
                            )
                        ) {
                            Text(
                                text = "-10s",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        // CC button
                        Surface(
                            onClick = {
                                if (hasSubs) {
                                    showQualityPanel = false
                                    showSubtitlePanel = !showSubtitlePanel
                                }
                            },
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = when {
                                    !hasSubs -> SurfaceElevated.copy(alpha = 0.3f)
                                    selectedSubtitle != -1 -> Primary.copy(alpha = 0.6f)
                                    else -> SurfaceElevated.copy(alpha = 0.6f)
                                },
                                focusedContainerColor = if (hasSubs) Primary else SurfaceElevated.copy(alpha = 0.4f)
                            ),
                            border = ClickableSurfaceDefaults.border(
                                focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
                            )
                        ) {
                            Text(
                                text = "CC",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (hasSubs) TextPrimary else TextDisabled
                            )
                        }

                        // Play/Pause
                        Surface(
                            onClick = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
                            modifier = Modifier.focusRequester(playPauseFocusRequester),
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = Primary,
                                focusedContainerColor = PrimaryVariant
                            ),
                            border = ClickableSurfaceDefaults.border(
                                focusedBorder = Border(BorderStroke(2.dp, Glow))
                            ),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.1f)
                        ) {
                            Text(
                                text = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        // Resolution button
                        Surface(
                            onClick = {
                                showSubtitlePanel = false
                                showQualityPanel = !showQualityPanel
                            },
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = if (selectedResolution != ResolutionMode.AUTO)
                                    Primary.copy(alpha = 0.6f)
                                else
                                    SurfaceElevated.copy(alpha = 0.6f),
                                focusedContainerColor = Primary
                            ),
                            border = ClickableSurfaceDefaults.border(
                                focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
                            )
                        ) {
                            Text(
                                text = selectedResolution.label,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary
                            )
                        }

                        // Fast forward
                        Surface(
                            onClick = { exoPlayer.seekTo(exoPlayer.currentPosition + 10000) },
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = SurfaceElevated.copy(alpha = 0.7f),
                                focusedContainerColor = Primary
                            ),
                            border = ClickableSurfaceDefaults.border(
                                focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
                            )
                        ) {
                            Text(
                                text = "+10s",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                // Resolution panel
                if (showQualityPanel) {
                    TrackSelectionPanel(
                        title = "Resolution",
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 120.dp),
                        options = resolutionModes.map { it.label },
                        selectedIndex = panelSelectedIndex
                    )
                }
            }
        }

        // Subtitle selection dialog
        if (showSubtitlePanel && subtitleTracks.size > 1) {
            Dialog(
                onDismissRequest = { showSubtitlePanel = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SubtitleSelectionDialog(
                        tracks = subtitleTracks,
                        selectedGroupIndex = selectedSubtitle,
                        onSelect = { track ->
                            selectedSubtitle = track.groupIndex
                            val params = exoPlayer.trackSelectionParameters.buildUpon()
                            if (track.groupIndex == -1) {
                                params.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                            } else {
                                params.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                                params.setPreferredTextLanguage(
                                    exoPlayer.currentTracks.groups
                                        .getOrNull(track.groupIndex)
                                        ?.getTrackFormat(track.trackIndex)
                                        ?.language
                                )
                            }
                            exoPlayer.trackSelectionParameters = params.build()
                            showSubtitlePanel = false
                        },
                        onDismiss = { showSubtitlePanel = false }
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(showQualityPanel) {
        if (showQualityPanel) {
            panelSelectedIndex = resolutionModes.indexOf(selectedResolution).coerceAtLeast(0)
            focusRequester.requestFocus()
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SubtitleSelectionDialog(
    tracks: List<TrackOption>,
    selectedGroupIndex: Int,
    onSelect: (TrackOption) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(320.dp)
            .wrapContentHeight()
            .background(color = Color(0xF0161628), shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                "Select Subtitle",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(tracks) { track ->
                    val isSelected = track.groupIndex == selectedGroupIndex
                    Surface(
                        onClick = { onSelect(track) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (isSelected) Primary.copy(alpha = 0.3f) else Color.Transparent,
                            focusedContainerColor = Primary
                        ),
                        border = ClickableSurfaceDefaults.border(
                            focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSelected) "✓  ${track.label}" else "    ${track.label}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) Primary else TextPrimary
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
private fun TrackSelectionPanel(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(220.dp)
            .background(
                color = Color(0xE6161628),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                items(options.indices.toList()) { index ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isSelected) Primary.copy(alpha = 0.3f) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = options[index],
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) Primary else TextPrimary
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
