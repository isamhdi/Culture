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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(viewModel: JournalViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showFertilizerSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Journal") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showFertilizerSheet = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter un engrais")
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text("Engrais enregistrés", style = MaterialTheme.typography.titleMedium) }
            if (state.fertilizers.isEmpty()) {
                item { Text("Aucun engrais.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.fertilizers.forEach { fert ->
                            Card { Text(fert.name, modifier = Modifier.padding(8.dp)) }
                        }
                    }
                }
            }

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
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
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

    if (showFertilizerSheet) {
        CreateFertilizerSheet(
            onDismiss = { showFertilizerSheet = false },
            onCreate = { name, npk, notes ->
                viewModel.createFertilizer(name, npk, notes)
                showFertilizerSheet = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateFertilizerSheet(onDismiss: () -> Unit, onCreate: (String, String?, String?) -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    var name by remember { mutableStateOf("") }
    var npk by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            com.culture.tracker.ui.components.SheetHeader("Nouvel engrais", onClose = onDismiss)
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = npk, onValueChange = { npk = it }, label = { Text("NPK (ex : 5-10-5)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
            TextButton(
                onClick = { onCreate(name, npk.ifBlank { null }, notes.ifBlank { null }) },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Créer") }
        }
    }
}
