package com.culture.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.culture.tracker.domain.model.GrowthPhase
import com.culture.tracker.ui.theme.themedColor

/** Ligne de progression phase par phase : pastilles reliées par un trait, colorées jusqu'à la phase actuelle. */
@Composable
fun PhaseTimeline(currentPhase: GrowthPhase, modifier: Modifier = Modifier) {
    val currentIndex = GrowthPhase.entries.indexOf(currentPhase)
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        GrowthPhase.entries.forEachIndexed { index, phase ->
            val reached = index <= currentIndex
            val isCurrent = index == currentIndex
            val color = if (reached) phase.themedColor() else MaterialTheme.colorScheme.surfaceVariant

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 14.dp else 10.dp)
                        .background(color, CircleShape),
                )
                Text(
                    phase.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (reached) phase.themedColor() else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            if (index != GrowthPhase.entries.lastIndex) {
                val lineColor = if (index < currentIndex) phase.themedColor() else MaterialTheme.colorScheme.surfaceVariant
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(lineColor),
                )
            }
        }
    }
}
