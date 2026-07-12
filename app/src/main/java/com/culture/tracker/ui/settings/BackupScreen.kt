package com.culture.tracker.ui.settings

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onBack: () -> Unit, viewModel: BackupViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) viewModel.exportTo(uri)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
            showRestoreConfirm = true
        }
    }

    LaunchedEffect(state.restoreSucceeded) {
        if (state.restoreSucceeded) restartApp(context)
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text("Restaurer cette sauvegarde ?") },
            text = {
                Text(
                    "Toutes les données actuelles (plantes, environnements, relevés, photos) vont être " +
                        "remplacées par celles du fichier sélectionné. Cette action est irréversible et " +
                        "l'application va redémarrer.",
                )
            },
            confirmButton = {
                Button(onClick = {
                    showRestoreConfirm = false
                    pendingRestoreUri?.let { viewModel.restoreFrom(it) }
                }) { Text("Restaurer") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRestoreConfirm = false }) { Text("Annuler") }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sauvegarde") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxWidth().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Exporter", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Crée un fichier contenant toutes tes données (plantes, environnements, relevés, photos). " +
                        "Garde-le où tu veux : stockage du téléphone, cloud, etc. Pratique avant une mise à jour " +
                        "ou un changement d'appareil.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = { exportLauncher.launch(defaultBackupFileName()) },
                    enabled = !state.isWorking,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Exporter une sauvegarde") }
            }

            HorizontalDivider()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Restaurer", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Remplace toutes les données actuelles par celles d'un fichier exporté précédemment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
                    enabled = !state.isWorking,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Restaurer une sauvegarde") }
            }

            if (state.isWorking) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Text("Traitement en cours…")
                    }
                }
            }

            state.message?.let { message ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(message, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private fun defaultBackupFileName(): String {
    val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
    return "pousse_backup_$timestamp.zip"
}

private fun restartApp(context: Context) {
    val packageManager = context.packageManager
    val intent = packageManager.getLaunchIntentForPackage(context.packageName) ?: return
    val restartIntent = Intent.makeRestartActivityTask(intent.component)
    context.startActivity(restartIntent)
    Runtime.getRuntime().exit(0)
}
