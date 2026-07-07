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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.culture.tracker.data.local.entity.PhaseHistory
import com.culture.tracker.domain.model.GrowthPhase
import com.culture.tracker.ui.components.PhaseChip
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
                },
            )
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
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Text("Progression des phases", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val reachedPhases = state.phaseHistory.map { it.phase }.toSet()
                        GrowthPhase.entries.forEach { phase ->
                            val reached = phase in reachedPhases
                            Box(
                                modifier = Modifier.weight(1f).height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (reached) phase.themedColor() else MaterialTheme.colorScheme.surfaceVariant),
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(GrowthPhase.entries.first().label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(GrowthPhase.entries.last().label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item { Text("Historique des phases", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp)) }
            items(state.phaseHistory, key = { it.id }) { phase ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
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

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }
            item { Text("Actions", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp)) }
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
                onSave = { name, geneticsId, environmentId, watering, fertilizing, startDate ->
                    viewModel.updatePlant(name, geneticsId, environmentId, watering, fertilizing, startDate)
                    showEditSheet = false
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPlantSheet(
    plant: com.culture.tracker.data.local.entity.Plant,
    genetics: List<com.culture.tracker.data.local.entity.Genetics>,
    environments: List<com.culture.tracker.data.local.entity.Environment>,
    onCreateGenetics: (String, String?, (Long) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, Long?, Long?, Int?, Int?, LocalDate) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var name by remember { mutableStateOf(plant.name) }
    var selectedGeneticsId by remember { mutableStateOf(plant.geneticsId) }
    var newGeneticsName by remember { mutableStateOf("") }
    var selectedEnvironmentId by remember { mutableStateOf(plant.environmentId) }
    var wateringInterval by remember { mutableStateOf(plant.wateringIntervalDays?.toString() ?: "") }
    var fertilizingInterval by remember { mutableStateOf(plant.fertilizingIntervalDays?.toString() ?: "") }
    var startDate by remember { mutableStateOf(plant.startDate) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Modifier la plante", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom de la plante") }, modifier = Modifier.fillMaxWidth())

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
