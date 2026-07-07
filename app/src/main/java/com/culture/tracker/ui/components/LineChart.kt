package com.culture.tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

data class ChartPoint(val label: String, val value: Float)

/**
 * Graphique en ligne minimaliste à une seule série (une seule teinte, un seul axe).
 * Suit les specs du skill dataviz : trait 2dp, marqueur de fin ≥8dp avec anneau de
 * surface, grilles hairline récessives, étiquette directe sur le dernier point,
 * tooltip au tap le plus proche.
 */
@Composable
fun SimpleLineChart(
    points: List<ChartPoint>,
    seriesColor: Color,
    valueFormatter: (Float) -> String = { "%.1f".format(it) },
    modifier: Modifier = Modifier,
) {
    if (points.size < 2) {
        Box(modifier = modifier.fillMaxWidth().height(140.dp)) {
            Text(
                "Pas encore assez de données pour un graphique.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
        return
    }

    var selectedIndex by remember(points) { mutableStateOf<Int?>(null) }
    val surfaceColor = MaterialTheme.colorScheme.surface
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    val minValue = points.minOf { it.value }
    val maxValue = points.maxOf { it.value }
    val range = (maxValue - minValue).takeIf { it > 0f } ?: 1f

    Box(modifier = modifier.fillMaxWidth().height(160.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(top = 8.dp, bottom = 24.dp, start = 4.dp, end = 4.dp)
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                        val idx = (offset.x / stepX).toInt().coerceIn(0, points.size - 1)
                        selectedIndex = idx
                    }
                },
        ) {
            val stepX = size.width / (points.size - 1).coerceAtLeast(1)

            fun yFor(value: Float): Float {
                val t = (value - minValue) / range
                return size.height - (t * size.height)
            }

            // Grilles hairline récessives (min / max)
            drawLine(gridColor, Offset(0f, yFor(minValue)), Offset(size.width, yFor(minValue)), strokeWidth = 1.dp.toPx())
            drawLine(gridColor, Offset(0f, yFor(maxValue)), Offset(size.width, yFor(maxValue)), strokeWidth = 1.dp.toPx())

            val path = androidx.compose.ui.graphics.Path()
            points.forEachIndexed { index, point ->
                val x = index * stepX
                val y = yFor(point.value)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = seriesColor,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )

            // Marqueur de fin avec anneau de surface
            val lastX = (points.size - 1) * stepX
            val lastY = yFor(points.last().value)
            drawCircle(surfaceColor, radius = 6.dp.toPx(), center = Offset(lastX, lastY))
            drawCircle(seriesColor, radius = 4.dp.toPx(), center = Offset(lastX, lastY))

            selectedIndex?.let { idx ->
                val x = idx * stepX
                val y = yFor(points[idx].value)
                drawLine(mutedColor.copy(alpha = 0.4f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
                drawCircle(surfaceColor, radius = 6.dp.toPx(), center = Offset(x, y))
                drawCircle(seriesColor, radius = 4.dp.toPx(), center = Offset(x, y))
            }
        }

        // Étiquette directe : valeur du dernier point (texte en encre, jamais la couleur série)
        Text(
            text = valueFormatter(points.last().value),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp).align(androidx.compose.ui.Alignment.TopEnd),
        )
        Text(
            text = points.first().label,
            style = MaterialTheme.typography.labelSmall,
            color = mutedColor,
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomStart),
        )
        Text(
            text = points.last().label,
            style = MaterialTheme.typography.labelSmall,
            color = mutedColor,
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomEnd),
        )

        selectedIndex?.let { idx ->
            val point = points[idx]
            Surface(
                color = MaterialTheme.colorScheme.inverseSurface,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.align(androidx.compose.ui.Alignment.TopCenter).padding(top = 4.dp),
            ) {
                Text(
                    "${point.label} · ${valueFormatter(point.value)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}
