package com.example.ringarea.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ringarea.data.model.MeasurementUnit
import com.example.ringarea.data.model.RingResult
import com.example.ringarea.domain.format.ResultFormatter
import com.example.ringarea.domain.unit.UnitConverter
import java.math.BigDecimal
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RingDiagram(
    result: RingResult?,
    unit: MeasurementUnit,
    decimals: Int,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    val outlineColor = MaterialTheme.colorScheme.outline
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val ringFillColor = if (isDark) Color(0x332DD4BF) else Color(0x240F766E)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (result == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Concentric Ring Diagram",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceVariant
                )
            }
        } else {
            val outerRMm = result.outerRadiusMm
            val innerRMm = result.innerRadiusMm
            val ringWidthMm = result.ringWidthMm

            val outerR = UnitConverter.fromMillimeters(outerRMm, unit)
            val innerR = UnitConverter.fromMillimeters(innerRMm, unit)
            val ringWidth = UnitConverter.fromMillimeters(ringWidthMm, unit)

            val outerRadiusStr = ResultFormatter.formatDimension(outerR, unit, decimals)
            val innerRadiusStr = ResultFormatter.formatDimension(innerR, unit, decimals)
            val ringWidthStr = ResultFormatter.formatDimension(ringWidth, unit, decimals)

            val density = LocalDensity.current

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = (minOf(size.width, size.height) / 2f) - with(density) { 36.dp.toPx() }

                // Ratio calculation: clamped between 0.18f and 0.88f for visual clarity and technical legibility
                val rawRatio = innerRMm.toDouble() / outerRMm.toDouble()
                val visualRatio = rawRatio.coerceIn(0.18, 0.88).toFloat()

                val outerRadiusPx = maxRadius
                val innerRadiusPx = maxRadius * visualRatio

                // 1. Shaded Annular Ring Region
                val ringPath = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addOval(Rect(center, outerRadiusPx))
                    addOval(Rect(center, innerRadiusPx))
                }
                drawPath(ringPath, color = ringFillColor)

                // 2. Outer Circle Outline
                drawCircle(
                    color = primaryColor,
                    radius = outerRadiusPx,
                    center = center,
                    style = Stroke(width = with(density) { 2.dp.toPx() })
                )

                // 3. Inner Circle Outline
                drawCircle(
                    color = tertiaryColor,
                    radius = innerRadiusPx,
                    center = center,
                    style = Stroke(width = with(density) { 2.dp.toPx() })
                )

                // 4. Center Marker & Label
                val centerDotRadius = with(density) { 3.5.dp.toPx() }
                drawCircle(
                    color = primaryColor,
                    radius = centerDotRadius,
                    center = center
                )

                // Draw center crosshairs (subtle architectural marker)
                val crossSize = with(density) { 8.dp.toPx() }
                drawLine(
                    color = onSurfaceVariant.copy(alpha = 0.6f),
                    start = Offset(center.x - crossSize, center.y),
                    end = Offset(center.x + crossSize, center.y),
                    strokeWidth = 1f
                )
                drawLine(
                    color = onSurfaceVariant.copy(alpha = 0.6f),
                    start = Offset(center.x, center.y - crossSize),
                    end = Offset(center.x, center.y + crossSize),
                    strokeWidth = 1f
                )

                // Label: "Center" if space permits
                if (innerRadiusPx > with(density) { 30.dp.toPx() }) {
                    val centerText = "Center"
                    val centerMeasured = textMeasurer.measure(
                        text = centerText,
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = onSurfaceVariant,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    drawText(
                        textLayoutResult = centerMeasured,
                        topLeft = Offset(
                            center.x - centerMeasured.size.width / 2f,
                            center.y + with(density) { 6.dp.toPx() }
                        )
                    )
                }

                // 5. Outer Radius Dimension Line (Angle: ~ -35 degrees)
                val outerAngleRad = -35.0 * PI / 180.0
                val outerEnd = Offset(
                    (center.x + outerRadiusPx * cos(outerAngleRad)).toFloat(),
                    (center.y + outerRadiusPx * sin(outerAngleRad)).toFloat()
                )
                drawDimensionLine(
                    start = center,
                    end = outerEnd,
                    color = primaryColor,
                    drawScope = this
                )
                // Text for Outer Radius
                val outerLabel = "R = $outerRadiusStr"
                val outerMeasured = textMeasurer.measure(
                    text = outerLabel,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryColor,
                        fontFamily = FontFamily.Monospace
                    )
                )
                val outerTextPos = Offset(
                    (center.x + (outerRadiusPx * 0.65f) * cos(outerAngleRad)).toFloat() + 6f,
                    (center.y + (outerRadiusPx * 0.65f) * sin(outerAngleRad)).toFloat() - outerMeasured.size.height - 2f
                )
                drawText(textLayoutResult = outerMeasured, topLeft = outerTextPos)

                // 6. Inner Radius Dimension Line (Angle: ~ 145 degrees)
                val innerAngleRad = 145.0 * PI / 180.0
                val innerEnd = Offset(
                    (center.x + innerRadiusPx * cos(innerAngleRad)).toFloat(),
                    (center.y + innerRadiusPx * sin(innerAngleRad)).toFloat()
                )
                drawDimensionLine(
                    start = center,
                    end = innerEnd,
                    color = tertiaryColor,
                    drawScope = this
                )
                // Text for Inner Radius
                val innerLabel = "r = $innerRadiusStr"
                val innerMeasured = textMeasurer.measure(
                    text = innerLabel,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tertiaryColor,
                        fontFamily = FontFamily.Monospace
                    )
                )
                val innerTextPos = Offset(
                    (center.x + (innerRadiusPx * 0.55f) * cos(innerAngleRad)).toFloat() - innerMeasured.size.width - 6f,
                    (center.y + (innerRadiusPx * 0.55f) * sin(innerAngleRad)).toFloat() - innerMeasured.size.height / 2f
                )
                drawText(textLayoutResult = innerMeasured, topLeft = innerTextPos)

                // 7. Ring Width Radial Dimension Indicator (Angle: ~ 85 degrees downward)
                val widthAngleRad = 85.0 * PI / 180.0
                val widthStart = Offset(
                    (center.x + innerRadiusPx * cos(widthAngleRad)).toFloat(),
                    (center.y + innerRadiusPx * sin(widthAngleRad)).toFloat()
                )
                val widthEnd = Offset(
                    (center.x + outerRadiusPx * cos(widthAngleRad)).toFloat(),
                    (center.y + outerRadiusPx * sin(widthAngleRad)).toFloat()
                )
                drawWidthDimensionLine(
                    start = widthStart,
                    end = widthEnd,
                    color = secondaryColor,
                    drawScope = this
                )
                val widthLabel = "Ring Width: $ringWidthStr"
                val widthMeasured = textMeasurer.measure(
                    text = widthLabel,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryColor,
                        fontFamily = FontFamily.Monospace
                    )
                )
                val widthMidX = (widthStart.x + widthEnd.x) / 2f
                val widthMidY = (widthStart.y + widthEnd.y) / 2f
                drawText(
                    textLayoutResult = widthMeasured,
                    topLeft = Offset(widthMidX + 8f, widthMidY - widthMeasured.size.height / 2f)
                )
            }
        }
    }
}

