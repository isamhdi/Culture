package com.culture.tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Anneau de progression circulaire (façon "meter") : la piste est une teinte
 * claire de la même couleur, le remplissage un dégradé à deux tons de la couleur d'accent.
 */
@Composable
fun CircularProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    strokeWidth: Dp = 10.dp,
    trackColor: Color = Color(0xFFE1E0D9),
    gradientColors: List<Color> = listOf(Color(0xFF2A78D6), Color(0xFF1BAF7A)),
    content: @Composable () -> Unit = {},
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val arcSize = Size(size.toPx() - strokeWidth.toPx(), size.toPx() - strokeWidth.toPx())
            val topLeft = androidx.compose.ui.geometry.Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            if (clamped > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(gradientColors),
                    startAngle = -90f,
                    sweepAngle = 360f * clamped,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
            }
        }
        content()
    }
}
