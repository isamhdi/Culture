package com.culture.tracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.culture.tracker.data.repository.ThemeMode

private val LightColors = lightColorScheme(
    primary = GreenPrimaryLight,
    onPrimary = GreenOnPrimaryLight,
    primaryContainer = GreenPrimaryContainerLight,
    onPrimaryContainer = GreenOnPrimaryContainerLight,
    secondary = EarthSecondaryLight,
    onSecondary = EarthOnSecondaryLight,
    secondaryContainer = EarthSecondaryContainerLight,
    onSecondaryContainer = EarthOnSecondaryContainerLight,
    tertiary = SunTertiaryLight,
    onTertiary = SunOnTertiaryLight,
    tertiaryContainer = SunTertiaryContainerLight,
    onTertiaryContainer = SunOnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
)

private val DarkColors = darkColorScheme(
    primary = GreenPrimaryDark,
    onPrimary = GreenOnPrimaryDark,
    primaryContainer = GreenPrimaryContainerDark,
    onPrimaryContainer = GreenOnPrimaryContainerDark,
    secondary = EarthSecondaryDark,
    onSecondary = EarthOnSecondaryDark,
    secondaryContainer = EarthSecondaryContainerDark,
    onSecondaryContainer = EarthOnSecondaryContainerDark,
    tertiary = SunTertiaryDark,
    onTertiary = SunOnTertiaryDark,
    tertiaryContainer = SunTertiaryContainerDark,
    onTertiaryContainer = SunOnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
)

@Composable
fun CultureTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        useDark -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CultureTypography,
        shapes = CultureShapes,
        content = content,
    )
}
