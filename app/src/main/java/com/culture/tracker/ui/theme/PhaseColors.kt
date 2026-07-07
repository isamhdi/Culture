package com.culture.tracker.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.EmojiNature
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

// Rampe ordinale (une seule teinte, du plus clair au plus foncé) : la progression
// de couleur suit la progression des stades de croissance.
private val lightSteps = listOf(0xFF86B6EF, 0xFF6DA7EC, 0xFF3987E5, 0xFF2A78D6, 0xFF1C5CAB, 0xFF104281)
private val darkSteps = listOf(0xFF9EC5F4, 0xFF86B6EF, 0xFF6DA7EC, 0xFF5598E7, 0xFF3987E5, 0xFF2A78D6)

private val GrowthPhase.stepIndex: Int
    get() = GrowthPhase.entries.indexOf(this)

@Composable
fun GrowthPhase.themedColor(): Color {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val steps = if (isDark) darkSteps else lightSteps
    return Color(steps[stepIndex])
}

val GrowthPhase.icon: ImageVector
    get() = when (this) {
        GrowthPhase.GERMINATION -> Icons.Filled.Spa
        GrowthPhase.SEMIS -> Icons.Filled.EmojiNature
        GrowthPhase.CROISSANCE -> Icons.Filled.Park
        GrowthPhase.FLORAISON -> Icons.Filled.LocalFlorist
        GrowthPhase.SECHAGE -> Icons.Filled.Air
        GrowthPhase.MATURATION -> Icons.Filled.Inventory2
    }
