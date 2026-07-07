package com.culture.tracker.ui.tools

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.culture.tracker.domain.model.GrowthPhase
import com.culture.tracker.ui.components.PhaseChip
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val defaultDurations = linkedMapOf(*GrowthPhase.entries.map { it to it.typicalDurationDays }.toTypedArray())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StageDatesCalculatorSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val durations = remember { mutableStateMapOfDurations() }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            com.culture.tracker.ui.components.SheetHeader("Dates de stade", onClose = onDismiss)
            Text(
                "Estime les dates de chaque phase à partir d'une date de départ et de durées modifiables.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = startDate.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date de départ") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { TextButton(onClick = { showDatePicker = true }) { Text("Choisir") } },
            )

            HorizontalDivider()

            var cursor = startDate
            GrowthPhase.entries.forEach { phase ->
                val duration = durations[phase] ?: 0
                val phaseStart = cursor
                cursor = cursor.plusDays(duration.toLong())
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PhaseChip(phase)
                        }
                        Text(
                            "Du $phaseStart au $cursor",
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
                "Récolte estimée : $cursor",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        startDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Annuler") } },
        ) { DatePicker(state = datePickerState) }
    }
}

private fun mutableStateMapOfDurations(): androidx.compose.runtime.snapshots.SnapshotStateMap<GrowthPhase, Int> {
    val map = androidx.compose.runtime.snapshots.SnapshotStateMap<GrowthPhase, Int>()
    map.putAll(defaultDurations)
    return map
}
