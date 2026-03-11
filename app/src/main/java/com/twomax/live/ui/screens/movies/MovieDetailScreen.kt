package com.twomax.live.ui.screens.movies

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
fun MovieDetailScreen(
    movieId: Long,
    navController: NavHostController,
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
            Text("Loading...", style = MaterialTheme.typography.titleLarge, color = TextSecondary)
        }
        return
    }

    val movie = state.movie ?: run {
        Box(Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
            Text("Movie not found", style = MaterialTheme.typography.titleLarge, color = TextSecondary)
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxSize().background(Background).padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Left side: Poster + Play button (always visible)
        Column(
            modifier = Modifier.width(220.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val coverUrl = state.coverBig.ifEmpty { movie.coverUrl }
            AsyncImage(
                model = coverUrl,
                contentDescription = movie.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.67f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceHighest),
                contentScale = ContentScale.Crop
            )

            // Play button - always visible next to poster
            Surface(
                onClick = {
                    navController.navigate(
                        Screen.Player.createRoute(movie.streamUrl, movie.name)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Primary,
                    focusedContainerColor = PrimaryVariant
                ),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(BorderStroke(2.dp, Glow))
                ),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\u25B6  Play",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Right side: scrollable info
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            item {
                Text(
                    text = movie.name,
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary
                )
            }

            // Year, Duration, Genre
            item {
                val metaItems = buildList {
                    if (state.year.isNotEmpty()) add(state.year)
                    if (state.duration.isNotEmpty()) add(state.duration)
                    if (state.genre.isNotEmpty()) add(state.genre)
                }
                if (metaItems.isNotEmpty()) {
                    Text(
                        text = metaItems.joinToString(" \u00B7 "),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
                if (state.country.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = state.country,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Rating badges
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state.imdbRating.isNotEmpty()) {
                        RatingBadge(
                            label = "IMDb",
                            value = "${state.imdbRating}/10",
                            backgroundColor = Color(0xFFF5C518),
                            textColor = Color.Black
                        )
                    }
                    if (state.rtRating.isNotEmpty()) {
                        RatingBadge(
                            label = "RT",
                            value = state.rtRating,
                            backgroundColor = Color(0xFFFA3200),
                            textColor = Color.White
                        )
                    }
                    if (state.rating5based > 0f) {
                        Text(
                            text = "\u2605 ${"%.1f".format(state.rating5based)}/5",
                            style = MaterialTheme.typography.titleMedium,
                            color = Warning,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            // Plot / Synopsis
            if (state.plot.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text("Synopsis", style = MaterialTheme.typography.titleLarge, color = Primary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = state.plot,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Cast & Director
            if (state.director.isNotEmpty() || state.cast.isNotEmpty()) {
                item {
                    Text("Cast & Crew", style = MaterialTheme.typography.titleLarge, color = Primary)
                    Spacer(Modifier.height(4.dp))
                    if (state.director.isNotEmpty()) {
                        Text(
                            text = "Director: ${state.director}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                    if (state.cast.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Cast: ${state.cast}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun RatingBadge(
    label: String,
    value: String,
    backgroundColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
