package com.culture.tracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Radius calés sur design_handoff_suivi_plante : 12=petits éléments/inputs,
// 14=stats/boutons, 16=carte graphique, 18=carte plante, pilules=full (extraLarge).
val CultureShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