private fun drawDimensionLine(
    start: Offset,
    end: Offset,
    color: Color,
    drawScope: DrawScope
) {
    with(drawScope) {
        drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = 1.8f,
            cap = StrokeCap.Round
        )

        // Arrow tick at end
        val angle = kotlin.math.atan2(end.y - start.y, end.x - start.x)
        val arrowLength = 12f
        val arrowAngle = PI / 6.0 // 30 degrees

        val p1 = Offset(
            (end.x - arrowLength * cos(angle - arrowAngle)).toFloat(),
            (end.y - arrowLength * sin(angle - arrowAngle)).toFloat()
        )
        val p2 = Offset(
            (end.x - arrowLength * cos(angle + arrowAngle)).toFloat(),
            (end.y - arrowLength * sin(angle + arrowAngle)).toFloat()
        )

        drawLine(color = color, start = end, end = p1, strokeWidth = 1.8f, cap = StrokeCap.Round)
        drawLine(color = color, start = end, end = p2, strokeWidth = 1.8f, cap = StrokeCap.Round)
    }
}

private fun drawWidthDimensionLine(
    start: Offset,
    end: Offset,
    color: Color,
    drawScope: DrawScope
) {
    with(drawScope) {
        // Dimension line between inner circle and outer circle
        drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = 1.8f,
            cap = StrokeCap.Round
        )

        val angle = kotlin.math.atan2(end.y - start.y, end.x - start.x)
        val arrowLength = 10f
        val arrowAngle = PI / 6.0

        // Arrow pointing inwards at start
        val s1 = Offset(
            (start.x + arrowLength * cos(angle - arrowAngle)).toFloat(),
            (start.y + arrowLength * sin(angle - arrowAngle)).toFloat()
        )
        val s2 = Offset(
            (start.x + arrowLength * cos(angle + arrowAngle)).toFloat(),
            (start.y + arrowLength * sin(angle + arrowAngle)).toFloat()
        )
        drawLine(color = color, start = start, end = s1, strokeWidth = 1.8f, cap = StrokeCap.Round)
        drawLine(color = color, start = start, end = s2, strokeWidth = 1.8f, cap = StrokeCap.Round)

        // Arrow pointing outwards at end
        val e1 = Offset(
            (end.x - arrowLength * cos(angle - arrowAngle)).toFloat(),
            (end.y - arrowLength * sin(angle - arrowAngle)).toFloat()
        )
        val e2 = Offset(
            (end.x - arrowLength * cos(angle + arrowAngle)).toFloat(),
            (end.y - arrowLength * sin(angle + arrowAngle)).toFloat()
        )
        drawLine(color = color, start = end, end = e1, strokeWidth = 1.8f, cap = StrokeCap.Round)
        drawLine(color = color, start = end, end = e2, strokeWidth = 1.8f, cap = StrokeCap.Round)
    }
}
