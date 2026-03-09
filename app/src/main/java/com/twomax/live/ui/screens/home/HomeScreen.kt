package com.twomax.live.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.tv.material3.*
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
                Text(text = "2maxLIVE", style = MaterialTheme.typography.displayLarge, color = Primary)
                Spacer(Modifier.height(16.dp))
                Text(text = "Set up your first provider to get started", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                Spacer(Modifier.height(24.dp))
                Surface(
                    onClick = { navController.navigate(Screen.Setup.route) },
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                    colors = ClickableSurfaceDefaults.colors(containerColor = Primary, focusedContainerColor = PrimaryVariant),
                    border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(2.dp, Glow))),
                    scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f)
                ) {
                    Text(text = "Add Provider", modifier = Modifier.padding(horizontal = 32.dp, vertical = 14.dp), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        // Compact header
        Row(
            modifier = Modifier.fillMaxWidth().height(40.dp).background(Surface).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "2maxLIVE", style = MaterialTheme.typography.labelLarge, color = Primary)
            Spacer(Modifier.weight(1f))
            if (state.activeProvider != null) {
                Text(text = state.activeProvider!!.name, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
        }

        // Three category navigation buttons
        Column(
            modifier = Modifier.weight(1f).padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CategoryButton(
                icon = "\uD83D\uDCFA",
                title = "Live TV",
                subtitle = "Watch live channels",
                onClick = { navController.navigate(Screen.LiveTv.route) }
            )
            CategoryButton(
                icon = "\uD83C\uDFAC",
                title = "Movies",
                subtitle = "Browse your movie library",
                onClick = { navController.navigate(Screen.Movies.route) }
            )
            CategoryButton(
                icon = "\uD83C\uDFAD",
                title = "TV Shows",
                subtitle = "Browse series & episodes",
                onClick = { navController.navigate(Screen.Series.route) }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun CategoryButton(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(100.dp),
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
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(text = icon, fontSize = 28.sp)
            Column {
                Text(text = title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}
