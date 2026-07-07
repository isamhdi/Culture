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

private data class SoilComponent(val name: String, var percent: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoilMixCalculatorSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    var totalVolume by remember { mutableStateOf("50") }
    var terreauPercent by remember { mutableStateOf("70") }
    var perlitePercent by remember { mutableStateOf("20") }
    var cocoPercent by remember { mutableStateOf("10") }

    val total = totalVolume.toDoubleOrNull() ?: 0.0
    val pTerreau = terreauPercent.toDoubleOrNull() ?: 0.0
    val pPerlite = perlitePercent.toDoubleOrNull() ?: 0.0
    val pCoco = cocoPercent.toDoubleOrNull() ?: 0.0
    val sum = pTerreau + pPerlite + pCoco

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            com.culture.tracker.ui.components.SheetHeader("Mélange de terreau", onClose = onDismiss)
            Text(
                "Répartit un volume total selon des proportions (%).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = totalVolume,
                onValueChange = { totalVolume = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Volume total (L)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = terreauPercent,
                    onValueChange = { terreauPercent = it.filter { c -> c.isDigit() } },
                    label = { Text("Terreau %") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = perlitePercent,
                    onValueChange = { perlitePercent = it.filter { c -> c.isDigit() } },
                    label = { Text("Perlite %") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = cocoPercent,
                    onValueChange = { cocoPercent = it.filter { c -> c.isDigit() } },
                    label = { Text("Coco %") },
                    modifier = Modifier.weight(1f),
                )
            }

            if (sum.toInt() != 100) {
                Text(
                    "La somme des proportions est de ${sum.toInt()}% (idéalement 100%).",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Terreau : %.1f L".format(total * pTerreau / 100.0), style = MaterialTheme.typography.bodyLarge)
                    Text("Perlite : %.1f L".format(total * pPerlite / 100.0), style = MaterialTheme.typography.bodyLarge)
                    Text("Coco : %.1f L".format(total * pCoco / 100.0), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
