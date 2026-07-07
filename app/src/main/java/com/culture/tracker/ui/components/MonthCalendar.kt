package com.culture.tracker.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/** Un point sur le calendrier : [filled] = action déjà enregistrée, sinon action prévue (non faite). */
data class DotSpec(val color: Color, val filled: Boolean = true)

@Composable
fun MonthCalendar(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    dotsForDate: (LocalDate) -> List<DotSpec>,
    onDateClick: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Mois précédent")
            }
            Text(
                text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.FRENCH).replaceFirstChar { it.uppercase() }} ${yearMonth.year}",
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Mois suivant")
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("L", "M", "M", "J", "V", "S", "D").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        val firstOfMonth = yearMonth.atDay(1)
        val leadingBlanks = (firstOfMonth.dayOfWeek.value - 1).coerceIn(0, 6)
        val totalDays = yearMonth.lengthOfMonth()
        val totalCells = leadingBlanks + totalDays
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - leadingBlanks + 1
                    Box(
                        modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (dayNumber in 1..totalDays) {
                            val date = yearMonth.atDay(dayNumber)
                            val isSelected = date == selectedDate
                            val isToday = date == LocalDate.now()
                            Column(
                                modifier = Modifier
                                    .selectable(selected = isSelected, onClick = { onDateClick(date) })
                                    .then(
                                        if (isSelected) {
                                            Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                        } else {
                                            Modifier
                                        },
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                        isToday -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    dotsForDate(date).take(4).forEach { dot ->
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .then(
                                                    if (dot.filled) {
                                                        Modifier.background(dot.color, CircleShape)
                                                    } else {
                                                        Modifier.border(BorderStroke(1.dp, dot.color), CircleShape)
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
    }
}
