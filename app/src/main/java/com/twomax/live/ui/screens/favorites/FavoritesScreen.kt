package com.twomax.live.ui.screens.favorites

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
fun FavoritesScreen(
    navController: NavHostController,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.channels.isEmpty() && state.movies.isEmpty() && !state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No favorites yet", style = MaterialTheme.typography.titleLarge, color = TextSecondary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text("Favorites", style = MaterialTheme.typography.headlineLarge)
        }

        if (state.channels.isNotEmpty()) {
            item {
                Text("Live Channels", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.channels, key = { it.id }) { channel ->
                        Surface(
                            onClick = { navController.navigate(Screen.Player.createRoute(channel.streamUrl, channel.name)) },
                            modifier = Modifier.width(180.dp).height(120.dp),
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                            colors = ClickableSurfaceDefaults.colors(containerColor = CardBackground, focusedContainerColor = SurfaceElevated),
                            border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, FocusBorder))),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (channel.logoUrl.isNotEmpty()) {
                                    AsyncImage(model = channel.logoUrl, contentDescription = channel.name, modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(8.dp))
                                }
                                Text(text = channel.name, style = MaterialTheme.typography.labelLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        if (state.movies.isNotEmpty()) {
            item {
                Text("Movies", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.movies, key = { it.id }) { movie ->
                        Surface(
                            onClick = { navController.navigate(Screen.Player.createRoute(movie.streamUrl, movie.name)) },
                            modifier = Modifier.width(140.dp).aspectRatio(0.67f),
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                            colors = ClickableSurfaceDefaults.colors(containerColor = CardBackground, focusedContainerColor = SurfaceElevated),
                            border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, FocusBorder))),
                            scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
                        ) {
                            Column {
                                AsyncImage(
                                    model = movie.coverUrl, contentDescription = movie.name,
                                    modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Text(text = movie.name, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }
}
