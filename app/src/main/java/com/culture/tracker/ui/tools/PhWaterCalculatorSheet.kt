package com.culture.tracker.ui.tools

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import kotlin.math.abs

/**
 * Estime la quantité de correcteur pH ("pH Up"/"pH Down") à ajouter à un volume d'eau,
 * à partir d'une dose de référence (mL par litre pour corriger 0,1 pH) que l'utilisateur
 * calibre selon son propre produit — les concentrations variant fortement d'une marque à l'autre.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhWaterCalculatorSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    var volumeText by remember { mutableStateOf("") }
    var currentPhText by remember { mutableStateOf("") }
    var targetPhText by remember { mutableStateOf("") }
    var doseText by remember { mutableStateOf("1.0") }

    val volume = volumeText.toDoubleOrNull()
    val currentPh = currentPhText.toDoubleOrNull()
    val targetPh = targetPhText.toDoubleOrNull()
    val dose = doseText.toDoubleOrNull()

    val delta = if (currentPh != null && targetPh != null) targetPh - currentPh else null
    val totalMl = if (volume != null && dose != null && delta != null) {
        volume * dose * (abs(delta) / 0.1)
    } else {
        null
    }
    val productLabel = when {
        delta == null || delta == 0.0 -> null
        delta > 0 -> "pH Up"
        else -> "pH Down"
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            com.culture.tracker.ui.components.SheetHeader("Calculateur pH / Eau", onClose = onDismiss)

            OutlinedTextField(
                value = volumeText,
                onValueChange = { volumeText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Volume d'eau (L)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = currentPhText,
                    onValueChange = { currentPhText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("pH actuel") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = targetPhText,
                    onValueChange = { targetPhText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("pH cible") },
                    modifier = Modifier.weight(1f),
                )
            }

            OutlinedTextField(
                value = doseText,
                onValueChange = { doseText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Dose du produit (mL/L pour 0,1 pH)") },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "À calibrer selon ton produit : les correcteurs pH n'ont pas tous la même concentration.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider()

            if (totalMl != null && productLabel != null) {
                Text("Produit à utiliser : $productLabel", style = MaterialTheme.typography.titleMedium)
                Text("Quantité estimée : %.1f mL".format(totalMl), style = MaterialTheme.typography.titleMedium)
            } else {
                Text(
                    "Renseigne le volume, le pH actuel et le pH cible pour estimer la dose.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
