package com.culture.tracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.culture.tracker.R
import com.culture.tracker.domain.model.ActionType
import com.culture.tracker.domain.model.PlantMeasurementType
import com.culture.tracker.ui.components.ActionTypeSelector
import com.culture.tracker.ui.components.CircularProgressRing
import com.culture.tracker.ui.components.PlantCardCompact
import com.culture.tracker.ui.components.SheetHeader
import com.culture.tracker.ui.components.StatTile
import com.culture.tracker.ui.components.WeekCalendar
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onPlantClick: (Long) -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToGarden: () -> Unit = {},
    onNavigateToEnvironments: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var showAddActionSheet by remember { mutableStateOf(false) }
    var showAddLogSheet by remember { mutableStateOf(false) }
    val greeting = remember(state.userName) { homeGreeting(LocalDate.now(), state.userName) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_home)) }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                Card {
                    WeekCalendar(
                        selectedDate = state.selectedDate,
                        dotsForDate = { date ->
                            state.actionsInWeek.filter { it.date == date }
                                .map { com.culture.tracker.ui.components.DotSpec(androidx.compose.ui.graphics.Color(it.actionType.colorHex)) }
                        },
                        onDateClick = viewModel::onDateSelected,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            if (state.todayScheduledCount > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToCalendar,
                        shape = MaterialTheme.shapes.extraLarge,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(Brush.linearGradient(listOf(Color(0xFF1C5CAB), Color(0xFF1BAF7A))))
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressRing(
                                progress = state.todayCompletionRatio,
                                size = 76.dp,
                                strokeWidth = 8.dp,
                                trackColor = Color.White.copy(alpha = 0.25f),
                                gradientColors = listOf(Color.White, Color.White.copy(alpha = 0.6f)),
                            ) {
                                Text(
                                    "${(state.todayCompletionRatio * 100).toInt()}%",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                            Column {
                                Text("Aujourd'hui", color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${state.todayDoneCount}/${state.todayScheduledCount} tâches complétées",
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }
            }

            item {
                StatTile(
                    label = "Plantes actives",
                    value = state.plants.size.toString(),
                    icon = Icons.Filled.Spa,
                    modifier = Modifier.fillMaxWidth(),
                    contentColor = Color.White,
                    containerBrush = Brush.linearGradient(listOf(Color(0xFF3B6939), Color(0xFF1BAF7A))),
                    onClick = onNavigateToGarden,
                )
            }

            item {
                Column {
                    Text("Boîte à outils", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ToolboxAction(Icons.Filled.Bolt, "Action") { showAddActionSheet = true }
                        ToolboxAction(Icons.Filled.Assignment, "Relevé") { showAddLogSheet = true }
                        ToolboxAction(Icons.Filled.SpaceDashboard, "Environnement") { onNavigateToEnvironments() }
                        ToolboxAction(Icons.Filled.MoreHoriz, "Plus") { onNavigateToSettings() }
                    }
                }
            }

            if (state.plants.isNotEmpty()) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Vos plantes", style = MaterialTheme.typography.titleMedium)
                        if (state.plants.size > 4) {
                            androidx.compose.material3.TextButton(onClick = onNavigateToGarden) { Text("Tout voir") }
                        }
                    }
                }
                items(state.plants.take(4).chunked(2), key = { pair -> pair.joinToString { it.id.toString() } }) { rowPlants ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowPlants.forEach { plant ->
                            val genetics = state.genetics.firstOrNull { it.id == plant.geneticsId }
                            PlantCardCompact(
                                plant = plant,
                                thumbnailPath = state.thumbnails[plant.id],
                                genetics = genetics,
                                openPhase = state.openPhaseByPlant[plant.id],
                                onClick = { onPlantClick(plant.id) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowPlants.size == 1) {
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(state.moonPhase.emoji, style = MaterialTheme.typography.titleLarge)
                        Column {
                            Text("Phase lunaire du jour", style = MaterialTheme.typography.labelMedium)
                            Text(state.moonPhase.label, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }

    if (showAddActionSheet) {
        HomeAddActionSheet(
            plants = state.plants,
            fertilizers = state.fertilizers,
            onDismiss = { showAddActionSheet = false },
            onSave = { plantId, actionType, date, fertilizerId, notes ->
                viewModel.addAction(plantId, actionType, date, fertilizerId, notes)
                showAddActionSheet = false
            },
        )
    }

    if (showAddLogSheet) {
        HomeAddLogSheet(
            plants = state.plants,
            onDismiss = { showAddLogSheet = false },
            onSave = { plantId, date, note, type, value ->
                viewModel.addPlantLog(plantId, date, note, type, value)
                showAddLogSheet = false
            },
        )
    }
}

@Composable
private fun ToolboxAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 2.dp)) {
        androidx.compose.material3.IconButton(
            onClick = onClick,
            modifier = Modifier.size(52.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeAddActionSheet(
    plants: List<com.culture.tracker.data.local.entity.Plant>,
    fertilizers: List<com.culture.tracker.data.local.entity.Fertilizer>,
    onDismiss: () -> Unit,
    onSave: (Long, ActionType, LocalDate, Long?, String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedPlantId by remember { mutableStateOf(plants.firstOrNull()?.id) }
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
                    value = plants.firstOrNull { it.id == selectedPlantId }?.name ?: "Choisir une plante",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Plante") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = plantMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                )
                DropdownMenu(expanded = plantMenuExpanded, onDismissRequest = { plantMenuExpanded = false }) {
                    plants.forEach { plant ->
                        DropdownMenuItem(text = { Text(plant.name) }, onClick = { selectedPlantId = plant.id; plantMenuExpanded = false })
                    }
                }
            }

            Text("Type d'action", style = MaterialTheme.typography.labelMedium)
            ActionTypeSelector(selected = selectedType, onSelect = { selectedType = it })

            if (selectedType == ActionType.ENGRAIS) {
                ExposedDropdownMenuBox(expanded = fertilizerMenuExpanded, onExpandedChange = { fertilizerMenuExpanded = it }) {
                    OutlinedTextField(
                        value = fertilizers.firstOrNull { it.id == selectedFertilizerId }?.name ?: "Choisir un engrais",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Engrais") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fertilizerMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    DropdownMenu(expanded = fertilizerMenuExpanded, onDismissRequest = { fertilizerMenuExpanded = false }) {
                        fertilizers.forEach { fert ->
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
                onClick = { selectedPlantId?.let { onSave(it, selectedType, date, selectedFertilizerId, notes.ifBlank { null }) } },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeAddLogSheet(
    plants: List<com.culture.tracker.data.local.entity.Plant>,
    onDismiss: () -> Unit,
    onSave: (Long, LocalDate, String?, PlantMeasurementType?, Double?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedPlantId by remember { mutableStateOf(plants.firstOrNull()?.id) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<PlantMeasurementType?>(null) }
    var valueText by remember { mutableStateOf("") }
    var plantMenuExpanded by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SheetHeader("Ajouter un relevé", onClose = onDismiss)

            ExposedDropdownMenuBox(expanded = plantMenuExpanded, onExpandedChange = { plantMenuExpanded = it }) {
                OutlinedTextField(
                    value = plants.firstOrNull { it.id == selectedPlantId }?.name ?: "Choisir une plante",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Plante") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = plantMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                )
                DropdownMenu(expanded = plantMenuExpanded, onDismissRequest = { plantMenuExpanded = false }) {
                    plants.forEach { plant ->
                        DropdownMenuItem(text = { Text(plant.name) }, onClick = { selectedPlantId = plant.id; plantMenuExpanded = false })
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

            OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Note") }, modifier = Modifier.fillMaxWidth())

            ExposedDropdownMenuBox(expanded = typeMenuExpanded, onExpandedChange = { typeMenuExpanded = it }) {
                OutlinedTextField(
                    value = selectedType?.label ?: "Ajouter une mesure",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mesure") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                )
                DropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Aucune") }, onClick = { selectedType = null; typeMenuExpanded = false })
                    PlantMeasurementType.entries.forEach { type ->
                        DropdownMenuItem(text = { Text(type.label) }, onClick = { selectedType = type; typeMenuExpanded = false })
                    }
                }
            }

            if (selectedType != null) {
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Valeur${selectedType?.unit?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""}") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            TextButton(
                onClick = {
                    selectedPlantId?.let { onSave(it, date, note.ifBlank { null }, selectedType, valueText.toDoubleOrNull()) }
                },
                enabled = selectedPlantId != null && (note.isNotBlank() || (selectedType != null && valueText.toDoubleOrNull() != null)),
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
