package com.culture.tracker.ui.garden.plants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import com.culture.tracker.ui.components.PlantCard
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.culture.tracker.domain.model.GrowthPhase
import com.culture.tracker.domain.model.PropagationType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantsScreen(onPlantClick: (Long) -> Unit = {}, viewModel: PlantsViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showCreateSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Plantes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateSheet = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter une plante")
            }
        },
    ) { padding ->
        if (state.plants.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                Text("Aucune plante pour le moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.plants, key = { it.id }) { plant ->
                    val environmentName = state.environments.firstOrNull { it.id == plant.environmentId }?.name
                    PlantCard(
                        plant = plant,
                        thumbnailPath = state.thumbnails[plant.id],
                        environmentName = environmentName,
                        onClick = { onPlantClick(plant.id) },
                    )
                }
            }
        }
    }

    if (showCreateSheet) {
        CreatePlantSheet(
            state = state,
            onDismiss = { showCreateSheet = false },
            onCreateGenetics = { name, breeder, onCreated -> viewModel.createGenetics(name, breeder, onCreated) },
            onCreate = { name, propagation, geneticsId, environmentId, phase, startDate, watering, fertilizing ->
                viewModel.createPlant(name, propagation, geneticsId, environmentId, phase, startDate, watering, fertilizing)
                showCreateSheet = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePlantSheet(
    state: PlantsUiState,
    onDismiss: () -> Unit,
    onCreateGenetics: (String, String?, (Long) -> Unit) -> Unit,
    onCreate: (String, PropagationType, Long?, Long?, GrowthPhase, LocalDate, Int?, Int?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var name by remember { mutableStateOf("") }
    var propagation by remember { mutableStateOf(PropagationType.GRAINE) }
    var selectedGeneticsId by remember { mutableStateOf<Long?>(null) }
    var newGeneticsName by remember { mutableStateOf("") }
    var selectedEnvironmentId by remember { mutableStateOf<Long?>(null) }
    var startingPhase by remember { mutableStateOf(GrowthPhase.GERMINATION) }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var wateringInterval by remember { mutableStateOf("") }
    var fertilizingInterval by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Nouvelle plante", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom de la plante") }, modifier = Modifier.fillMaxWidth())

            Text("Origine", style = MaterialTheme.typography.labelMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                PropagationType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = propagation == type,
                        onClick = { propagation = type },
                        shape = SegmentedButtonDefaults.itemShape(index, PropagationType.entries.size),
                    ) { Text(type.label) }
                }
            }

            DropdownField(
                label = "Génétique (variété)",
                options = state.genetics.map { it.id to it.name },
                selectedId = selectedGeneticsId,
                onSelect = { selectedGeneticsId = it },
                allowCreateNew = true,
                newValueLabel = "Nouvelle génétique",
                newValue = newGeneticsName,
                onNewValueChange = { newGeneticsName = it },
                onCreateNew = {
                    if (newGeneticsName.isNotBlank()) {
                        onCreateGenetics(newGeneticsName, null) { id -> selectedGeneticsId = id }
                        newGeneticsName = ""
                    }
                },
            )

            DropdownField(
                label = "Environnement",
                options = state.environments.map { it.id to it.name },
                selectedId = selectedEnvironmentId,
                onSelect = { selectedEnvironmentId = it },
            )

            var phaseExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = phaseExpanded, onExpandedChange = { phaseExpanded = it }) {
                OutlinedTextField(
                    value = startingPhase.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Phase de départ") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = phaseExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                )
                DropdownMenu(expanded = phaseExpanded, onDismissRequest = { phaseExpanded = false }) {
                    GrowthPhase.entries.forEach { phase ->
                        DropdownMenuItem(text = { Text(phase.label) }, onClick = { startingPhase = phase; phaseExpanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = startDate.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date de début") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) { Text("Choisir") }
                },
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = wateringInterval,
                    onValueChange = { wateringInterval = it.filter(Char::isDigit) },
                    label = { Text("Arrosage tous les (j)") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = fertilizingInterval,
                    onValueChange = { fertilizingInterval = it.filter(Char::isDigit) },
                    label = { Text("Engrais tous les (j)") },
                    modifier = Modifier.weight(1f),
                )
            }

            TextButton(
                onClick = {
                    onCreate(
                        name,
                        propagation,
                        selectedGeneticsId,
                        selectedEnvironmentId,
                        startingPhase,
                        startDate,
                        wateringInterval.toIntOrNull(),
                        fertilizingInterval.toIntOrNull(),
                    )
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Créer la plante") }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<Pair<Long, String>>,
    selectedId: Long?,
    onSelect: (Long?) -> Unit,
    allowCreateNew: Boolean = false,
    newValueLabel: String = "",
    newValue: String = "",
    onNewValueChange: (String) -> Unit = {},
    onCreateNew: () -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedId }?.second ?: "Aucun(e)"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Aucun(e)") }, onClick = { onSelect(null); expanded = false })
            options.forEach { (id, optionLabel) ->
                DropdownMenuItem(text = { Text(optionLabel) }, onClick = { onSelect(id); expanded = false })
            }
        }
    }

    if (allowCreateNew) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = newValue,
                onValueChange = onNewValueChange,
                label = { Text(newValueLabel) },
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onCreateNew, enabled = newValue.isNotBlank()) { Text("Ajouter") }
        }
    }
}
