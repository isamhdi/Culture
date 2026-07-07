package com.culture.tracker.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.culture.tracker.domain.durationFor
import com.culture.tracker.domain.model.GrowthPhase
import com.culture.tracker.ui.components.PhaseChip
import com.culture.tracker.ui.genetics.GeneticsEditSheet
import org.koin.androidx.compose.koinViewModel

private val defaultDurations = linkedMapOf(*GrowthPhase.entries.map { it to it.typicalDurationDays }.toTypedArray())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StageDatesCalculatorSheet(onDismiss: () -> Unit, viewModel: StageDatesViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var selectedGeneticsId by remember { mutableStateOf<Long?>(null) }
    var showCreateSheet by remember { mutableStateOf(false) }
    val durations = remember { mutableStateMapOfDurations() }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            com.culture.tracker.ui.components.SheetHeader("Dates de stade", onClose = onDismiss)
            Text(
                "Estime la durée de chaque phase et la durée totale de culture. Les durées sont modifiables.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider()
            Text("Dates de phase par variété", style = MaterialTheme.typography.titleMedium)
            Text(
                "Choisis une variété pour préremplir les durées, ou ajoute la tienne.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.genetics, key = { it.id }) { genetics ->
                    FilterChip(
                        selected = selectedGeneticsId == genetics.id,
                        onClick = {
                            selectedGeneticsId = genetics.id
                            GrowthPhase.entries.forEach { phase -> durations[phase] = genetics.durationFor(phase) }
                        },
                        label = { Text(genetics.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                }
                item {
                    FilterChip(
                        selected = false,
                        onClick = { showCreateSheet = true },
                        label = { Text("Nouvelle variété") },
                        leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    )
                }
            }

            HorizontalDivider()

            var cumulativeDay = 0
            GrowthPhase.entries.forEach { phase ->
                val duration = durations[phase] ?: 0
                val dayStart = cumulativeDay
                cumulativeDay += duration
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PhaseChip(phase)
                        }
                        Text(
                            "Jour $dayStart à $cumulativeDay",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        OutlinedTextField(
                            value = duration.toString(),
                            onValueChange = { value -> durations[phase] = value.filter(Char::isDigit).toIntOrNull() ?: 0 },
                            label = { Text("Durée (jours)") },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                    }
                }
            }

            Text(
                "Durée totale estimée : $cumulativeDay jours",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }

    if (showCreateSheet) {
        GeneticsEditSheet(
            genetics = null,
            onDismiss = { showCreateSheet = false },
            onSave = { name, breeder, newDurations ->
                viewModel.createGenetics(name, breeder, newDurations) { created ->
                    selectedGeneticsId = created.id
                    GrowthPhase.entries.forEach { phase -> durations[phase] = created.durationFor(phase) }
                }
                showCreateSheet = false
            },
        )
    }
}

private fun mutableStateMapOfDurations(): androidx.compose.runtime.snapshots.SnapshotStateMap<GrowthPhase, Int> {
    val map = androidx.compose.runtime.snapshots.SnapshotStateMap<GrowthPhase, Int>()
    map.putAll(defaultDurations)
    return map
}
