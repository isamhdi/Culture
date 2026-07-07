package com.culture.tracker.ui.tools

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightingCalculatorSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    var lengthCm by remember { mutableStateOf("100") }
    var widthCm by remember { mutableStateOf("100") }
    var wattage by remember { mutableStateOf("300") }

    val areaM2 = ((lengthCm.toDoubleOrNull() ?: 0.0) / 100.0) * ((widthCm.toDoubleOrNull() ?: 0.0) / 100.0)
    val density = wattage.toDoubleOrNull()?.let { if (areaM2 > 0) it / areaM2 else null }
    val (label, color) = densityLabel(density)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            com.culture.tracker.ui.components.SheetHeader("Éclairage", onClose = onDismiss)
            Text(
                "Calcule la densité de puissance (W/m²) de votre tente selon sa surface.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = lengthCm,
                    onValueChange = { lengthCm = it.filter { c -> c.isDigit() } },
                    label = { Text("Longueur (cm)") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = widthCm,
                    onValueChange = { widthCm = it.filter { c -> c.isDigit() } },
                    label = { Text("Largeur (cm)") },
                    modifier = Modifier.weight(1f),
                )
            }
            OutlinedTextField(
                value = wattage,
                onValueChange = { wattage = it.filter { c -> c.isDigit() } },
                label = { Text("Puissance de la lampe (W)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Card(colors = CardDefaults.cardColors(containerColor = color)) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Surface : %.2f m²".format(areaM2), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        density?.let { "%.0f W/m²".format(it) } ?: "—",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Text(
                "Valeurs indicatives — à adapter selon le type de lampe (LED, HPS, CMH) et son efficacité réelle.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun densityLabel(density: Double?): Pair<String, androidx.compose.ui.graphics.Color> = when {
    density == null -> "Renseignez une surface et une puissance" to MaterialTheme.colorScheme.surfaceVariant
    density < 200 -> "Faible — convient à la propagation/semis" to MaterialTheme.colorScheme.secondaryContainer
    density < 400 -> "Modérée — adaptée à la croissance" to MaterialTheme.colorScheme.tertiaryContainer
    density < 600 -> "Élevée — adaptée à la floraison" to MaterialTheme.colorScheme.primaryContainer
    else -> "Très élevée — vérifier la dissipation thermique" to MaterialTheme.colorScheme.errorContainer
}
