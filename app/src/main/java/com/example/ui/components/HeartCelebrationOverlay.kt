package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpotifyGreen
import kotlinx.coroutines.delay
import kotlin.random.Random

data class HeartParticle(
    val id: Int,
    val initialX: Float,
    val initialY: Float,
    val targetX: Float,
    val targetY: Float,
    val size: Float,
    val rotation: Float,
    val color: Color
)

@Composable
fun HeartCelebrationOverlay(
    songTitle: String?,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible || songTitle == null) return

    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = LocalHapticFeedback.current

    var mainScale by remember { mutableFloatStateOf(0.2f) }
    var mainAlpha by remember { mutableFloatStateOf(1f) }

    val heartParticles = remember {
        List(14) { id ->
            val angle = Random.nextDouble(0.0, 2 * Math.PI)
            val dist = Random.nextDouble(80.0, 180.0)
            HeartParticle(
                id = id,
                initialX = 0f,
                initialY = 0f,
                targetX = (Math.cos(angle) * dist).toFloat(),
                targetY = (Math.sin(angle) * dist).toFloat(),
                size = Random.nextDouble(16.0, 32.0).toFloat(),
                rotation = Random.nextDouble(-30.0, 30.0).toFloat(),
                color = listOf(
                    Color(0xFFFF1744),
                    Color(0xFFFF4081),
                    Color(0xFFE040FB),
                    Color(0xFFFF5252),
                    Color(0xFFFF6090)
                ).random()
            )
        }
    }

    val particleProgress = remember { Animatable(0f) }

    LaunchedEffect(songTitle, isVisible) {
        // Distinct heartbeat tactile vibration
        com.example.util.TactileFeedbackHelper.performHeartbeatHaptic(context, haptic)
        mainScale = 0.2f
        mainAlpha = 1f

        // Spring punch
        animate(
            initialValue = 0.2f,
            targetValue = 1.35f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        ) { value, _ ->
            mainScale = value
        }

        particleProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )

        // Settle down and fade
        delay(300)
        animate(initialValue = 1f, targetValue = 0f, animationSpec = tween(250)) { v, _ ->
            mainAlpha = v
        }
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // Semi-transparent backdrop glow
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFF1744).copy(alpha = 0.25f * mainAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        // Flying heart particles
        heartParticles.forEach { p ->
            val curX = p.targetX * particleProgress.value
            val curY = p.targetY * particleProgress.value
            val pAlpha = (1f - particleProgress.value) * mainAlpha

            Box(
                modifier = Modifier
                    .offset(x = curX.dp, y = curY.dp)
                    .scale(particleProgress.value)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = p.color.copy(alpha = pAlpha),
                    modifier = Modifier.size(p.size.dp)
                )
            }
        }

        // Center big bouncing glowing heart
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(mainScale)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFF1744), Color(0xFFC2185B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Liked",
                    tint = Color.White.copy(alpha = mainAlpha),
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Added to Favorites ❤️",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = mainAlpha)
            )
            Text(
                text = songTitle,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color(0xFFFF80AB).copy(alpha = mainAlpha)
            )
        }
    }
}
