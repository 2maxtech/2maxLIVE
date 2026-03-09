package com.twomax.live.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
private val DarkPurpleScheme = darkColorScheme(
    primary = Primary,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryVariant,
    secondary = Secondary,
    onSecondary = TextPrimary,
    secondaryContainer = SecondaryVariant,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = TextPrimary
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TwoMaxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkPurpleScheme,
        typography = TwoMaxTypography,
        content = content
    )
}
