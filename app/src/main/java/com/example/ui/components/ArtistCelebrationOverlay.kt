package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArtistCelebrationOverlay(
    artistName: String?,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible || artistName == null) return

    val haptic = LocalHapticFeedback.current

    var targetScale by remember { mutableFloatStateOf(0.1f) }
    var startParticle by remember { mutableStateOf(false) }

    LaunchedEffect(artistName, isVisible) {
        targetScale = 0.1f
        startParticle = false
        delay(40)
        targetScale = 1.0f
        startParticle = true
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            // ignore
        }
        delay(2300)
        onDismiss()
    }

    val bounceScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bounce_scale"
    )

    // Pulsing glow infinite animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // Floating particle expansion (0f to 1f)
    val particleProgress by animateFloatAsState(
        targetValue = if (startParticle) 1f else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "particle_progress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // Exploding orbital emojis around the center
        val particles = listOf("💖", "✨", "🔥", "⭐", "🎵", "🎧", "⚡", "🌟")
        particles.forEachIndexed { index, emoji ->
            val angle = (index * (360.0 / particles.size) * (Math.PI / 180.0)).toFloat()
            val distance = 130.dp * particleProgress
            val offsetX = distance * cos(angle.toDouble()).toFloat()
            val offsetY = distance * sin(angle.toDouble()).toFloat()
            val alpha = (1f - particleProgress * 0.7f).coerceIn(0f, 1f)

            Text(
                text = emoji,
                fontSize = 32.sp,
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .scale(0.8f + particleProgress * 0.4f)
                    .rotate(index * 45f * particleProgress)
            )
        }

        // Central Huge Animated Emoji with glow ring
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(170.dp)
            ) {
                // Radiant glow ring
                Box(
                    modifier = Modifier
                        .size(150.dp * pulseGlow)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFF2A68).copy(alpha = 0.45f),
                                    SpotifyGreen.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Giant Emoji Bubble
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1E1E1E),
                    border = androidx.compose.foundation.BorderStroke(3.dp, SpotifyGreen),
                    modifier = Modifier
                        .size(120.dp)
                        .scale(bounceScale)
                        .shadow(24.dp, CircleShape, spotColor = Color(0xFFFF2A68))
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "❤️",
                            fontSize = 62.sp,
                            modifier = Modifier.scale(bounceScale)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Celebration Label Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF181818),
                border = androidx.compose.foundation.BorderStroke(1.dp, SpotifyGreen.copy(alpha = 0.5f)),
                modifier = Modifier
                    .scale(bounceScale.coerceAtMost(1.05f))
                    .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = SpotifyGreen)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Following",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "💖",
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Added to your Favorite Artists • You'll hear new tracks first!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
