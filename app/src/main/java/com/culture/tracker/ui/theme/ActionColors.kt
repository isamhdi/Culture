package com.culture.tracker.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import com.culture.tracker.domain.model.ActionType

/** Couleur de la catégorie d'action adaptée au thème actif (clair/sombre), pas seulement au thème système. */
@Composable
fun ActionType.themedColor(): Color {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return Color(if (isDark) darkColorHex else colorHex)
}

val ActionType.icon: ImageVector
    get() = when (this) {
        ActionType.ARROSAGE -> Icons.Filled.Opacity
        ActionType.ENGRAIS -> Icons.Filled.Yard
        ActionType.COUPE -> Icons.Filled.ContentCut
        ActionType.PINCH -> Icons.Filled.PanTool
        ActionType.REMPOTAGE -> Icons.Filled.Grass
        ActionType.AUTRE -> Icons.Filled.MoreHoriz
    }
