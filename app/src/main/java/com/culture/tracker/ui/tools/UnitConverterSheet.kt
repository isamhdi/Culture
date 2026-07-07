package com.culture.tracker.ui.tools

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private enum class ConversionCategory(val label: String, val fromUnit: String, val toUnit: String) {
    VOLUME("Volume", "mL", "L"),
    TEMPERATURE("Température", "°C", "°F"),
    LENGTH("Longueur", "cm", "inch"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    var category by remember { mutableStateOf(ConversionCategory.VOLUME) }
    var inputValue by remember { mutableStateOf("") }

    val input = inputValue.toDoubleOrNull()
    val output = input?.let {
        when (category) {
            ConversionCategory.VOLUME -> it / 1000.0
            ConversionCategory.TEMPERATURE -> it * 9.0 / 5.0 + 32.0
            ConversionCategory.LENGTH -> it / 2.54
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Conversion d'unités", style = MaterialTheme.typography.titleLarge)

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ConversionCategory.entries.forEachIndexed { index, cat ->
                    SegmentedButton(
                        selected = category == cat,
                        onClick = { category = cat; inputValue = "" },
                        shape = SegmentedButtonDefaults.itemShape(index, ConversionCategory.entries.size),
                    ) { Text(cat.label) }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                    label = { Text(category.fromUnit) },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = output?.let { "%.2f".format(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(category.toUnit) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
