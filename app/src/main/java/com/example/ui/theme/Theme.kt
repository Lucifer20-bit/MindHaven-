package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SageLightColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = SageOnPrimary,
    primaryContainer = SagePrimaryContainer,
    onPrimaryContainer = SageOnPrimaryContainer,
    secondary = SageSecondary,
    secondaryContainer = SageSecondaryContainer,
    onSecondaryContainer = SageOnSecondaryContainer,
    tertiary = SageTertiary,
    tertiaryContainer = SageTertiaryContainer,
    onTertiaryContainer = SageOnTertiaryContainer,
    background = SageBackground,
    onBackground = SageOnBackground,
    surface = SageSurface,
    onSurface = SageOnSurface,
    surfaceVariant = SageSurfaceVariant,
    onSurfaceVariant = SageOnSurfaceVariant,
    outline = SageOutline,
    outlineVariant = SageOutlineVariant
)

private val SageDarkColorScheme = darkColorScheme(
    primary = Color(0xFF96D4B7),
    onPrimary = Color(0xFF033824),
    primaryContainer = Color(0xFF1E4E3B),
    onPrimaryContainer = Color(0xFFD6EAE0),
    secondary = Color(0xFFB1CCC0),
    onSecondary = Color(0xFF1D352B),
    secondaryContainer = Color(0xFF344D42),
    onSecondaryContainer = Color(0xFFD9E7E0),
    background = Color(0xFF111814),
    onBackground = Color(0xFFE1E8E3),
    surface = Color(0xFF17201B),
    onSurface = Color(0xFFE1E8E3),
    surfaceVariant = Color(0xFF28362F),
    onSurfaceVariant = Color(0xFFB9CAC1)
)

private val LavenderLightColorScheme = lightColorScheme(
    primary = LavenderPrimary,
    onPrimary = LavenderOnPrimary,
    primaryContainer = LavenderPrimaryContainer,
    onPrimaryContainer = LavenderOnPrimaryContainer,
    secondary = Color(0xFF625B71),
    secondaryContainer = Color(0xFFE8DEF8),
    background = LavenderBackground,
    surface = LavenderSurface,
    surfaceVariant = LavenderSurfaceVariant
)

private val RoseLightColorScheme = lightColorScheme(
    primary = RosePrimary,
    onPrimary = RoseOnPrimary,
    primaryContainer = RosePrimaryContainer,
    onPrimaryContainer = RoseOnPrimaryContainer,
    secondary = Color(0xFF74575B),
    secondaryContainer = Color(0xFFFFD9DD),
    background = RoseBackground,
    surface = RoseSurface,
    surfaceVariant = RoseSurfaceVariant
)

@Composable
fun MindHavenTheme(
    themeMode: String = "gentle_sage",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> SageDarkColorScheme
        themeMode == "lavender_dusk" -> LavenderLightColorScheme
        themeMode == "rose_dawn" -> RoseLightColorScheme
        else -> SageLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
