package com.culture.tracker.ui.tools

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private enum class ToolKind { STAGE_DATES, LIGHTING, SOIL_MIX, UNIT_CONVERTER }

private data class ToolEntry(val kind: ToolKind, val title: String, val description: String, val icon: ImageVector)

private val tools = listOf(
    ToolEntry(ToolKind.STAGE_DATES, "Dates de stade", "Estimer les dates des phases à partir d'une date de départ", Icons.Filled.CalendarMonth),
    ToolEntry(ToolKind.LIGHTING, "Éclairage", "Densité de puissance (W/m²) selon la surface", Icons.Filled.LightMode),
    ToolEntry(ToolKind.SOIL_MIX, "Mélange de terreau", "Répartir un volume total selon des proportions", Icons.Filled.Grain),
    ToolEntry(ToolKind.UNIT_CONVERTER, "Conversion d'unités", "Volume, température et longueur", Icons.Filled.SwapHoriz),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(onBack: () -> Unit) {
    var openTool by remember { mutableStateOf<ToolKind?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Outils de culture") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(tools) { tool ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { openTool = tool.kind },
                    shape = MaterialTheme.shapes.large,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(tool.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(tool.title, style = MaterialTheme.typography.titleMedium)
                            Text(tool.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    when (openTool) {
        ToolKind.STAGE_DATES -> StageDatesCalculatorSheet(onDismiss = { openTool = null })
        ToolKind.LIGHTING -> LightingCalculatorSheet(onDismiss = { openTool = null })
        ToolKind.SOIL_MIX -> SoilMixCalculatorSheet(onDismiss = { openTool = null })
        ToolKind.UNIT_CONVERTER -> UnitConverterSheet(onDismiss = { openTool = null })
        null -> Unit
    }
}
