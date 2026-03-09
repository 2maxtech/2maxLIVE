package com.twomax.live.ui.screens.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.tv.material3.*
import com.twomax.live.core.model.Channel
import com.twomax.live.core.model.Movie
import com.twomax.live.core.model.Series
import com.twomax.live.ui.navigation.Screen
import com.twomax.live.ui.theme.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceElevated, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(Primary),
                singleLine = true,
                decorationBox = { inner ->
                    if (uiState.query.isEmpty()) {
                        Text(
                            text = "Enter a title to search...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextDisabled
                        )
                    }
                    inner()
                }
            )
        }

        val hasResults = uiState.channels.isNotEmpty() || uiState.movies.isNotEmpty() || uiState.series.isNotEmpty()

        when {
            uiState.query.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Search across Live TV, Movies, and TV Shows",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }
            uiState.isSearching -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Searching...", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                }
            }
            !hasResults -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No results for \"${uiState.query}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (uiState.channels.isNotEmpty()) {
                        item {
                            SectionHeader("Live TV")
                        }
                        items(uiState.channels) { channel ->
                            SearchResultRow(
                                name = channel.name,
                                badge = "LIVE",
                                onClick = { navController.navigate(Screen.Player.createRoute(channel.streamUrl, channel.name)) }
                            )
                        }
                    }
                    if (uiState.movies.isNotEmpty()) {
                        item {
                            SectionHeader("Movies")
                        }
                        items(uiState.movies) { movie ->
                            SearchResultRow(
                                name = movie.name,
                                badge = "MOVIE",
                                onClick = { navController.navigate(Screen.Player.createRoute(movie.streamUrl, movie.name)) }
                            )
                        }
                    }
                    if (uiState.series.isNotEmpty()) {
                        item {
                            SectionHeader("TV Shows")
                        }
                        items(uiState.series) { series ->
                            SearchResultRow(
                                name = series.name,
                                badge = "SERIES",
                                onClick = { navController.navigate(Screen.SeriesDetail.createRoute(series.id)) }
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
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = Primary,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SearchResultRow(
    name: String,
    badge: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = SurfaceElevated,
            focusedContainerColor = Primary
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(BorderStroke(2.dp, FocusBorder))
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .background(SurfaceHighest, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }
    }
}
