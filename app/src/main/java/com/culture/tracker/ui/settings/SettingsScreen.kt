package com.culture.tracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.culture.tracker.R
import com.culture.tracker.data.repository.ThemeMode
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onOpenTools: () -> Unit = {}, onOpenGenetics: () -> Unit = {}, viewModel: SettingsViewModel = koinViewModel()) {
    val settings by viewModel.settings.collectAsState()

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
                            "Gérer les génétiques et leurs durées de phase",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }

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
            Text(
                "Toutes les données (plantes, photos, relevés) sont stockées uniquement sur cet appareil. Aucune information n'est envoyée sur internet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
