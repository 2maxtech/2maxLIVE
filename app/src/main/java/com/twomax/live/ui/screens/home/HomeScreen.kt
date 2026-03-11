package com.twomax.live.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.tv.material3.*
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.twomax.live.ui.navigation.Screen
import com.twomax.live.ui.theme.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (!state.hasProviders && !state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "No playlists found", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Text(text = "Manage your playlists at 2maxplayer.com", style = MaterialTheme.typography.bodyMedium, color = TextDisabled)
                Spacer(Modifier.height(4.dp))
                Text(text = "Restart the app after adding playlists", style = MaterialTheme.typography.bodySmall, color = TextDisabled)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Compact header
        item {
            Row(
                modifier = Modifier.fillMaxWidth().height(40.dp).background(Surface).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))
                if (state.activeProvider != null) {
                    Text(text = state.activeProvider!!.name, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                }
            }
        }

        // Quick-nav row
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickNavButton(
                    icon = Icons.Default.LiveTv,
                    title = "Live TV",
                    onClick = { navController.navigate(Screen.LiveTv.route) },
                    modifier = Modifier.weight(1f)
                )
                QuickNavButton(
                    icon = Icons.Default.Movie,
                    title = "Movies",
                    onClick = { navController.navigate(Screen.Movies.route) },
                    modifier = Modifier.weight(1f)
                )
                QuickNavButton(
                    icon = Icons.Default.VideoLibrary,
                    title = "TV Shows",
                    onClick = { navController.navigate(Screen.Series.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Movies row
        if (state.movies.isNotEmpty()) {
            item {
                Text(
                    text = "Movies",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 12.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.movies, key = { it.id }) { movie ->
                        PosterCard(
                            title = movie.name,
                            imageUrl = movie.coverUrl,
                            onClick = { navController.navigate(Screen.MovieDetail.createRoute(movie.id)) }
                        )
                    }
                }
            }
        }

        // TV Shows row
        if (state.series.isNotEmpty()) {
            item {
                Text(
                    text = "TV Shows",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 12.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.series, key = { it.id }) { series ->
                        PosterCard(
                            title = series.name,
                            imageUrl = series.coverUrl,
                            onClick = { navController.navigate(Screen.SeriesDetail.createRoute(series.id)) }
                        )
                    }
                }
            }
        }

        // Favorites row
        if (state.favoriteMovies.isNotEmpty()) {
            item {
                Text(
                    text = "Favorites",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 12.dp)
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.favoriteMovies, key = { "fav_${it.id}" }) { movie ->
                        PosterCard(
                            title = movie.name,
                            imageUrl = movie.coverUrl,
                            onClick = { navController.navigate(Screen.MovieDetail.createRoute(movie.id)) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun QuickNavButton(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(16.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = SurfaceElevated,
            focusedContainerColor = Primary
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.02f)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp), tint = Primary)
            Spacer(Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PosterCard(
    title: String,
    imageUrl: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(120.dp).height(180.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = CardBackground,
            focusedContainerColor = SurfaceElevated
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(SurfaceHighest),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    ) {
                        when (painter.state) {
                            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                            is AsyncImagePainter.State.Error -> {
                                Text(title.take(2).uppercase(), style = MaterialTheme.typography.titleMedium, color = Primary)
                            }
                            else -> {
                                Box(Modifier.fillMaxSize().background(SurfaceHighest))
                            }
                        }
                    }
                } else {
                    Text(title.take(2).uppercase(), style = MaterialTheme.typography.titleMedium, color = Primary)
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(6.dp)
            )
        }
    }
}
