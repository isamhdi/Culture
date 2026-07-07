package com.culture.tracker.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.culture.tracker.data.local.entity.Genetics
import com.culture.tracker.data.local.entity.PhaseHistory
import com.culture.tracker.data.local.entity.Plant
import com.culture.tracker.domain.phaseProgressOf
import com.culture.tracker.ui.theme.HandoffColors
import com.culture.tracker.ui.theme.themedColor
import java.io.File

/** Variante condensée de [PlantCard] pour une grille 2 colonnes (écran d'accueil). */
@Composable
fun PlantCardCompact(
    plant: Plant,
    thumbnailPath: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    genetics: Genetics? = null,
    openPhase: PhaseHistory? = null,
) {
    val phaseColor = plant.currentPhase.themedColor()
    val progress = openPhase?.let { phaseProgressOf(it.startDate, plant.currentPhase, genetics) }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, HandoffColors.BorderCard),
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(phaseColor.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (thumbnailPath != null) {
                        AsyncImage(
                            model = File(thumbnailPath),
                            contentDescription = plant.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)),
                        )
                    } else {
                        Text(
                            plant.name.take(1).uppercase(),
                            color = phaseColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                        )
                    }
                }
                Text(
                    plant.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(HandoffColors.ProgressTrack),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(progress?.fraction ?: 0f).height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(phaseColor),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PhasePill(plant.currentPhase)
                Text(
                    progress?.let { "J${it.daysInPhase}/${it.totalDurationDays}" } ?: "—",
                    style = MaterialTheme.typography.labelSmall,
                    color = HandoffColors.TextSecondary,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}
