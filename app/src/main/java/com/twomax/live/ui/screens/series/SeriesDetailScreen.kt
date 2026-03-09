package com.twomax.live.ui.screens.series

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import com.twomax.live.ui.navigation.Screen
import com.twomax.live.ui.theme.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SeriesDetailScreen(
    seriesId: Long,
    navController: NavHostController,
    viewModel: SeriesDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...", style = MaterialTheme.typography.titleLarge)
        }
        return
    }

    val series = state.series ?: return
    val seasonEpisodes = state.episodes.filter { it.seasonNumber == state.selectedSeason }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Series info header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AsyncImage(
                    model = series.coverUrl,
                    contentDescription = series.name,
                    modifier = Modifier
                        .width(200.dp)
                        .aspectRatio(0.67f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = series.name, style = MaterialTheme.typography.headlineLarge)
                    if (series.genre.isNotEmpty()) {
                        Text(text = series.genre, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                    }
                    if (series.rating > 0) {
                        Text(text = "Rating: ${series.rating}", style = MaterialTheme.typography.bodyMedium, color = Warning)
                    }
                    if (series.plot.isNotEmpty()) {
                        Text(
                            text = series.plot,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Season selector
        item {
            Text("Seasons", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.seasons) { season ->
                    val isSelected = season == state.selectedSeason
                    Surface(
                        onClick = { viewModel.selectSeason(season) },
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(20.dp)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = if (isSelected) Primary else SurfaceElevated,
                            focusedContainerColor = if (isSelected) Primary else SurfaceHighest
                        ),
                        border = ClickableSurfaceDefaults.border(
                            focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
                        )
                    ) {
                        Text(
                            text = "Season $season",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Episodes list
        item {
            Text("Episodes", style = MaterialTheme.typography.titleLarge)
        }

        items(seasonEpisodes) { episode ->
            Surface(
                onClick = {
                    navController.navigate(
                        Screen.Player.createRoute(episode.streamUrl, episode.title.ifEmpty { "Episode ${episode.episodeNumber}" })
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = CardBackground,
                    focusedContainerColor = SurfaceElevated
                ),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "E${episode.episodeNumber}", style = MaterialTheme.typography.titleMedium, color = Primary)
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = episode.title.ifEmpty { "Episode ${episode.episodeNumber}" },
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (episode.plot.isNotEmpty()) {
                            Text(
                                text = episode.plot,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (episode.duration.isNotEmpty()) {
                        Text(text = episode.duration, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    }
                }
            }
        }
    }
}
