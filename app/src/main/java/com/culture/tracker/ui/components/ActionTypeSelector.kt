package com.culture.tracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.culture.tracker.domain.model.ActionType
import com.culture.tracker.ui.theme.icon
import com.culture.tracker.ui.theme.themedColor

/** Sélecteur de type d'action en puces qui s'enroulent sur plusieurs lignes (remplace un SegmentedButtonRow qui déborde à 6 options). */
@Composable
fun ActionTypeSelector(
    selected: ActionType,
    onSelect: (ActionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ActionType.entries.forEach { type ->
            val isSelected = type == selected
            val color = type.themedColor()
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(type) },
                label = { androidx.compose.material3.Text(type.label) },
                leadingIcon = { Icon(type.icon, contentDescription = null, tint = if (isSelected) color else color.copy(alpha = 0.7f)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.18f),
                    selectedLabelColor = color,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = color.copy(alpha = 0.4f),
                    selectedBorderColor = color,
                ),
            )
        }
    }
}
