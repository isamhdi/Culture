package com.culture.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** Aperçu compact limité à la semaine en cours (lundi → dimanche), pour l'Accueil. */
@Composable
fun WeekCalendar(
    selectedDate: LocalDate?,
    dotsForDate: (LocalDate) -> List<DotSpec>,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val monday = today.with(DayOfWeek.MONDAY)
    val days = (0..6).map { monday.plusDays(it.toLong()) }

    Column(modifier = modifier) {
        Text(
            text = "Semaine du ${monday.dayOfMonth} ${monday.month.getDisplayName(TextStyle.FULL, Locale.FRENCH)}".replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            days.forEach { date ->
                val isSelected = date == selectedDate
                val isToday = date == today
                Box(
                    modifier = Modifier.weight(1f).aspectRatio(0.8f).padding(2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .selectable(selected = isSelected, onClick = { onDateClick(date) })
                            .then(
                                if (isSelected) {
                                    Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                } else if (isToday) {
                                    Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                } else {
                                    Modifier
                                },
                            )
                            .padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.FRENCH).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                isToday -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            dotsForDate(date).take(3).forEach { dot ->
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .then(
                                            if (dot.filled) {
                                                Modifier.background(dot.color, CircleShape)
                                            } else {
                                                Modifier.border(androidx.compose.foundation.BorderStroke(1.dp, dot.color), CircleShape)
                                            },
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
