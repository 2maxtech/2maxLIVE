package com.twomax.live

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.twomax.live.data.sync.RemotePlaylistSync
import com.twomax.live.data.sync.SyncStatus
import com.twomax.live.ui.components.SideNav
import com.twomax.live.ui.navigation.AppNavigation
import com.twomax.live.ui.navigation.Screen
import com.twomax.live.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var remotePlaylistSync: RemotePlaylistSync

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check cached activation status to pick start destination
        val isActivated = getSharedPreferences("activation", Context.MODE_PRIVATE)
            .getBoolean("is_activated", false)
        val startDest = if (isActivated) Screen.Splash.route else Screen.Activation.route

        setContent {
            TwoMaxTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val syncState by remotePlaylistSync.syncState.collectAsStateWithLifecycle()

                // Auto-dismiss success/error after 3 seconds
                LaunchedEffect(syncState.status) {
                    if (syncState.status == SyncStatus.SUCCESS || syncState.status == SyncStatus.ERROR) {
                        delay(3000)
                        remotePlaylistSync.clearSyncState()
                    }
                }

                // Hide nav rail on player, setup, and activation screens
                val showNav = currentRoute != null &&
                    !currentRoute.startsWith("player") &&
                    currentRoute != Screen.Setup.route &&
                    currentRoute != Screen.Activation.route &&
                    currentRoute != Screen.Splash.route

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (showNav) {
                            SideNav(
                                currentRoute = currentRoute,
                                onNavigate = { screen ->
                                    if (screen == Screen.Home) {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(0) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        navController.navigate(screen.route) {
                                            popUpTo(Screen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                        AppNavigation(
                            navController = navController,
                            startDestination = startDest,
                            remotePlaylistSync = remotePlaylistSync,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Sync status banner
                    @OptIn(ExperimentalTvMaterial3Api::class)
                    AnimatedVisibility(
                        visible = syncState.status != SyncStatus.IDLE,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        val bgColor = when (syncState.status) {
                            SyncStatus.SYNCING -> Primary
                            SyncStatus.SUCCESS -> Success
                            SyncStatus.ERROR -> Error
                            else -> Primary
                        }
                        val textColor = when (syncState.status) {
                            SyncStatus.SUCCESS -> Background
                            else -> TextPrimary
                        }
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .background(bgColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = syncState.message,
                                color = textColor,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
