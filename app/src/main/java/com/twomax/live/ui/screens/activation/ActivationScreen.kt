package com.twomax.live.ui.screens.activation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun ActivationScreen(
    navController: NavHostController,
    viewModel: ActivationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var hasNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.startPolling()
    }

    // Navigate to Home when activated AND sync is done
    if (state.isActivated && !state.isSyncing && !hasNavigated) {
        hasNavigated = true
        LaunchedEffect(Unit) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Activation.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.align(Alignment.Center).padding(48.dp)
        ) {
            Text(
                text = "Activate Your Device",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Spacer(Modifier.height(16.dp))

            // MAC Address display
            Text(
                text = "Your Device ID",
                fontSize = 16.sp,
                color = TextSecondary
            )

            Text(
                text = state.macAddress,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary,
                letterSpacing = 4.sp
            )

            Spacer(Modifier.height(8.dp))

            // Instructions
            Text(
                text = "Visit 2maxplayer.com/activation on your phone or computer\nand enter the Device ID above to activate",
                fontSize = 18.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )

            Spacer(Modifier.height(16.dp))

            // Status indicator
            if (state.isChecking || state.isSyncing) {
                Text(
                    text = if (state.isSyncing) "Syncing playlists..." else "Checking activation...",
                    fontSize = 16.sp,
                    color = Primary
                )
            } else if (state.error != null) {
                Text(
                    text = state.error!!,
                    fontSize = 16.sp,
                    color = Error
                )
            }

            // Check Activation button
            Surface(
                onClick = { viewModel.checkActivation() },
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = Primary,
                    focusedContainerColor = PrimaryVariant
                ),
                border = ClickableSurfaceDefaults.border(
                    focusedBorder = Border(BorderStroke(3.dp, FocusBorder))
                ),
                modifier = Modifier.width(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.isChecking) "Checking..." else "Check Activation",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Auto-poll notice
            Text(
                text = "Auto-checking every 15 seconds",
                fontSize = 14.sp,
                color = TextDisabled
            )

        }
    }
}
