package com.culture.tracker.ui.garden.plants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.culture.tracker.data.local.entity.Environment
import com.culture.tracker.domain.model.GrowthPhase
import com.culture.tracker.ui.components.SheetHeader
import com.culture.tracker.ui.theme.themedColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantFilterSheet(
    filters: PlantFilters,
    environments: List<Environment>,
    onDismiss: () -> Unit,
    onApply: (PlantFilters) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var status by remember { mutableStateOf(filters.status) }
    var groupBy by remember { mutableStateOf(filters.groupBy) }
    var sortBy by remember { mutableStateOf(filters.sortBy) }
    var stages by remember { mutableStateOf(filters.stages) }
    var envIds by remember { mutableStateOf(filters.environments) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SheetHeader("Filtres", onClose = onDismiss)

            Text("Afficher les plantes", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PlantStatusFilter.entries.forEach { option ->
                    FilterChip(selected = status == option, onClick = { status = option }, label = { Text(option.label) })
                }
            }

            Text("Grouper par", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PlantGroupBy.entries.forEach { option ->
                    FilterChip(selected = groupBy == option, onClick = { groupBy = option }, label = { Text(option.label) })
                }
            }

            Text("Trier par", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PlantSortBy.entries.forEach { option ->
                    FilterChip(selected = sortBy == option, onClick = { sortBy = option }, label = { Text(option.label) })
                }
            }

            Text("Stade", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                GrowthPhase.entries.forEach { phase ->
                    val color = phase.themedColor()
                    val selected = phase in stages
                    FilterChip(
                        selected = selected,
                        onClick = { stages = if (selected) stages - phase else stages + phase },
                        label = { Text(phase.label) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color, selectedLabelColor = androidx.compose.ui.graphics.Color.Black),
                    )
                }
            }

            if (environments.isNotEmpty()) {
                Text("Environnement", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    environments.forEach { env ->
                        val selected = env.id in envIds
                        FilterChip(
                            selected = selected,
                            onClick = { envIds = if (selected) envIds - env.id else envIds + env.id },
                            label = { Text(env.name) },
                        )
                    }
                }
            }

            TextButton(
                onClick = { onApply(PlantFilters(status, groupBy, sortBy, stages, envIds)) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Appliquer les filtres") }
        }
    }
}
