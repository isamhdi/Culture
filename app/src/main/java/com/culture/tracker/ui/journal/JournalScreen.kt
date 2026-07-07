package com.culture.tracker.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.culture.tracker.domain.model.ActionType
import com.culture.tracker.ui.components.SheetHeader
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(viewModel: JournalViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showAddActionSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Journal") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddActionSheet = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter une action")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text("Historique des actions", style = MaterialTheme.typography.titleMedium) }

            if (state.recentActions.isEmpty()) {
                item { Text("Aucune action enregistrée.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(state.recentActions, key = { it.id }) { action ->
                    val plantName = state.plants.firstOrNull { it.id == action.plantId }?.name ?: "?"
                    val fertilizerName = action.fertilizerId?.let { id -> state.fertilizers.firstOrNull { it.id == id }?.name }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(modifier = Modifier.size(10.dp).background(Color(action.actionType.colorHex), CircleShape))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${action.actionType.label} · $plantName" + (fertilizerName?.let { " ($it)" } ?: ""),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Text(action.date.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddActionSheet) {
        AddJournalActionSheet(
            state = state,
            onDismiss = { showAddActionSheet = false },
            onSave = { plantId, actionType, date, fertilizerId, notes ->
                viewModel.addAction(plantId, actionType, date, fertilizerId, notes)
                showAddActionSheet = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddJournalActionSheet(
    state: JournalUiState,
    onDismiss: () -> Unit,
    onSave: (Long, ActionType, LocalDate, Long?, String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedPlantId by remember { mutableStateOf(state.plants.firstOrNull()?.id) }
    var selectedType by remember { mutableStateOf(ActionType.ARROSAGE) }
    var selectedFertilizerId by remember { mutableStateOf<Long?>(null) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var notes by remember { mutableStateOf("") }
    var plantMenuExpanded by remember { mutableStateOf(false) }
    var fertilizerMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SheetHeader("Ajouter une action", onClose = onDismiss)

            ExposedDropdownMenuBox(expanded = plantMenuExpanded, onExpandedChange = { plantMenuExpanded = it }) {
                OutlinedTextField(
                    value = state.plants.firstOrNull { it.id == selectedPlantId }?.name ?: "Choisir une plante",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Plante") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = plantMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                )
                DropdownMenu(expanded = plantMenuExpanded, onDismissRequest = { plantMenuExpanded = false }) {
                    state.plants.forEach { plant ->
                        DropdownMenuItem(text = { Text(plant.name) }, onClick = { selectedPlantId = plant.id; plantMenuExpanded = false })
                    }
                }
            }

            Text("Type d'action", style = MaterialTheme.typography.labelMedium)
            com.culture.tracker.ui.components.ActionTypeSelector(
                selected = selectedType,
                onSelect = { selectedType = it },
            )

            if (selectedType == ActionType.ENGRAIS) {
                ExposedDropdownMenuBox(expanded = fertilizerMenuExpanded, onExpandedChange = { fertilizerMenuExpanded = it }) {
                    OutlinedTextField(
                        value = state.fertilizers.firstOrNull { it.id == selectedFertilizerId }?.name ?: "Choisir un engrais",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Engrais") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fertilizerMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    DropdownMenu(expanded = fertilizerMenuExpanded, onDismissRequest = { fertilizerMenuExpanded = false }) {
                        state.fertilizers.forEach { fert ->
                            DropdownMenuItem(text = { Text(fert.name) }, onClick = { selectedFertilizerId = fert.id; fertilizerMenuExpanded = false })
                        }
                    }
                }
            }

            OutlinedTextField(
                value = date.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { TextButton(onClick = { showDatePicker = true }) { Text("Choisir") } },
            )

            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())

            TextButton(
                onClick = {
                    selectedPlantId?.let { plantId -> onSave(plantId, selectedType, date, selectedFertilizerId, notes.ifBlank { null }) }
                },
                enabled = selectedPlantId != null,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Enregistrer") }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Annuler") } },
        ) { DatePicker(state = datePickerState) }
    }
}
