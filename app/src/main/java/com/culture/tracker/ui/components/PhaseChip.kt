package com.culture.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.culture.tracker.domain.model.GrowthPhase
import com.culture.tracker.ui.theme.icon
import com.culture.tracker.ui.theme.themedColor

@Composable
fun PhaseChip(phase: GrowthPhase, modifier: Modifier = Modifier) {
    val color = phase.themedColor()
    val onColor = if (color.luminance() > 0.5f) Color.Black else Color.White
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(phase.icon, contentDescription = null, tint = onColor, modifier = Modifier.size(14.dp))
        Text(phase.label, color = onColor, style = MaterialTheme.typography.labelMedium)
    }
}
