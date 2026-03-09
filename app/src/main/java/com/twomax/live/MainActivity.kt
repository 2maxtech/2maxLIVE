package com.twomax.live

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.twomax.live.ui.components.SideNav
import com.twomax.live.ui.navigation.AppNavigation
import com.twomax.live.ui.navigation.Screen
import com.twomax.live.ui.theme.Background
import com.twomax.live.ui.theme.TwoMaxTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TwoMaxTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Hide nav rail on player and setup screens
                val showNav = currentRoute != null &&
                    !currentRoute.startsWith("player") &&
                    currentRoute != Screen.Setup.route

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background)
                ) {
                    if (showNav) {
                        SideNav(
                            currentRoute = currentRoute,
                            onNavigate = { screen ->
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    AppNavigation(
                        navController = navController,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
