package com.twomax.live.ui.screens.epg

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import com.twomax.live.core.model.EpgProgram
import com.twomax.live.ui.navigation.Screen
import com.twomax.live.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
private const val PIXELS_PER_MINUTE = 4 // 4dp per minute
private const val CHANNEL_COLUMN_WIDTH = 160

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EpgScreen(
    navController: NavHostController,
    viewModel: EpgViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading EPG...", style = MaterialTheme.typography.titleLarge)
        }
        return
    }

    if (state.channels.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No EPG data available", style = MaterialTheme.typography.titleLarge, color = TextSecondary)
        }
        return
    }

    val scrollState = rememberScrollState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        // Header with time navigation
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("EPG Guide", style = MaterialTheme.typography.headlineLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    onClick = { viewModel.shiftTime(-3) },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                    colors = ClickableSurfaceDefaults.colors(containerColor = SurfaceElevated, focusedContainerColor = Primary),
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, FocusBorder)))
                ) {
                    Text("<< -3h", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
                }
                Surface(
                    onClick = { viewModel.shiftTime(3) },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
                    colors = ClickableSurfaceDefaults.colors(containerColor = SurfaceElevated, focusedContainerColor = Primary),
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, FocusBorder)))
                ) {
                    Text("+3h >>", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // Time header
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(CHANNEL_COLUMN_WIDTH.dp))
            Row(
                modifier = Modifier.horizontalScroll(scrollState)
            ) {
                val totalMinutes = ((state.endTimeMillis - state.startTimeMillis) / 60000).toInt()
                for (i in 0..totalMinutes step 30) {
                    val time = state.startTimeMillis + i * 60000L
                    Text(
                        text = timeFormat.format(Date(time)),
                        modifier = Modifier.width((30 * PIXELS_PER_MINUTE).dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // Channel rows with programs
        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(state.channels) { channel ->
                val programs = state.programs[channel.epgChannelId] ?: emptyList()

                Row(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Channel info
                    Row(
                        modifier = Modifier
                            .width(CHANNEL_COLUMN_WIDTH.dp)
                            .fillMaxHeight()
                            .background(Surface, RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (channel.logoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = channel.logoUrl,
                                contentDescription = channel.name,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = channel.name,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Programs timeline
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .horizontalScroll(scrollState)
                    ) {
                        if (programs.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .width(((state.endTimeMillis - state.startTimeMillis) / 60000 * PIXELS_PER_MINUTE).toInt().dp)
                                    .fillMaxHeight()
                                    .background(SurfaceElevated.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text("No data", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                            }
                        } else {
                            programs.forEach { program ->
                                ProgramBlock(
                                    program = program,
                                    startTimeMillis = state.startTimeMillis,
                                    currentTimeMillis = state.currentTimeMillis,
                                    onClick = {
                                        navController.navigate(Screen.Player.createRoute(channel.streamUrl, channel.name))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ProgramBlock(
    program: EpgProgram,
    startTimeMillis: Long,
    currentTimeMillis: Long,
    onClick: () -> Unit
) {
    val durationMinutes = ((program.endTime - program.startTime) / 60000).toInt().coerceAtLeast(1)
    val widthDp = (durationMinutes * PIXELS_PER_MINUTE).dp
    val isCurrentlyAiring = currentTimeMillis in program.startTime..program.endTime

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(widthDp)
            .fillMaxHeight()
            .padding(horizontal = 1.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(4.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isCurrentlyAiring) Primary.copy(alpha = 0.3f) else SurfaceElevated,
            focusedContainerColor = Primary.copy(alpha = 0.5f)
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = program.title,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrentlyAiring) TextPrimary else TextSecondary
            )
            Text(
                text = "${timeFormat.format(Date(program.startTime))} - ${timeFormat.format(Date(program.endTime))}",
                style = MaterialTheme.typography.labelSmall,
                color = TextDisabled
            )
        }
    }
}
