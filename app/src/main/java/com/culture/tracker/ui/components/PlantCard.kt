package com.culture.tracker.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.culture.tracker.data.local.entity.Plant
import com.culture.tracker.ui.theme.themedColor
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun PlantCard(
    plant: Plant,
    thumbnailPath: String?,
    environmentName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val phaseColor = plant.currentPhase.themedColor()
    Card(onClick = onClick, modifier = modifier, shape = MaterialTheme.shapes.large) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                if (thumbnailPath != null) {
                    AsyncImage(
                        model = File(thumbnailPath),
                        contentDescription = plant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.linearGradient(listOf(phaseColor.copy(alpha = 0.55f), phaseColor.copy(alpha = 0.15f))),
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Yard, contentDescription = null, tint = Color.White, modifier = Modifier.fillMaxSize().padding(28.dp))
                    }
                }
                PhaseChip(plant.currentPhase, modifier = Modifier.align(Alignment.BottomStart).padding(8.dp))
            }
            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Text(plant.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val days = ChronoUnit.DAYS.between(plant.startDate, LocalDate.now())
                Text(
                    "Jour $days" + (environmentName?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
