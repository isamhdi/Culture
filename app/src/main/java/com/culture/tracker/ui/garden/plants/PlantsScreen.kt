package com.culture.tracker.ui.garden.plants

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.culture.tracker.domain.model.GrowMedium
import com.culture.tracker.domain.model.GrowthPhase
import com.culture.tracker.domain.model.PropagationType
import com.culture.tracker.ui.garden.environments.CreateEnvironmentSheet
import com.culture.tracker.ui.theme.icon
import com.culture.tracker.ui.theme.themedColor
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantsScreen(onPlantClick: (Long) -> Unit = {}, viewModel: PlantsViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showCreateSheet by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var filters by remember { mutableStateOf(PlantFilters()) }
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (!success) pendingPhotoFile = null
    }

    val filteredPlants = applyPlantFilters(state.plants, filters)
    val groupedPlants = groupPlants(filteredPlants, filters.groupBy, state.environments)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Plantes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateSheet = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter une plante")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, androidx.compose.foundation.shape.CircleShape),
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = "Filtres", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                FilterChip(
                    selected = filters.stages.isNotEmpty(),
                    onClick = { showFilterSheet = true },
                    label = { Text(if (filters.stages.isEmpty()) "Stade" else "Stade (${filters.stages.size})") },
                )
                FilterChip(
                    selected = filters.environments.isNotEmpty(),
                    onClick = { showFilterSheet = true },
                    label = { Text(if (filters.environments.isEmpty()) "Environnement" else "Environnement (${filters.environments.size})") },
                )
            }

            if (filteredPlants.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                ) {
                    Text("Aucune plante pour le moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    groupedPlants.forEach { (groupLabel, plantsInGroup) ->
                        if (groupLabel.isNotEmpty()) {
                            item(key = "header_$groupLabel") {
                                Text(groupLabel, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        items(plantsInGroup, key = { it.id }) { plant ->
                        val genetics = state.genetics.firstOrNull { it.id == plant.geneticsId }
                        PlantCard(
                            plant = plant,
                            thumbnailPath = state.thumbnails[plant.id],
                            genetics = genetics,
                            openPhase = state.openPhaseByPlant[plant.id],
                            heightCm = state.latestHeightByPlant[plant.id],
                            onClick = { onPlantClick(plant.id) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        }
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        CreatePlantSheet(
            state = state,
            photoFile = pendingPhotoFile,
            onTakePhoto = {
                val (file, uri) = viewModel.createPhotoCaptureTarget()
                pendingPhotoFile = file
                takePictureLauncher.launch(uri)
            },
            onDismiss = { showCreateSheet = false; pendingPhotoFile = null },
            onCreateGenetics = { name, breeder, onCreated -> viewModel.createGenetics(name, breeder, onCreated) },
            onCreateEnvironment = { name, hours, size, material, lightType, power, spectrum, model, onCreated ->
                viewModel.createEnvironment(name, hours, size, material, lightType, power, spectrum, model, onCreated)
            },
            onCreate = { name, propagation, geneticsId, environmentId, phase, watering, fertilizing, count, medium, mediumDescription, phaseDates ->
                val earliestDate = phaseDates.values.minOrNull() ?: LocalDate.now()
                viewModel.createPlant(
                    name, propagation, geneticsId, environmentId, phase, earliestDate,
                    watering, fertilizing, count, pendingPhotoFile, medium, mediumDescription, phaseDates,
                )
                pendingPhotoFile = null
                showCreateSheet = false
            },
        )
    }

    if (showFilterSheet) {
        PlantFilterSheet(
            filters = filters,
            environments = state.environments,
            onDismiss = { showFilterSheet = false },
            onApply = { filters = it; showFilterSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePlantSheet(
    state: PlantsUiState,
    photoFile: File?,
    onTakePhoto: () -> Unit,
    onDismiss: () -> Unit,
    onCreateGenetics: (String, String?, (Long) -> Unit) -> Unit,
    onCreateEnvironment: (String, Double, String?, String?, String?, Int?, String?, String?, (Long) -> Unit) -> Unit,
    onCreate: (String, PropagationType, Long?, Long?, GrowthPhase, Int?, Int?, Int, GrowMedium?, String?, Map<GrowthPhase, LocalDate>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var name by remember { mutableStateOf("") }
    var propagation by remember { mutableStateOf(PropagationType.GRAINE) }
    var selectedGeneticsId by remember { mutableStateOf<Long?>(null) }
    var newGeneticsName by remember { mutableStateOf("") }
    var selectedEnvironmentId by remember { mutableStateOf<Long?>(null) }
    var showCreateEnvironmentSheet by remember { mutableStateOf(false) }
    var startingPhase by remember { mutableStateOf(GrowthPhase.GERMINATION) }
    var editingDateForPhase by remember { mutableStateOf<GrowthPhase?>(null) }
    val phaseDates = remember { mutableStateMapOf(GrowthPhase.GERMINATION to LocalDate.now()) }
    var wateringInterval by remember { mutableStateOf("") }
    var fertilizingInterval by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("1") }
    var selectedMedium by remember { mutableStateOf<GrowMedium?>(null) }
    var mediumDescription by remember { mutableStateOf("") }
    var mediumExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(startingPhase) {
        val included = GrowthPhase.entries.filter { it.ordinal <= startingPhase.ordinal }
        val previousDates = phaseDates.toMap()
        phaseDates.keys.retainAll(included.toSet())
        included.forEach { phase ->
            if (phase !in phaseDates) {
                phaseDates[phase] = previousDates.values.maxOrNull() ?: LocalDate.now()
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            com.culture.tracker.ui.components.SheetHeader("Nouvelle plante", onClose = onDismiss)

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
            TextButton(onClick = { showCreateEnvironmentSheet = true }) { Text("+ Créer un environnement") }

            Text("Phase de départ", style = MaterialTheme.typography.labelMedium)
            com.culture.tracker.ui.components.PhaseGridSelector(selected = startingPhase, onSelect = { startingPhase = it })

            Text("Dates de phase", style = MaterialTheme.typography.labelMedium)
            GrowthPhase.entries.filter { it.ordinal <= startingPhase.ordinal }.forEach { phase ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(phase.icon, contentDescription = null, tint = phase.themedColor(), modifier = Modifier.size(20.dp))
                    Text(phase.label, modifier = Modifier.weight(1f))
                    TextButton(onClick = { editingDateForPhase = phase }) {
                        Text(phaseDates[phase]?.toString() ?: "Choisir")
                    }
                }
            }

            Text("Medium", style = MaterialTheme.typography.labelMedium)
            ExposedDropdownMenuBox(expanded = mediumExpanded, onExpandedChange = { mediumExpanded = it }) {
                OutlinedTextField(
                    value = selectedMedium?.label ?: "Choisir un medium",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type de medium") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mediumExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                )
                DropdownMenu(expanded = mediumExpanded, onDismissRequest = { mediumExpanded = false }) {
                    GrowMedium.entries.forEach { medium ->
                        DropdownMenuItem(text = { Text(medium.label) }, onClick = { selectedMedium = medium; mediumExpanded = false })
                    }
                }
            }
            if (selectedMedium != null) {
                OutlinedTextField(
                    value = mediumDescription,
                    onValueChange = { mediumDescription = it },
                    label = { Text("Description du medium") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

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

            Text("Photo", style = MaterialTheme.typography.labelMedium)
            Box(
                modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (photoFile != null) {
                    AsyncImage(
                        model = photoFile,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp)),
                    )
                }
                IconButton(onClick = onTakePhoto) {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = "Ajouter une photo")
                }
            }

            OutlinedTextField(
                value = count,
                onValueChange = { count = it.filter(Char::isDigit) },
                label = { Text("Nombre d'exemplaires") },
                supportingText = { Text("Crée plusieurs plantes avec le même réglage") },
                modifier = Modifier.fillMaxWidth(),
            )

            TextButton(
                onClick = {
                    onCreate(
                        name,
                        propagation,
                        selectedGeneticsId,
                        selectedEnvironmentId,
                        startingPhase,
                        wateringInterval.toIntOrNull(),
                        fertilizingInterval.toIntOrNull(),
                        count.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                        selectedMedium,
                        mediumDescription.ifBlank { null },
                        phaseDates.toMap(),
                    )
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Créer la plante") }
        }
    }

    editingDateForPhase?.let { phase ->
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = (phaseDates[phase] ?: LocalDate.now()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { editingDateForPhase = null },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        phaseDates[phase] = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    editingDateForPhase = null
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { editingDateForPhase = null }) { Text("Annuler") } },
        ) { DatePicker(state = datePickerState) }
    }

    if (showCreateEnvironmentSheet) {
        CreateEnvironmentSheet(
            onDismiss = { showCreateEnvironmentSheet = false },
            onCreate = { envName, hours, size, material, lightType, power, spectrum, model ->
                onCreateEnvironment(envName, hours, size, material, lightType, power, spectrum, model) { id ->
                    selectedEnvironmentId = id
                }
                showCreateEnvironmentSheet = false
            },
        )
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
