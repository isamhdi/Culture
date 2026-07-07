package com.culture.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.culture.tracker.domain.model.GrowthPhase
import com.culture.tracker.ui.theme.themedColor

/** Pastille de phase reprise du design_handoff : fond teinté, texte mono coloré, radius 8dp. */
@Composable
fun PhasePill(phase: GrowthPhase, modifier: Modifier = Modifier) {
    val color = phase.themedColor()
    Text(
        text = phase.label,
        color = color,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}
