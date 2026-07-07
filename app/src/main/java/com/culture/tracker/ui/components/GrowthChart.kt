package com.culture.tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.culture.tracker.data.local.entity.HeightMeasurement
import com.culture.tracker.ui.theme.HandoffColors

/**
 * Courbe de croissance (hauteur en cm dans le temps), reprise à l'identique du
 * design_handoff_suivi_plante : aire remplie à faible opacité, 3 lignes de grille
 * pointillées, ligne 2.5dp, marqueur de fin plein, libellés J0/J{totalDay}.
 */
@Composable
fun GrowthChart(history: List<HeightMeasurement>, stageColor: Color, totalDay: Long, modifier: Modifier = Modifier) {
    if (history.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
            Text(
                "Pas encore de relevé de hauteur.",
                style = MaterialTheme.typography.bodyMedium,
                color = HandoffColors.TextTertiary,
            )
        }
        return
    }

    val gridColor = HandoffColors.BorderCard
    val maxHeight = (history.maxOf { it.heightCm }).coerceAtLeast(1.0)

    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(top = 8.dp, bottom = 18.dp),
        ) {
            val w = size.width
            val h = size.height
            val n = history.size

            fun xFor(i: Int): Float = if (n == 1) w / 2f else (i.toFloat() / (n - 1)) * w
            fun yFor(value: Double): Float = (1f - (value / maxHeight).toFloat()) * h

            // Grilles hairline pointillées (25/50/75%)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 4.dp.toPx()), 0f)
            listOf(0.25f, 0.5f, 0.75f).forEach { f ->
                drawLine(
                    color = gridColor,
                    start = Offset(0f, f * h),
                    end = Offset(w, f * h),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect,
                )
            }

            val path = androidx.compose.ui.graphics.Path()
            history.forEachIndexed { i, point ->
                val x = xFor(i)
                val y = yFor(point.heightCm)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            // Aire remplie sous la courbe
            val areaPath = androidx.compose.ui.graphics.Path().apply {
                addPath(path)
                lineTo(xFor(n - 1), h)
                lineTo(xFor(0), h)
                close()
            }
            drawPath(areaPath, color = stageColor.copy(alpha = 0.18f))

            drawPath(
                path = path,
                color = stageColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )

            val lastX = xFor(n - 1)
            val lastY = yFor(history.last().heightCm)
            drawCircle(stageColor, radius = 4.5.dp.toPx(), center = Offset(lastX, lastY))
        }

        Text(
            "J0",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = HandoffColors.TextTertiary,
            modifier = Modifier.align(Alignment.BottomStart),
        )
        Text(
            "J$totalDay",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = HandoffColors.TextTertiary,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}
