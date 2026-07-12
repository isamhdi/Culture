package com.culture.tracker.ui.splash

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.culture.tracker.R
import kotlinx.coroutines.delay

private const val HOLD_DURATION_MS = 350L

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var animate by remember { mutableFloatStateOf(0f) }

    val scale by animateFloatAsState(
        targetValue = animate,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "splashLogoScale",
    )
    val alpha by animateFloatAsState(
        targetValue = animate,
        animationSpec = tween(durationMillis = 300),
        label = "splashLogoAlpha",
    )

    LaunchedEffect(Unit) {
        animate = 1f
        delay(700 + HOLD_DURATION_MS)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.pousse_logo),
            contentDescription = null,
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .alpha(alpha),
        )
    }
}
