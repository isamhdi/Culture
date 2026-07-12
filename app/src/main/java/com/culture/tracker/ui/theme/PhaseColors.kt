package com.culture.tracker.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import com.culture.tracker.domain.model.GrowthPhase

// Une teinte distincte par phase (palette catégorielle), calée pixel-pour-pixel sur
// les tokens OKLCH du design_handoff_suivi_plante (croissance/floraison identiques
// au handoff ; germination/séchage/maturation étendues dans la même famille L/C).
private val lightColors = mapOf(
    GrowthPhase.GERMINATION to 0xFF17D0D8, // oklch(0.78 0.13 200)
    GrowthPhase.CROISSANCE to 0xFF6ED274, // oklch(0.78 0.16 145)
    GrowthPhase.FLORAISON to 0xFFE196F3, // oklch(0.78 0.15 320)
    GrowthPhase.SECHAGE to 0xFFF4A437, // oklch(0.78 0.15 70)
    GrowthPhase.MATURATION to 0xFFB7A7FF, // oklch(0.78 0.14 290)
)
private val darkColors = lightColors

// Teintes foncées de chaque couleur de phase, utilisées comme texte sur un fond pâle de la même
// teinte (pastille, initiale d'avatar) : le texte clair sur fond clair mesurait ~1.3:1 de contraste
// en thème jour (quasi illisible) ; ces variantes visent ≥4.5:1 (WCAG AA texte normal).
private val lightBadgeTextColors = mapOf(
    GrowthPhase.GERMINATION to 0xFF0A4F54,
    GrowthPhase.CROISSANCE to 0xFF1F4F23,
    GrowthPhase.FLORAISON to 0xFF6E2879,
    GrowthPhase.SECHAGE to 0xFF6B4407,
    GrowthPhase.MATURATION to 0xFF3C2E87,
)

@Composable
fun GrowthPhase.themedColor(): Color {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val map = if (isDark) darkColors else lightColors
    return Color(map.getValue(this))
}

/** Couleur à utiliser pour du texte posé sur un fond teinté de [themedColor] (pastille, avatar). */
@Composable
fun GrowthPhase.onBadgeColor(): Color {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return if (isDark) themedColor() else Color(lightBadgeTextColors.getValue(this))
}

val GrowthPhase.icon: ImageVector
    get() = when (this) {
        GrowthPhase.GERMINATION -> Icons.Filled.Spa
        GrowthPhase.CROISSANCE -> Icons.Filled.Park
        GrowthPhase.FLORAISON -> Icons.Filled.LocalFlorist
        GrowthPhase.SECHAGE -> Icons.Filled.Air
        GrowthPhase.MATURATION -> Icons.Filled.Inventory2
    }
