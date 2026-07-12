package com.culture.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.culture.tracker.data.local.entity.Genetics
import com.culture.tracker.data.local.entity.PhaseHistory
import com.culture.tracker.data.local.entity.Plant
import com.culture.tracker.domain.phaseProgressOf
import com.culture.tracker.ui.theme.onBadgeColor
import com.culture.tracker.ui.theme.themedColor
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun PlantCard(
    plant: Plant,
    thumbnailPath: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    genetics: Genetics? = null,
    openPhase: PhaseHistory? = null,
    heightCm: Double? = null,
) {
    val phaseColor = plant.currentPhase.themedColor()
    val progress = openPhase?.let { phaseProgressOf(it.startDate, plant.currentPhase, genetics) }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(phaseColor.copy(alpha = 0.22f))
                        .border(androidx.compose.foundation.BorderStroke(1.dp, phaseColor), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (thumbnailPath != null) {
                        AsyncImage(
                            model = File(thumbnailPath),
                            contentDescription = plant.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)),
                        )
                    } else {
                        Text(
                            plant.name.take(1).uppercase(),
                            color = plant.currentPhase.onBadgeColor(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                ) {
                    Text(plant.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    genetics?.name?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                PhasePill(plant.currentPhase)
            }

            Column(Modifier.padding(top = 14.dp)) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(progress?.fraction ?: 0f).height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(phaseColor),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    val totalDays = ChronoUnit.DAYS.between(plant.startDate, LocalDate.now())
                    Text(
                        if (progress != null) "J${progress.daysInPhase}/${progress.totalDurationDays}" else "J$totalDays",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val totalDays = ChronoUnit.DAYS.between(plant.startDate, LocalDate.now())
                    Text(
                        heightCm?.let { "H ${it.toInt()}cm" } ?: "H —",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                    )
                    Text(
                        "Jour $totalDays",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}
