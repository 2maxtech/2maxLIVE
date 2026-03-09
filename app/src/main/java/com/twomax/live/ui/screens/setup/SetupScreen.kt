package com.twomax.live.ui.screens.setup

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.tv.material3.*
import com.twomax.live.data.sync.SyncEngine
import com.twomax.live.ui.input.TvInputActivity
import com.twomax.live.ui.theme.*

private enum class EditingField { NAME, SERVER, USERNAME, PASSWORD }

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SetupScreen(
    navController: NavHostController,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Which field is waiting for input from TvInputActivity
    var pendingField by remember { mutableStateOf<EditingField?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val value = result.data?.getStringExtra(TvInputActivity.RESULT_VALUE) ?: ""
            when (pendingField) {
                EditingField.NAME     -> viewModel.updateProviderName(value)
                EditingField.SERVER   -> viewModel.updateXtreamServer(value)
                EditingField.USERNAME -> viewModel.updateXtreamUsername(value)
                EditingField.PASSWORD -> viewModel.updateXtreamPassword(value)
                null -> Unit
            }
        }
        pendingField = null
    }

    fun openInput(field: EditingField, title: String, current: String, hint: String = "", isPassword: Boolean = false) {
        pendingField = field
        val intent = Intent(context, TvInputActivity::class.java).apply {
            putExtra(TvInputActivity.EXTRA_TITLE, title)
            putExtra(TvInputActivity.EXTRA_VALUE, current)
            putExtra(TvInputActivity.EXTRA_HINT, hint)
            putExtra(TvInputActivity.EXTRA_IS_PASSWORD, isPassword)
        }
        launcher.launch(intent)
    }

    LaunchedEffect(uiState.syncResult) {
        if (uiState.syncResult is SyncEngine.SyncResult.Success) {
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Plain Box — NOT a TV Surface/ClickableSurface.
        // A TV Surface captures all D-pad focus and highlights the whole card white,
        // preventing inner field rows from ever receiving individual focus.
        Box(
            modifier = Modifier
                .width(560.dp)
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceElevated)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Xtream Codes",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )

                Spacer(Modifier.height(4.dp))

                FieldRow(
                    label = "Name (optional)",
                    value = uiState.providerName,
                    placeholder = "My Playlist",
                    onClick = { openInput(EditingField.NAME, "Name (optional)", uiState.providerName, "My Playlist") }
                )
                FieldRow(
                    label = "Server URL",
                    value = uiState.xtreamServer,
                    placeholder = "http://example.com:8080",
                    onClick = { openInput(EditingField.SERVER, "Server URL", uiState.xtreamServer, "http://example.com:8080") }
                )
                FieldRow(
                    label = "Username",
                    value = uiState.xtreamUsername,
                    onClick = { openInput(EditingField.USERNAME, "Username", uiState.xtreamUsername) }
                )
                FieldRow(
                    label = "Password",
                    value = uiState.xtreamPassword,
                    isPassword = true,
                    onClick = { openInput(EditingField.PASSWORD, "Password", uiState.xtreamPassword, isPassword = true) }
                )

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = Error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.colors(
                            containerColor = Surface,
                            contentColor = TextSecondary,
                            focusedContainerColor = SurfaceHighest,
                            focusedContentColor = TextPrimary
                        )
                    ) {
                        Text("Cancel", modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                    }

                    Button(
                        onClick = {
                            if (!uiState.isLoading) {
                                if (uiState.xtreamServer.isNotBlank() &&
                                    uiState.xtreamUsername.isNotBlank() &&
                                    uiState.xtreamPassword.isNotBlank()
                                ) viewModel.addProvider()
                                else viewModel.showValidationError(SetupType.XTREAM)
                            }
                        },
                        colors = ButtonDefaults.colors(
                            containerColor = Primary,
                            contentColor = TextPrimary,
                            focusedContainerColor = PrimaryVariant,
                            focusedContentColor = TextPrimary
                        )
                    ) {
                        if (uiState.isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = TextPrimary,
                                    strokeWidth = 2.dp
                                )
                                Text("Connecting...", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                            }
                        } else {
                            Text("Connect", modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun FieldRow(
    label: String,
    value: String,
    placeholder: String = "",
    isPassword: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Background,
            focusedContainerColor = Background
        ),
        border = ClickableSurfaceDefaults.border(
            border = Border(BorderStroke(1.dp, Divider)),
            focusedBorder = Border(BorderStroke(2.dp, Primary))
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Primary
            )
            Spacer(Modifier.height(2.dp))
            val display = when {
                value.isEmpty() -> placeholder.ifEmpty { "Press OK to enter" }
                isPassword -> "•".repeat(value.length.coerceAtMost(24))
                else -> value
            }
            Text(
                text = display,
                style = MaterialTheme.typography.bodyMedium,
                color = if (value.isEmpty()) TextDisabled else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
