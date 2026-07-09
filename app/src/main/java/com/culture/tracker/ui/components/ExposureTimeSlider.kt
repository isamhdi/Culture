package com.culture.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** Réglage des heures de lumière/jour façon "Exposure Time" : lune (0h) à soleil (24h), badge de valeur. */
@Composable
fun ExposureTimeSlider(hours: Float, onHoursChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Durée d'exposition", style = MaterialTheme.typography.labelLarge)
            Text(
                "${hours.toInt()} hs",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.NightsStay, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Slider(
                value = hours,
                onValueChange = onHoursChange,
                valueRange = 0f..24f,
                steps = 23,
                colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Filled.LightMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
