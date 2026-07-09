package com.culture.tracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.culture.tracker.domain.model.GrowthPhase
import com.culture.tracker.ui.theme.icon
import com.culture.tracker.ui.theme.themedColor

/**
 * Sélecteur de phase de départ en grille : les phases antérieures à celle choisie
 * s'affichent comme déjà passées (coche, couleur atténuée), la phase choisie ressort
 * en couleur pleine, et les phases suivantes restent de simples options non cochées.
 */
@Composable
fun PhaseGridSelector(selected: GrowthPhase, onSelect: (GrowthPhase) -> Unit, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GrowthPhase.entries.forEach { phase ->
            val isSelected = phase == selected
            val isPassed = phase.ordinal < selected.ordinal
            val color = phase.themedColor()
            FilterChip(
                selected = isSelected || isPassed,
                onClick = { onSelect(phase) },
                label = { Text(phase.label) },
                leadingIcon = {
                    if (isSelected || isPassed) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    } else {
                        Icon(phase.icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = color.copy(alpha = 0.7f))
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (isSelected) color else color.copy(alpha = 0.35f),
                    selectedLabelColor = androidx.compose.ui.graphics.Color.Black,
                    selectedLeadingIconColor = androidx.compose.ui.graphics.Color.Black,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected || isPassed,
                    borderColor = color.copy(alpha = 0.4f),
                    selectedBorderColor = color,
                ),
                modifier = Modifier.clip(RoundedCornerShape(50)),
            )
        }
    }
}
