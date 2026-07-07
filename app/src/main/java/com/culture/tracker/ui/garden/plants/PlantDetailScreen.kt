package com.culture.tracker.ui.garden.plants

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.culture.tracker.data.local.entity.PhaseHistory
import com.culture.tracker.domain.model.ActionType
import com.culture.tracker.domain.model.GrowthPhase
import com.culture.tracker.ui.components.GrowthChart
import com.culture.tracker.ui.components.PhaseChip
import com.culture.tracker.ui.components.PhaseTimeline
import com.culture.tracker.ui.components.SheetHeader
import com.culture.tracker.ui.theme.HandoffColors
import com.culture.tracker.ui.theme.icon
import com.culture.tracker.ui.theme.themedColor
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plantId: Long,
    onBack: () -> Unit,
    viewModel: PlantDetailViewModel = koinViewModel(parameters = { parametersOf(plantId) }),
) {
    val state by viewModel.uiState.collectAsState()
    var pendingPhotoFile by remember { mutableStateOf<File?>(null) }
    var editingPhase by remember { mutableStateOf<PhaseHistory?>(null) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showAddHeightDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAddActionSheet by remember { mutableStateOf(false) }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) pendingPhotoFile?.let { viewModel.savePhoto(it) }
        pendingPhotoFile = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.plant?.name ?: "Plante") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") }
                },
                actions = {
                    IconButton(onClick = { showEditSheet = true }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Modifier la plante")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Close, contentDescription = "Supprimer la plante", tint = HandoffColors.Danger)
                    }
                },
            )
        },
        bottomBar = {
            state.plant?.let { plant ->
                val currentIndex = com.culture.tracker.domain.model.GrowthPhase.entries.indexOf(plant.currentPhase)
                val nextPhase = com.culture.tracker.domain.model.GrowthPhase.entries.getOrNull(currentIndex + 1)
                if (nextPhase != null) {
                    val color = plant.currentPhase.themedColor()
                    androidx.compose.material3.OutlinedButton(
                        onClick = { viewModel.advanceToNextPhase() },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = color),
                        border = androidx.compose.foundation.BorderStroke(1.dp, color),
                    ) {
                        Text("Passer en ${nextPhase.label.lowercase()}")
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                state.plant?.let { plant ->
                    val phaseColor = plant.currentPhase.themedColor()
                    val heroPhoto = state.photos.firstOrNull()
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        if (heroPhoto != null) {
                            AsyncImage(
                                model = File(heroPhoto.filePath),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize().background(
                                    Brush.linearGradient(listOf(phaseColor.copy(alpha = 0.7f), phaseColor.copy(alpha = 0.25f))),
                                ),
                            )
                        }
                        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)))))
                        Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                            PhaseChip(plant.currentPhase)
                            val days = ChronoUnit.DAYS.between(plant.startDate, LocalDate.now())
                            Text(
                                "$days jours · ${plant.propagationType.label}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Photos", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        val (file, uri) = viewModel.createPhotoCaptureTarget()
                        pendingPhotoFile = file
                        takePictureLauncher.launch(uri)
                    }) { Icon(Icons.Filled.CameraAlt, contentDescription = "Prendre une photo") }
                }
            }

            item {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        Box(
                            modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            IconButton(onClick = {
                                val (file, uri) = viewModel.createPhotoCaptureTarget()
                                pendingPhotoFile = file
                                takePictureLauncher.launch(uri)
                            }) { Icon(Icons.Filled.AddAPhoto, contentDescription = "Ajouter une photo") }
                        }
                    }
                    items(state.photos, key = { it.id }) { photo ->
                        AsyncImage(
                            model = File(photo.filePath),
                            contentDescription = photo.caption,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp)),
                        )
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            item {
                state.plant?.let { plant ->
                    PhaseTimeline(currentPhase = plant.currentPhase, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

            item {
                state.plant?.let { plant ->
                    val genetics = state.genetics.firstOrNull { it.id == plant.geneticsId }
                    val currentPhaseStart = state.phaseHistory.lastOrNull { it.phase == plant.currentPhase && it.endDate == null }?.startDate
                    val totalDays = ChronoUnit.DAYS.between(plant.startDate, LocalDate.now())
                    val progress = currentPhaseStart?.let { com.culture.tracker.domain.phaseProgressOf(it, plant.currentPhase, genetics) }
                    val latestHeight = state.heightHistory.lastOrNull()?.heightCm

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DetailStatTile("Jour total", "$totalDays", Modifier.weight(1f))
                        DetailStatTile(
                            "Hauteur",
                            latestHeight?.let { "${it.toInt()}cm" } ?: "—",
                            Modifier.weight(1f),
                        )
                        DetailStatTile("J. de stade", progress?.daysInPhase?.toString() ?: "—", Modifier.weight(1f))
                        DetailStatTile(
                            "J. restants",
                            progress?.let { if (it.remainingDays >= 0) "${it.remainingDays}" else "+${-it.remainingDays}" } ?: "—",
                            Modifier.weight(1f),
                        )
                    }
                }
            }

            item {
                state.plant?.let { plant ->
                    val phaseColor = plant.currentPhase.themedColor()
                    val totalDays = ChronoUnit.DAYS.between(plant.startDate, LocalDate.now())
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = MaterialTheme.shapes.large) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Croissance", style = MaterialTheme.typography.titleMedium)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        "cm / jour",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = HandoffColors.TextTertiary,
                                    )
                                    IconButton(onClick = { showAddHeightDialog = true }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Filled.Add, contentDescription = "Ajouter un relevé de hauteur")
                                    }
                                }
                            }
                            GrowthChart(
                                history = state.heightHistory,
                                stageColor = phaseColor,
                                totalDay = totalDays,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }

            item { Text("Historique des phases", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp)) }
            items(state.phaseHistory, key = { it.id }) { phase ->
                val isOpenAndRevertible = phase.endDate == null && state.phaseHistory.size > 1
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart && isOpenAndRevertible) {
                            viewModel.revertToPreviousPhase(phase)
                            true
                        } else {
                            false
                        }
                    },
                )
                SwipeToDismissBox(
                    state = dismissState,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    enableDismissFromStartToEnd = false,
                    enableDismissFromEndToStart = isOpenAndRevertible,
                    backgroundContent = {
                        Box(
                            modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.medium).background(HandoffColors.Danger.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                tint = HandoffColors.Danger,
                                modifier = Modifier.padding(horizontal = 20.dp),
                            )
                        }
                    },
                ) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(36.dp).background(phase.phase.themedColor(), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(phase.phase.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text(phase.phase.label, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "Du ${phase.startDate}" + (phase.endDate?.let { " au $it" } ?: " (en cours)"),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            IconButton(onClick = { editingPhase = phase }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Modifier la date de début")
                            }
                        }
                    }
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Actions", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { showAddActionSheet = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Ajouter une action")
                    }
                }
            }
            if (state.actions.isEmpty()) {
                item {
                    Text(
                        "Aucune action enregistrée.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
            items(state.actions, key = { it.id }) { action ->
                val fertilizerName = action.fertilizerId?.let { id -> state.fertilizers.firstOrNull { it.id == id }?.name }
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(action.actionType.icon, contentDescription = null, tint = action.actionType.themedColor())
                        Column {
                            Text(
                                action.actionType.label + (fertilizerName?.let { " · $it" } ?: ""),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(action.date.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    editingPhase?.let { phase ->
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = phase.startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { editingPhase = null },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val newDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        viewModel.editPhaseHistoryDate(phase, newDate)
                    }
                    editingPhase = null
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { editingPhase = null }) { Text("Annuler") } },
        ) { DatePicker(state = datePickerState) }
    }

    if (showEditSheet) {
        state.plant?.let { plant ->
            EditPlantSheet(
                plant = plant,
                genetics = state.genetics,
                environments = state.environments,
                onCreateGenetics = { name, breeder, onCreated -> viewModel.createGenetics(name, breeder, onCreated) },
                onDismiss = { showEditSheet = false },
                onSave = { name, geneticsId, environmentId, watering, fertilizing, startDate, phase ->
                    viewModel.updatePlant(name, geneticsId, environmentId, watering, fertilizing, startDate, phase)
                    showEditSheet = false
                },
            )
        }
    }

    if (showAddActionSheet) {
        AddActionForPlantSheet(
            fertilizers = state.fertilizers,
            onDismiss = { showAddActionSheet = false },
            onSave = { actionType, date, fertilizerId, notes ->
                viewModel.addAction(actionType, date, fertilizerId, notes)
                showAddActionSheet = false
            },
        )
    }

    if (showAddHeightDialog) {
        AddHeightDialog(
            onDismiss = { showAddHeightDialog = false },
            onSave = { cm, date ->
                viewModel.addHeightMeasurement(cm, date)
                showAddHeightDialog = false
            },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer cette plante ?") },
            text = { Text("Elle sera retirée de votre jardin actif. Ses données restent stockées localement.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.archivePlant()
                    showDeleteConfirm = false
                    onBack()
                }) { Text("Supprimer", color = HandoffColors.Danger) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Annuler") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHeightDialog(onDismiss: () -> Unit, onSave: (Double, LocalDate) -> Unit) {
    var heightText by remember { mutableStateOf("") }
    val date = LocalDate.now()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un relevé de hauteur") },
        text = {
            OutlinedTextField(
                value = heightText,
                onValueChange = { heightText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Hauteur (cm)") },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { heightText.toDoubleOrNull()?.let { onSave(it, date) } },
                enabled = heightText.toDoubleOrNull() != null,
            ) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPlantSheet(
    plant: com.culture.tracker.data.local.entity.Plant,
    genetics: List<com.culture.tracker.data.local.entity.Genetics>,
    environments: List<com.culture.tracker.data.local.entity.Environment>,
    onCreateGenetics: (String, String?, (Long) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, Long?, Long?, Int?, Int?, LocalDate, GrowthPhase) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var name by remember { mutableStateOf(plant.name) }
    var selectedGeneticsId by remember { mutableStateOf(plant.geneticsId) }
    var newGeneticsName by remember { mutableStateOf("") }
    var selectedEnvironmentId by remember { mutableStateOf(plant.environmentId) }
    var selectedPhase by remember { mutableStateOf(plant.currentPhase) }
    var phaseExpanded by remember { mutableStateOf(false) }
    var wateringInterval by remember { mutableStateOf(plant.wateringIntervalDays?.toString() ?: "") }
    var fertilizingInterval by remember { mutableStateOf(plant.fertilizingIntervalDays?.toString() ?: "") }
    var startDate by remember { mutableStateOf(plant.startDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SheetHeader("Modifier la plante", onClose = onDismiss)

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom de la plante") }, modifier = Modifier.fillMaxWidth())

            ExposedDropdownMenuBox(expanded = phaseExpanded, onExpandedChange = { phaseExpanded = it }) {
                OutlinedTextField(
                    value = selectedPhase.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Phase actuelle") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = phaseExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                )
                DropdownMenu(expanded = phaseExpanded, onDismissRequest = { phaseExpanded = false }) {
                    GrowthPhase.entries.forEach { phase ->
                        DropdownMenuItem(text = { Text(phase.label) }, onClick = { selectedPhase = phase; phaseExpanded = false })
                    }
                }
            }

            DropdownField(
                label = "Génétique (variété)",
                options = genetics.map { it.id to it.name },
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
                options = environments.map { it.id to it.name },
                selectedId = selectedEnvironmentId,
                onSelect = { selectedEnvironmentId = it },
            )

            OutlinedTextField(
                value = startDate.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date de début (germination)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { TextButton(onClick = { showDatePicker = true }) { Text("Choisir") } },
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
                    onSave(
                        name,
                        selectedGeneticsId,
                        selectedEnvironmentId,
                        wateringInterval.toIntOrNull(),
                        fertilizingInterval.toIntOrNull(),
                        startDate,
                        selectedPhase,
                    )
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Enregistrer") }
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
private fun AddActionForPlantSheet(
    fertilizers: List<com.culture.tracker.data.local.entity.Fertilizer>,
    onDismiss: () -> Unit,
    onSave: (ActionType, LocalDate, Long?, String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedType by remember { mutableStateOf(ActionType.ARROSAGE) }
    var selectedFertilizerId by remember { mutableStateOf<Long?>(null) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var notes by remember { mutableStateOf("") }
    var fertilizerMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SheetHeader("Ajouter une action", onClose = onDismiss)

            Text("Type d'action", style = MaterialTheme.typography.labelMedium)
            com.culture.tracker.ui.components.ActionTypeSelector(
                selected = selectedType,
                onSelect = { selectedType = it },
            )

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
                onClick = { onSave(selectedType, date, selectedFertilizerId, notes.ifBlank { null }) },
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

@Composable
private fun DetailStatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.large) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
