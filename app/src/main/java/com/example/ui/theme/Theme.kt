package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SageGreen,
    onPrimary = ForestDeep,
    primaryContainer = ForestDeep,
    onPrimaryContainer = SageSoft,
    secondary = SageGreen,
    onSecondary = ForestDeep,
    tertiary = AmberYellow,
    onTertiary = Color.Black,
    background = Color(0xFF10201C), // Calm deep slate-teal
    onBackground = Color(0xFFE7F1EE),
    surface = Color(0xFF172924),
    onSurface = Color(0xFFE7F1EE),
    error = TerraRed,
    errorContainer = Color(0xFF3A1E1B),
    onError = Color.White,
    outline = Color(0xFF2A3B36)
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = SageSoft,
    onPrimaryContainer = ForestDeep,
    secondary = SageGreen,
    onSecondary = ForestDeep,
    tertiary = AmberYellow,
    onTertiary = Color.White,
    background = CreamBg,
    onBackground = InkDark,
    surface = CardWhite,
    onSurface = InkDark,
    error = TerraRed,
    errorContainer = TerraSoft,
    onError = Color.White,
    onErrorContainer = TerraRed,
    outline = LineDivider
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
