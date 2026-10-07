package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Modern tactile rotary knob control for adjusting Audio Frequency EQ bands & Bass.
 * Touch and drag in a circular motion to smoothly rotate and tune dB gain.
 */
@Composable
fun RotaryEqualizerKnob(
    value: Float, // from -10f to 10f
    onValueChange: (Float) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    range: ClosedFloatingPointRange<Float> = -10f..10f
) {
    // Map value (-10..10) to angle (-135 deg to +135 deg)
    val minAngle = -135f
    val maxAngle = 135f
    val sweepAngle = maxAngle - minAngle

    val normalized = ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
    val currentAngleDeg = minAngle + normalized * sweepAngle

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(68.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Sensitivity based on horizontal or vertical drag
                        val delta = (dragAmount.x - dragAmount.y) * 0.15f
                        val newValue = (value + delta).coerceIn(range.start, range.endInclusive)
                        onValueChange(newValue)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = (size.minDimension / 2f) - 6.dp.toPx()

                // Background track arc (dim grey)
                drawArc(
                    color = Color.White.copy(alpha = 0.12f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Active track arc (SpotifyGreen glow)
                val activeSweep = normalized * 270f
                if (activeSweep > 0) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color(0xFF00B377), SpotifyGreen, Color(0xFF33FFAE))
                        ),
                        startAngle = 135f,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner metallic knob body
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF282E38), Color(0xFF14171C))
                    ),
                    radius = radius - 7.dp.toPx(),
                    center = center
                )

                // Knob indicator tick mark
                val rad = Math.toRadians((currentAngleDeg + 90).toDouble())
                val innerR = radius - 15.dp.toPx()
                val outerR = radius - 7.dp.toPx()
                val tickStart = Offset(
                    (center.x + innerR * cos(rad)).toFloat(),
                    (center.y + innerR * sin(rad)).toFloat()
                )
                val tickEnd = Offset(
                    (center.x + outerR * cos(rad)).toFloat(),
                    (center.y + outerR * sin(rad)).toFloat()
                )

                drawLine(
                    color = if (value != 0f) SpotifyGreen else Color.White,
                    start = tickStart,
                    end = tickEnd,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Center value readout
            Text(
                text = if (value > 0) "+${value.toInt()}" else "${value.toInt()}",
                color = if (value != 0f) SpotifyGreen else TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
