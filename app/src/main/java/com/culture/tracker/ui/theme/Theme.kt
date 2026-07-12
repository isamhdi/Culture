package com.culture.tracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
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
    surfaceVariant = Color(0xFFE8E4D4),
    onSurfaceVariant = Color(0xFF48493E),
    // Assombris par rapport aux tons d'origine pour que les cartes et la piste des barres de
    // progression se détachent réellement du fond en thème jour (contraste mesuré ~1.2:1 avant
    // changement, quasi imperceptible ; ~1.5-1.7:1 après, cf. discussion sur la lisibilité).
    surfaceContainer = Color(0xFFDCD6BA),
    surfaceContainerHigh = Color(0xFFD1CAAB),
    surfaceContainerHighest = Color(0xFFC8BF9F),
    surfaceContainerLow = Color(0xFFF6F3E7),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    outline = Color(0xFF7C7C6C),
    outlineVariant = Color(0xFFA39C80),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF6ED274),
    onPrimary = HandoffColors.TextOnAccent,
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
    background = HandoffColors.BackgroundApp,
    onBackground = HandoffColors.TextPrimary,
    surface = HandoffColors.SurfaceCard,
    onSurface = HandoffColors.TextPrimary,
    surfaceContainer = HandoffColors.SurfaceCard,
    surfaceContainerHigh = Color(0xFF171F1A),
    surfaceContainerHighest = Color(0xFF1B231D),
    surfaceContainerLow = HandoffColors.BackgroundApp,
    surfaceContainerLowest = HandoffColors.BackgroundApp,
    surfaceVariant = HandoffColors.SurfaceCard,
    onSurfaceVariant = HandoffColors.TextSecondary,
    outline = HandoffColors.BorderForm,
    outlineVariant = HandoffColors.BorderCard,
    error = HandoffColors.Danger,
    onError = HandoffColors.TextOnAccent,
)

@Composable
fun CultureTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
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

    // enableEdgeToEdge() ne fixe le style des icônes système qu'une fois, à partir du thème
    // système — indépendant du réglage Jour/Nuit/Système propre à l'app. On le resynchronise
    // ici à chaque changement pour que les icônes restent lisibles dans toutes les combinaisons.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !useDark
            controller.isAppearanceLightNavigationBars = !useDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CultureTypography,
        shapes = CultureShapes,
        content = content,
    )
}
