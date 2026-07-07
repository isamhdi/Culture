package com.culture.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.culture.tracker.ui.theme.HandoffColors

/** En-tête standard des bottom sheets : titre + croix rouge de fermeture, reprise du design_handoff. */
@Composable
fun SheetHeader(title: String, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(40.dp)
                .background(HandoffColors.BackButtonBg, RoundedCornerShape(12.dp)),
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Fermer", tint = HandoffColors.Danger)
        }
    }
}
