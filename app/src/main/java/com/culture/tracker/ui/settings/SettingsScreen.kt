package com.culture.tracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.culture.tracker.R
import com.culture.tracker.data.repository.ThemeMode
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenTools: () -> Unit = {},
    onOpenGenetics: () -> Unit = {},
    onOpenFertilizers: () -> Unit = {},
    onOpenArchive: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    onOpenBackup: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val settings by viewModel.settings.collectAsState()
    val isLoaded by viewModel.isLoaded.collectAsState()
    // Ne se synchronise qu'une fois, dès que la vraie valeur DataStore est connue (isLoaded) :
    // au-delà, le champ local reste seul maître pour ne pas écraser une saisie en cours par un
    // aller-retour DataStore en retard (une résync sur chaque frappe provoquait des pertes de
    // caractères). Attendre isLoaded évite aussi de figer le champ sur la valeur par défaut
    // ("") émise avant que DataStore n'ait livré le vrai prénom enregistré.
    var nameInput by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(isLoaded, settings.userName) {
        if (isLoaded && nameInput == null) nameInput = settings.userName ?: ""
    }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_settings)) }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxWidth().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenTools,
                shape = MaterialTheme.shapes.large,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text("Outils de culture", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Dates de stade, éclairage, terreau, conversions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenGenetics,
                shape = MaterialTheme.shapes.large,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Yard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text("Variétés", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Gérer les variétés et leurs durées de phase",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenFertilizers,
                shape = MaterialTheme.shapes.large,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Science, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text("Engrais", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Gérer les engrais disponibles",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenArchive,
                shape = MaterialTheme.shapes.large,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Archive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text("Archives", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Plantes archivées ou déclarées mortes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenBackup,
                shape = MaterialTheme.shapes.large,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text("Sauvegarde", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Exporter ou restaurer toutes tes données",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenAbout,
                shape = MaterialTheme.shapes.large,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text("À propos", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Version de l'application",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }

            Text("Profil", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = nameInput ?: "",
                onValueChange = { nameInput = it; viewModel.setUserName(it) },
                label = { Text("Prénom (optionnel)") },
                supportingText = { Text("Utilisé de temps en temps dans le message d'accueil.") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider()

            Text("Apparence", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val modes = ThemeMode.entries
                modes.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = settings.themeMode == mode,
                        onClick = { viewModel.setThemeMode(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                    ) {
                        Text(
                            when (mode) {
                                ThemeMode.SYSTEM -> "Système"
                                ThemeMode.LIGHT -> "Jour"
                                ThemeMode.DARK -> "Nuit"
                            },
                        )
                    }
                }
            }

            HorizontalDivider()

            Text("Notifications", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Rappels d'arrosage et d'engrais")
                Switch(checked = settings.notificationsEnabled, onCheckedChange = viewModel::setNotificationsEnabled)
            }

            Text("Heure de rappel : ${settings.reminderHour}h", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = settings.reminderHour.toFloat(),
                onValueChange = { viewModel.setReminderHour(it.toInt()) },
                valueRange = 0f..23f,
                steps = 22,
            )

            HorizontalDivider()

            Text("Réseau", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f).padding(end = 12.dp)) {
                    Text("Capteurs externes")
                    Text(
                        "Autorise l'app à interroger Home Assistant ou une URL de capteur sur ton réseau local pour relever température/humidité automatiquement.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = settings.networkSensorsEnabled, onCheckedChange = viewModel::setNetworkSensorsEnabled)
            }

            HorizontalDivider()
            Text(
                if (settings.networkSensorsEnabled) {
                    "Toutes les données (plantes, photos, relevés) sont stockées uniquement sur cet appareil. Les capteurs externes étant activés, l'app peut effectuer des requêtes réseau vers les sources que tu configures toi-même (Home Assistant, URL de capteur) — aucune autre donnée n'est envoyée où que ce soit."
                } else {
                    "Toutes les données (plantes, photos, relevés) sont stockées uniquement sur cet appareil. Aucune information n'est envoyée sur internet."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
