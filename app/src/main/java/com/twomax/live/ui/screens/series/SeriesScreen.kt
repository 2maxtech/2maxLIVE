package com.twomax.live.ui.screens.series

import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.twomax.live.ui.navigation.Screen
import com.twomax.live.ui.theme.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeriesSortChip(
    label: String,
    order: SeriesSortOrder,
    currentOrder: SeriesSortOrder,
    onClick: (SeriesSortOrder) -> Unit
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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SeriesScreen(
    navController: NavHostController,
    viewModel: SeriesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
            Text("Loading...", style = MaterialTheme.typography.titleLarge, color = TextSecondary)
        }
        return
    }

    if (uiState.groups.isEmpty() && uiState.seriesList.isEmpty()) {
        Box(Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
            Text("No series found.", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
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
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, FocusBorder)))
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

        // Series grid
        if (uiState.seriesList.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                Text("No series in this category", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
            }
        } else {
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                // Sort bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SeriesSortChip("Default", SeriesSortOrder.DEFAULT, uiState.sortOrder) { viewModel.setSortOrder(it) }
                    SeriesSortChip("A-Z", SeriesSortOrder.A_Z, uiState.sortOrder) { viewModel.setSortOrder(it) }
                    SeriesSortChip("Z-A", SeriesSortOrder.Z_A, uiState.sortOrder) { viewModel.setSortOrder(it) }
                }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.seriesList, key = { it.id }) { series ->
                    SeriesCard(
                        series = series,
                        onClick = {
                            // For M3U series, play the stream directly
                            navController.navigate(Screen.Player.createRoute(series.streamUrl, series.name))
                        },
                        onFavoriteToggle = { viewModel.toggleFavorite(series) }
                    )
                }
            }
            } // close Column wrapping sort bar + grid
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeriesCard(series: Channel, onClick: () -> Unit, onFavoriteToggle: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by remember { derivedStateOf { if (isFocused) 1.05f else 1f } }
    var longPressTriggered by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
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
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(containerColor = CardBackground, focusedContainerColor = SurfaceElevated),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, FocusBorder)))
    ) {
        Column(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)).background(SurfaceHighest),
                contentAlignment = Alignment.Center
            ) {
                if (series.logoUrl.isNotEmpty()) {
                    AsyncImage(model = series.logoUrl, contentDescription = series.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Text(series.name.take(2).uppercase(), style = MaterialTheme.typography.headlineMedium, color = Primary)
                }
                // Favorite status indicator (toggle via long-press ENTER)
                Text(
                    text = if (series.isFavorite) "\u2605" else "\u2606",
                    color = if (series.isFavorite) Warning else TextSecondary,
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = series.name,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
