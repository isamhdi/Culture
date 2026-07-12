package com.culture.tracker.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.CheckCircleOutline
import com.culture.tracker.domain.model.ActionType
import com.culture.tracker.domain.model.GrowthPhase
import com.culture.tracker.ui.components.DotSpec
import com.culture.tracker.ui.components.MonthCalendar
import java.time.LocalDate
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

private enum class SheetMode { ACTION, PHASE, READING }

/**
 * Petites particules qui s'échappent du bouton de validation en spirale (angle et rayon
 * augmentent ensemble) puis s'estompent — le petit geste "satisfaisant" demandé pour marquer
 * une action comme faite.
 */
@Composable
private fun SpiralCheckBurst(progress: Float, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (progress <= 0f || progress >= 1f) return@Canvas
        val particleCount = 8
        val maxRadius = size.minDimension / 2f
        val eased = 1f - (1f - progress) * (1f - progress)
        repeat(particleCount) { i ->
            val baseAngle = i * (360f / particleCount)
            val angleDeg = baseAngle + eased * 360f * 1.5f
            val radius = eased * maxRadius
            val rad = Math.toRadians(angleDeg.toDouble())
            val x = center.x + radius * kotlin.math.cos(rad).toFloat()
            val y = center.y + radius * kotlin.math.sin(rad).toFloat()
            val alpha = (1f - eased).coerceIn(0f, 1f)
            val particleRadius = (2.2f + 1.5f * (1f - eased)).dp.toPx()
            drawCircle(color = color.copy(alpha = alpha), radius = particleRadius, center = Offset(x, y))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var sheetMode by remember { mutableStateOf<SheetMode?>(null) }
    var prefillPlantId by remember { mutableStateOf<Long?>(null) }
    var prefillType by remember { mutableStateOf<ActionType?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Calendrier") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                prefillPlantId = null
                prefillType = null
                sheetMode = SheetMode.ACTION
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Card {
                    MonthCalendar(
                        yearMonth = state.visibleMonth,
                        selectedDate = state.selectedDate,
                        dotsForDate = { date ->
                            val done = state.actionsInMonth.filter { it.date == date }
                                .map { DotSpec(Color(it.actionType.colorHex), filled = true) }
                            val due = state.predictedActions.filter { it.date == date }
                                .map { DotSpec(Color(it.actionType.colorHex), filled = false) }
                            done + due
                        },
                        onDateClick = viewModel::onDateSelected,
                        onPreviousMonth = viewModel::onPreviousMonth,
                        onNextMonth = viewModel::onNextMonth,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { sheetMode = SheetMode.PHASE }) { Text("Changer une phase") }
                    TextButton(onClick = { sheetMode = SheetMode.READING }) { Text("Relevé T°/Humidité") }
                }
            }

            if (state.predictedForSelectedDay.isNotEmpty()) {
                item { Text("À faire le ${state.selectedDate}", style = MaterialTheme.typography.titleMedium) }
                items(state.predictedForSelectedDay, key = { "${it.plantId}-${it.actionType}-${it.date}" }) { predicted ->
                    val scope = rememberCoroutineScope()
                    val haptics = LocalHapticFeedback.current
                    val checkScale = remember { Animatable(1f) }
                    val spiralProgress = remember { Animatable(0f) }
                    var isCompleting by remember { mutableStateOf(false) }
                    Card(
                        onClick = {
                            prefillPlantId = predicted.plantId
                            prefillType = predicted.actionType
                            sheetMode = SheetMode.ACTION
                        },
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier.size(10.dp)
                                        .background(androidx.compose.ui.graphics.Color.Transparent, CircleShape)
                                        .border(androidx.compose.foundation.BorderStroke(1.5.dp, Color(predicted.actionType.colorHex)), CircleShape),
                                )
                                Text("${predicted.actionType.label} · ${predicted.plantName}", style = MaterialTheme.typography.bodyLarge)
                            }
                            Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                SpiralCheckBurst(
                                    progress = spiralProgress.value,
                                    color = Color(predicted.actionType.colorHex),
                                    modifier = Modifier.size(56.dp),
                                )
                                IconButton(
                                    onClick = {
                                        if (isCompleting) return@IconButton
                                        isCompleting = true
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        scope.launch {
                                            checkScale.animateTo(1.4f, animationSpec = tween(100))
                                            checkScale.animateTo(
                                                1f,
                                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                                            )
                                        }
                                        scope.launch {
                                            spiralProgress.snapTo(0f)
                                            spiralProgress.animateTo(1f, animationSpec = tween(550, easing = FastOutSlowInEasing))
                                        }
                                        scope.launch {
                                            // Laisse l'animation se jouer avant de faire disparaître l'item de la liste
                                            // (sinon la recomposition liée à l'ajout en base coupe le geste avant la fin).
                                            kotlinx.coroutines.delay(500)
                                            viewModel.addAction(predicted.plantId, predicted.actionType, predicted.date, null, null)
                                        }
                                    },
                                ) {
                                    Icon(
                                        Icons.Filled.CheckCircleOutline,
                                        contentDescription = "Marquer comme fait",
                                        modifier = Modifier.scale(checkScale.value),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Text("Actions faites le ${state.selectedDate}", style = MaterialTheme.typography.titleMedium) }

            if (state.actionsForSelectedDay.isEmpty()) {
                item { Text("Aucune action ce jour.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(state.actionsForSelectedDay, key = { it.id }) { action ->
                    val plantName = state.plants.firstOrNull { it.id == action.plantId }?.name ?: "?"
                    val fertilizerName = action.fertilizerId?.let { id -> state.fertilizers.firstOrNull { it.id == id }?.name }
                    Card(modifier = Modifier.fillMaxWidth().animateItem()) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier.size(10.dp).background(Color(action.actionType.colorHex), CircleShape),
                                    )
                                    Text(
                                        "${action.actionType.label} · $plantName" + (fertilizerName?.let { " ($it)" } ?: ""),
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                }
                                action.notes?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                            }
                            IconButton(onClick = { viewModel.deleteAction(action) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                            }
                        }
                    }
                }
            }
        }
    }

    when (sheetMode) {
        SheetMode.ACTION -> AddActionSheet(
            state = state,
            defaultDate = state.selectedDate,
            prefillPlantId = prefillPlantId,
            prefillType = prefillType,
            onDismiss = { sheetMode = null },
            viewModel = viewModel,
        )
        SheetMode.PHASE -> ChangePhaseSheet(state = state, defaultDate = state.selectedDate, onDismiss = { sheetMode = null }, viewModel = viewModel)
        SheetMode.READING -> RecordReadingSheet(state = state, onDismiss = { sheetMode = null }, viewModel = viewModel)
        null -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddActionSheet(
    state: CalendarUiState,
    defaultDate: LocalDate,
    onDismiss: () -> Unit,
    viewModel: CalendarViewModel,
    prefillPlantId: Long? = null,
    prefillType: ActionType? = null,
) {
    val sheetState = rememberModalBottomSheetState()
    var selectedPlantId by remember { mutableStateOf(prefillPlantId ?: state.plants.firstOrNull()?.id) }
    var selectedType by remember { mutableStateOf(prefillType ?: ActionType.ARROSAGE) }
    var selectedFertilizerId by remember { mutableStateOf<Long?>(null) }
    var date by remember { mutableStateOf(defaultDate) }
    var notes by remember { mutableStateOf("") }
    var plantMenuExpanded by remember { mutableStateOf(false) }
    var fertilizerMenuExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            com.culture.tracker.ui.components.SheetHeader("Ajouter une action", onClose = onDismiss)

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
                onValueChange = { runCatching { date = LocalDate.parse(it) } },
                label = { Text("Date (AAAA-MM-JJ) — modifiable rétroactivement") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())

            TextButton(
                onClick = {
                    selectedPlantId?.let { plantId ->
                        viewModel.addAction(plantId, selectedType, date, selectedFertilizerId, notes.ifBlank { null })
                    }
                    onDismiss()
                },
                enabled = selectedPlantId != null,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Enregistrer") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePhaseSheet(state: CalendarUiState, defaultDate: LocalDate, onDismiss: () -> Unit, viewModel: CalendarViewModel) {
    val sheetState = rememberModalBottomSheetState()
    var selectedPlantId by remember { mutableStateOf(state.plants.firstOrNull()?.id) }
    var selectedPhase by remember { mutableStateOf(GrowthPhase.GERMINATION) }
    var date by remember { mutableStateOf(defaultDate) }
    var plantMenuExpanded by remember { mutableStateOf(false) }
    var phaseMenuExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            com.culture.tracker.ui.components.SheetHeader("Changer de phase", onClose = onDismiss)
            Text(
                "La date peut être définie dans le passé pour corriger l'historique.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

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

            ExposedDropdownMenuBox(expanded = phaseMenuExpanded, onExpandedChange = { phaseMenuExpanded = it }) {
                OutlinedTextField(
                    value = selectedPhase.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Nouvelle phase") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = phaseMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                )
                DropdownMenu(expanded = phaseMenuExpanded, onDismissRequest = { phaseMenuExpanded = false }) {
                    GrowthPhase.entries.forEach { phase ->
                        DropdownMenuItem(text = { Text(phase.label) }, onClick = { selectedPhase = phase; phaseMenuExpanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = date.toString(),
                onValueChange = { runCatching { date = LocalDate.parse(it) } },
                label = { Text("Date d'effet (AAAA-MM-JJ)") },
                modifier = Modifier.fillMaxWidth(),
            )

            TextButton(
                onClick = {
                    selectedPlantId?.let { viewModel.changePlantPhase(it, selectedPhase, date) }
                    onDismiss()
                },
                enabled = selectedPlantId != null,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Valider") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordReadingSheet(state: CalendarUiState, onDismiss: () -> Unit, viewModel: CalendarViewModel) {
    val sheetState = rememberModalBottomSheetState()
    var selectedEnvId by remember { mutableStateOf(state.environments.firstOrNull()?.id) }
    var temperature by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var envMenuExpanded by remember { mutableStateOf(false) }
    var recordedAt by remember { mutableStateOf(java.time.LocalDateTime.now()) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            com.culture.tracker.ui.components.SheetHeader("Relevé température / humidité", onClose = onDismiss)

            ExposedDropdownMenuBox(expanded = envMenuExpanded, onExpandedChange = { envMenuExpanded = it }) {
                OutlinedTextField(
                    value = state.environments.firstOrNull { it.id == selectedEnvId }?.name ?: "Choisir un environnement",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Environnement") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = envMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                )
                DropdownMenu(expanded = envMenuExpanded, onDismissRequest = { envMenuExpanded = false }) {
                    state.environments.forEach { env ->
                        DropdownMenuItem(text = { Text(env.name) }, onClick = { selectedEnvId = env.id; envMenuExpanded = false })
                    }
                }
            }

            com.culture.tracker.ui.components.DateTimePickerRow(
                dateTime = recordedAt,
                onDateTimeChange = { recordedAt = it },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                    label = { Text("Température (°C)") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = humidity,
                    onValueChange = { humidity = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Humidité (%)") },
                    modifier = Modifier.weight(1f),
                )
            }

            TextButton(
                onClick = {
                    val envId = selectedEnvId
                    val temp = temperature.toDoubleOrNull()
                    val hum = humidity.toDoubleOrNull()
                    if (envId != null && temp != null && hum != null) {
                        viewModel.recordReading(envId, temp, hum, recordedAt)
                    }
                    onDismiss()
                },
                enabled = selectedEnvId != null && temperature.toDoubleOrNull() != null && humidity.toDoubleOrNull() != null,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Enregistrer") }
        }
    }
}
