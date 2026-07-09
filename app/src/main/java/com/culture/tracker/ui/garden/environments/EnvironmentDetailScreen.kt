package com.culture.tracker.ui.garden.environments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.culture.tracker.domain.model.EnvironmentMeasurementType
import com.culture.tracker.ui.components.ChartPoint
import com.culture.tracker.ui.components.SimpleLineChart
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private val dayFormatter = DateTimeFormatter.ofPattern("dd/MM")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentDetailScreen(
    environmentId: Long,
    onBack: () -> Unit,
    viewModel: EnvironmentDetailViewModel = koinViewModel(parameters = { parametersOf(environmentId) }),
) {
    val state by viewModel.uiState.collectAsState()
    var showReadingSheet by remember { mutableStateOf(false) }
    var showLogSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.environment?.name ?: "Environnement") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showReadingSheet = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter un relevé")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                state.environment?.let { env ->
                    Column {
                        Text("${env.lightHoursPerDay}h de lumière/jour", style = MaterialTheme.typography.bodyLarge)
                        env.sizeDescription?.let { Text("Taille : $it", style = MaterialTheme.typography.bodyMedium) }
                        env.materialDescription?.let { Text("Matériel : $it", style = MaterialTheme.typography.bodyMedium) }
                        env.lightingType?.let {
                            Text(
                                "Éclairage : $it" + (env.lightingPowerWatts?.let { w -> " · ${w}W" } ?: ""),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            item { HorizontalDivider() }

            item {
                Card(shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Température (°C)", style = MaterialTheme.typography.titleMedium)
                        SimpleLineChart(
                            points = state.readings.map { ChartPoint(it.recordedAt.format(dayFormatter), it.temperatureCelsius.toFloat()) },
                            seriesColor = Color(0xFF2A78D6),
                            valueFormatter = { "%.1f°C".format(it) },
                        )
                    }
                }
            }

            item {
                Card(shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Humidité (%)", style = MaterialTheme.typography.titleMedium)
                        SimpleLineChart(
                            points = state.readings.map { ChartPoint(it.recordedAt.format(dayFormatter), it.humidityPercent.toFloat()) },
                            seriesColor = Color(0xFF1BAF7A),
                            valueFormatter = { "%.0f%%".format(it) },
                        )
                    }
                }
            }

            item { Text("Historique des relevés", style = MaterialTheme.typography.titleMedium) }

            if (state.readings.isEmpty()) {
                item { Text("Aucun relevé pour le moment.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                state.readings.reversed().forEach { reading ->
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(reading.recordedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), style = MaterialTheme.typography.bodyMedium)
                                Text("${reading.temperatureCelsius}°C · ${reading.humidityPercent}%", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            item { HorizontalDivider() }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text("Relevés", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { showLogSheet = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Ajouter un relevé libre")
                    }
                }
            }
            if (state.logs.isEmpty()) {
                item { Text("Aucun relevé enregistré.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(state.logs, key = { it.id }) { log ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                val measureText = log.measurementType?.let { type ->
                                    "${type.label} : ${log.measurementValue} ${type.unit}".trim()
                                }
                                Text(measureText ?: log.note.orEmpty().ifBlank { "Note" }, style = MaterialTheme.typography.bodyLarge)
                                if (measureText != null && !log.note.isNullOrBlank()) {
                                    Text(log.note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(log.date.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { viewModel.deleteEnvironmentLog(log) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Supprimer le relevé")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReadingSheet) {
        AddReadingSheet(
            onDismiss = { showReadingSheet = false },
            onSave = { temp, hum ->
                viewModel.recordReading(temp, hum)
                showReadingSheet = false
            },
        )
    }

    if (showLogSheet) {
        AddEnvironmentLogSheet(
            onDismiss = { showLogSheet = false },
            onSave = { date, note, type, value ->
                viewModel.addEnvironmentLog(date, note, type, value)
                showLogSheet = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReadingSheet(onDismiss: () -> Unit, onSave: (Double, Double) -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    var temperature by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            com.culture.tracker.ui.components.SheetHeader("Nouveau relevé", onClose = onDismiss)
            OutlinedTextField(
                value = temperature,
                onValueChange = { temperature = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                label = { Text("Température (°C)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = humidity,
                onValueChange = { humidity = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Humidité (%)") },
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(
                onClick = {
                    val t = temperature.toDoubleOrNull()
                    val h = humidity.toDoubleOrNull()
                    if (t != null && h != null) onSave(t, h)
                },
                enabled = temperature.toDoubleOrNull() != null && humidity.toDoubleOrNull() != null,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Enregistrer") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEnvironmentLogSheet(
    onDismiss: () -> Unit,
    onSave: (LocalDate, String?, EnvironmentMeasurementType?, Double?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var date by remember { mutableStateOf(LocalDate.now()) }
    var note by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<EnvironmentMeasurementType?>(null) }
    var valueText by remember { mutableStateOf("") }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            com.culture.tracker.ui.components.SheetHeader("Ajouter un relevé", onClose = onDismiss)

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
                    EnvironmentMeasurementType.entries.forEach { type ->
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
                onClick = { onSave(date, note.ifBlank { null }, selectedType, valueText.toDoubleOrNull()) },
                enabled = note.isNotBlank() || (selectedType != null && valueText.toDoubleOrNull() != null),
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
