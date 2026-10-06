package com.example.ringarea.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1300, easing = FastOutSlowInEasing)
            )
        }
        launch {
            delay(400)
            titleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800, easing = LinearEasing)
            )
        }
        delay(1500)
        onSplashFinished()
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val p = progress.value

                val outerRadius = (size.width / 2f - with(density) { 16.dp.toPx() }) * p.coerceIn(0f, 1f)
                val innerRatio = 0.55f
                val innerRadius = outerRadius * innerRatio

                if (p > 0.3f) {
                    val ringFillAlpha = ((p - 0.3f) / 0.7f).coerceIn(0f, 0.22f)
                    val ringPath = Path().apply {
                        fillType = PathFillType.EvenOdd
                        addOval(Rect(center, outerRadius))
                        addOval(Rect(center, innerRadius))
                    }
                    drawPath(ringPath, color = primaryColor.copy(alpha = ringFillAlpha))
                }

                if (outerRadius > 0f) {
                    drawCircle(
                        color = primaryColor,
                        radius = outerRadius,
                        center = center,
                        style = Stroke(width = with(density) { 2.5.dp.toPx() })
                    )
                }

                if (innerRadius > 0f && p > 0.2f) {
                    drawCircle(
                        color = tertiaryColor,
                        radius = innerRadius,
                        center = center,
                        style = Stroke(width = with(density) { 2.dp.toPx() })
                    )
                }

                if (p > 0.4f) {
                    val centerDotRadius = with(density) { 3.dp.toPx() } * ((p - 0.4f) / 0.6f)
                    drawCircle(
                        color = primaryColor,
                        radius = centerDotRadius,
                        center = center
                    )
                }

                // Radius indicator lines animating outward
                if (p > 0.5f) {
                    val lineFactor = ((p - 0.5f) / 0.5f).coerceIn(0f, 1f)
                    val angleR = -35.0 * PI / 180.0
                    val rLineEnd = Offset(
                        (center.x + outerRadius * lineFactor * cos(angleR)).toFloat(),
                        (center.y + outerRadius * lineFactor * sin(angleR)).toFloat()
                    )
                    drawLine(
                        color = primaryColor.copy(alpha = lineFactor),
                        start = center,
                        end = rLineEnd,
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )

                    val angleIn = 145.0 * PI / 180.0
                    val inLineEnd = Offset(
                        (center.x + innerRadius * lineFactor * cos(angleIn)).toFloat(),
                        (center.y + innerRadius * lineFactor * sin(angleIn)).toFloat()
                    )
                    drawLine(
                        color = tertiaryColor.copy(alpha = lineFactor),
                        start = center,
                        end = inLineEnd,
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "RINGAREA",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Calculate the area between two circles",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = titleAlpha.value)
            )
        }
    }
}
