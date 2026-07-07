package com.culture.tracker.ui.genetics

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.culture.tracker.data.local.entity.Genetics
import com.culture.tracker.domain.model.GrowthPhase
import com.culture.tracker.domain.overrideFor
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneticsScreen(onBack: () -> Unit, viewModel: GeneticsViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var editingGenetics by remember { mutableStateOf<Genetics?>(null) }
    var showCreateSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Variétés") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateSheet = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter une variété")
            }
        },
    ) { padding ->
        if (state.genetics.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Aucune variété pour le moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.genetics, key = { it.id }) { genetics ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { editingGenetics = genetics },
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(genetics.name, style = MaterialTheme.typography.titleMedium)
                                genetics.breeder?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                val customCount = GrowthPhase.entries.count { genetics.overrideFor(it) != null }
                                Text(
                                    if (customCount > 0) "$customCount durée(s) personnalisée(s)" else "Durées par défaut",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = { viewModel.deleteGenetics(genetics) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        GeneticsEditSheet(
            genetics = null,
            onDismiss = { showCreateSheet = false },
            onSave = { name, breeder, durations ->
                viewModel.createGenetics(name, breeder, durations)
                showCreateSheet = false
            },
        )
    }

    editingGenetics?.let { genetics ->
        GeneticsEditSheet(
            genetics = genetics,
            onDismiss = { editingGenetics = null },
            onSave = { name, breeder, durations ->
                viewModel.updateGenetics(genetics, name, breeder, durations)
                editingGenetics = null
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GeneticsEditSheet(
    genetics: Genetics?,
    onDismiss: () -> Unit,
    onSave: (String, String?, Map<GrowthPhase, Int?>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var name by remember { mutableStateOf(genetics?.name ?: "") }
    var breeder by remember { mutableStateOf(genetics?.breeder ?: "") }
    val durationInputs = remember {
        mutableStateOf(GrowthPhase.entries.associateWith { genetics.overrideFor(it)?.toString() ?: "" })
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            com.culture.tracker.ui.components.SheetHeader(
                if (genetics == null) "Nouvelle variété" else "Modifier la variété",
                onClose = onDismiss,
            )

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = breeder, onValueChange = { breeder = it }, label = { Text("Breeder (optionnel)") }, modifier = Modifier.fillMaxWidth())

            HorizontalDivider()
            Text("Durées de phase (jours)", style = MaterialTheme.typography.titleMedium)
            Text(
                "Laisse vide pour utiliser la durée par défaut.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            GrowthPhase.entries.forEach { phase ->
                OutlinedTextField(
                    value = durationInputs.value[phase] ?: "",
                    onValueChange = { value ->
                        durationInputs.value = durationInputs.value.toMutableMap().apply { put(phase, value.filter(Char::isDigit)) }
                    },
                    label = { Text(phase.label) },
                    placeholder = { Text("Défaut : ${phase.typicalDurationDays} j") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            TextButton(
                onClick = {
                    val durations = GrowthPhase.entries.associateWith { durationInputs.value[it]?.toIntOrNull() }
                    onSave(name, breeder.ifBlank { null }, durations)
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Enregistrer") }
        }
    }
}
