package com.culture.tracker.ui.garden.environments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentsScreen(onEnvironmentClick: (Long) -> Unit = {}, viewModel: EnvironmentsViewModel = koinViewModel()) {
    val environments by viewModel.environments.collectAsState()
    var showCreateSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Environnements") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateSheet = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter un environnement")
            }
        },
    ) { padding ->
        if (environments.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                Text("Aucun environnement pour le moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(environments, key = { it.id }) { env ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onEnvironmentClick(env.id) },
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier.size(44.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = androidx.compose.ui.Alignment.Center,
                                ) {
                                    Icon(Icons.Filled.Yard, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Column {
                                    Text(env.name, style = MaterialTheme.typography.titleMedium)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        AssistChip(
                                            onClick = {},
                                            enabled = false,
                                            label = { Text("${env.lightHoursPerDay}h/j") },
                                            leadingIcon = { Icon(Icons.Filled.LightMode, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                            colors = AssistChipDefaults.assistChipColors(disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant, disabledLeadingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                        )
                                        if (env.lightingType != null) {
                                            AssistChip(
                                                onClick = {},
                                                enabled = false,
                                                label = { Text(env.lightingType + (env.lightingPowerWatts?.let { " ${it}W" } ?: "")) },
                                                colors = AssistChipDefaults.assistChipColors(disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                            )
                                        }
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.deleteEnvironment(env) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateEnvironmentSheet(
            onDismiss = { showCreateSheet = false },
            onCreate = { name, hours, size, material, lightType, power, spectrum, model ->
                viewModel.createEnvironment(name, hours, size, material, lightType, power, spectrum, model)
                showCreateSheet = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEnvironmentSheet(
    onDismiss: () -> Unit,
    onCreate: (String, Double, String?, String?, String?, Int?, String?, String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var name by remember { mutableStateOf("") }
    var lightHours by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var material by remember { mutableStateOf("") }
    var lightingType by remember { mutableStateOf("") }
    var lightingPower by remember { mutableStateOf("") }
    var lightingSpectrum by remember { mutableStateOf("") }
    var lightingModel by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Nouvel environnement", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = lightHours,
                onValueChange = { lightHours = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Heures de lumière/jour") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(value = size, onValueChange = { size = it }, label = { Text("Taille (ex : 80x80x160cm)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = material, onValueChange = { material = it }, label = { Text("Matériel") }, modifier = Modifier.fillMaxWidth())

            HorizontalDivider()
            Text("Éclairage", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = lightingType, onValueChange = { lightingType = it }, label = { Text("Type (LED, HPS, CMH...)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = lightingPower,
                onValueChange = { lightingPower = it.filter(Char::isDigit) },
                label = { Text("Puissance (W)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(value = lightingSpectrum, onValueChange = { lightingSpectrum = it }, label = { Text("Spectre") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = lightingModel, onValueChange = { lightingModel = it }, label = { Text("Modèle") }, modifier = Modifier.fillMaxWidth())

            TextButton(
                onClick = {
                    onCreate(
                        name,
                        lightHours.toDoubleOrNull() ?: 0.0,
                        size.ifBlank { null },
                        material.ifBlank { null },
                        lightingType.ifBlank { null },
                        lightingPower.toIntOrNull(),
                        lightingSpectrum.ifBlank { null },
                        lightingModel.ifBlank { null },
                    )
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Créer l'environnement") }
        }
    }
}
