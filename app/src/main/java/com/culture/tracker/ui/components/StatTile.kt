package com.culture.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatTile(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    containerBrush: Brush? = null,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier,
        onClick = onClick ?: {},
        enabled = onClick != null,
        colors = CardDefaults.cardColors(
            containerColor = if (containerBrush != null) Color.Transparent else containerColor,
            contentColor = contentColor,
            disabledContainerColor = if (containerBrush != null) Color.Transparent else containerColor,
            disabledContentColor = contentColor,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            Modifier.fillMaxWidth()
                .then(if (containerBrush != null) Modifier.background(containerBrush) else Modifier)
                .padding(16.dp),
        ) {
            Icon(icon, contentDescription = null, tint = contentColor)
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}
