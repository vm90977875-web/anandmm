package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.AppLanguage
import com.example.util.AppStrings
import com.example.util.TactileFeedbackHelper
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class BoosterPreset(
    val name: String,
    val icon: String,
    val desc: String,
    val bands: List<Float>,
    val bass: Float,
    val surround: Float,
    val clarity: Float,
    val accentColor: Color
)

@Composable
fun EqualizerBoosterScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val isEnabled by viewModel.isEqualizerEnabled.collectAsState()
    val bassBoost by viewModel.bassBoostLevel.collectAsState()
    val virtualizer by viewModel.virtualizerLevel.collectAsState()
    val loudnessBoost by viewModel.loudnessBoostLevel.collectAsState()
    val vocalClarity by viewModel.vocalClarityLevel.collectAsState()
    val currentPreset by viewModel.equalizerPreset.collectAsState()
    val bands by viewModel.equalizerBands.collectAsState()
    val reverbPreset by viewModel.reverbPreset.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val lang by viewModel.currentLanguage.collectAsState()

    BackHandler {
        viewModel.handleBackPress()
    }

    val presets = remember {
        listOf(
            BoosterPreset(
                name = "Bass Boost",
                icon = "🚀",
                desc = "Deep Heavy Bass & Subwoofer Impact",
                bands = listOf(8f, 10f, 4f, 1f, 3f),
                bass = 95f,
                surround = 60f,
                clarity = 55f,
                accentColor = Color(0xFF1DB954)
            ),
            BoosterPreset(
                name = "Vocal Booster",
                icon = "🎤",
                desc = "Crystal Clear Singing & Bollywood Vocals",
                bands = listOf(1f, 3f, 8f, 7f, 4f),
                bass = 45f,
                surround = 50f,
                clarity = 95f,
                accentColor = Color(0xFFFF5252)
            ),
            BoosterPreset(
                name = "Electronic",
                icon = "⚡",
                desc = "Punchy Synth & EDM Club Dynamics",
                bands = listOf(9f, 6f, 0f, 5f, 8f),
                bass = 85f,
                surround = 80f,
                clarity = 75f,
                accentColor = Color(0xFF00E5FF)
            ),
            BoosterPreset(
                name = "Rock",
                icon = "🎸",
                desc = "Aggressive Electric Guitars & Drum Punch",
                bands = listOf(6f, 4f, -1f, 5f, 7f),
                bass = 70f,
                surround = 65f,
                clarity = 70f,
                accentColor = Color(0xFFFF9100)
            ),
            BoosterPreset(
                name = "Pop",
                icon = "🎧",
                desc = "Smooth Modern Radio & Chart Balance",
                bands = listOf(4f, 5f, 3f, 5f, 6f),
                bass = 65f,
                surround = 70f,
                clarity = 80f,
                accentColor = Color(0xFFE040FB)
            ),
            BoosterPreset(
                name = "Acoustic",
                icon = "🌊",
                desc = "Natural Strings, Piano & Unplugged",
                bands = listOf(3f, 2f, 4f, 5f, 5f),
                bass = 40f,
                surround = 75f,
                clarity = 85f,
                accentColor = Color(0xFF76FF03)
            ),
            BoosterPreset(
                name = "Hip-Hop",
                icon = "🎚️",
                desc = "Heavy 808 Sub-Bass & Trap Beats",
                bands = listOf(10f, 8f, 2f, 3f, 5f),
                bass = 98f,
                surround = 55f,
                clarity = 60f,
                accentColor = Color(0xFFFFD600)
            ),
            BoosterPreset(
                name = "Flat",
                icon = "🎛️",
                desc = "Pure Unaltered Studio Reference",
                bands = listOf(0f, 0f, 0f, 0f, 0f),
                bass = 0f,
                surround = 0f,
                clarity = 0f,
                accentColor = Color(0xFFB0BEC5)
            )
        )
    }

    val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
    val bandDescriptions = listOf("Sub-Bass", "Bass", "Mid", "High Mid", "Treble")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("equalizer_booster_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Studio Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (lang == AppLanguage.HINDI) "🎛️ इक्वलाइज़र & बूस्टर" else "🎛️ Studio Equalizer",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "320 kbps High-Definition DSP Engine",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Large Master Toggle Switch with Soft Glow
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEnabled) SpotifyGreen.copy(alpha = 0.2f) else DarkCard
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isEnabled) SpotifyGreen else Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier
                            .clickable {
                                TactileFeedbackHelper.performTick(context, haptic)
                                viewModel.setEqualizerEnabled(!isEnabled)
                            }
                            .testTag("eq_master_power_switch")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isEnabled) SpotifyGreen else TextMuted)
                            )
                            Text(
                                text = if (isEnabled) "ACTIVE" else "OFF",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEnabled) SpotifyGreen else TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Live Dynamic Frequency Wave Visualizer
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF141414)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GraphicEq,
                                contentDescription = null,
                                tint = SpotifyGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Real-Time Spectrum Analyzer",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = if (isPlaying && isEnabled) "● LIVE PROCESSING" else "● STANDBY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaying && isEnabled) SpotifyGreen else TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AnimatedFrequencyCanvas(
                        isPlaying = isPlaying && isEnabled,
                        bassBoost = bassBoost,
                        bands = bands,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                    )
                }
            }
        }

        // Big Rotary Booster Knobs Section (बटन्स वो बड़े-बड़े हों)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "🔊 Sound Booster Knobs (बड़े बूस्टर कंट्रोल्स)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Touch and slide around or tap to dial boost level softly",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Big Bass Boost Knob
                    BigRotaryKnobCard(
                        title = "BASS BOOST",
                        subtitle = "Subwoofer Lows",
                        value = bassBoost,
                        unit = "%",
                        min = 0f,
                        max = 100f,
                        activeColor = Color(0xFF1DB954),
                        icon = Icons.Filled.VolumeUp,
                        onValueChange = {
                            TactileFeedbackHelper.performTick(context, haptic)
                            viewModel.setBassBoost(it)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Big 3D Surround / Virtualizer Knob
                    BigRotaryKnobCard(
                        title = "3D SURROUND",
                        subtitle = "Spatial Room",
                        value = virtualizer,
                        unit = "%",
                        min = 0f,
                        max = 100f,
                        activeColor = Color(0xFF00E5FF),
                        icon = Icons.Filled.SurroundSound,
                        onValueChange = {
                            TactileFeedbackHelper.performTick(context, haptic)
                            viewModel.setVirtualizer(it)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Big Loudness / Volume Overdrive Knob
                    BigRotaryKnobCard(
                        title = "LOUDNESS",
                        subtitle = "+12 dB Overdrive",
                        value = loudnessBoost,
                        unit = "%",
                        min = 0f,
                        max = 100f,
                        activeColor = Color(0xFFFF9100),
                        icon = Icons.Filled.Speed,
                        onValueChange = {
                            TactileFeedbackHelper.performTick(context, haptic)
                            viewModel.setLoudnessBoost(it)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Big Vocal Clarity Knob
                    BigRotaryKnobCard(
                        title = "VOCAL CLARITY",
                        subtitle = "Crisp Treble",
                        value = vocalClarity,
                        unit = "%",
                        min = 0f,
                        max = 100f,
                        activeColor = Color(0xFFE040FB),
                        icon = Icons.Filled.Mic,
                        onValueChange = {
                            TactileFeedbackHelper.performTick(context, haptic)
                            viewModel.setVocalClarity(it)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Multi-Band Graphic Equalizer (5 Studio Bands with Big Sliders)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF161616)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🎚️ 5-Band Graphic Equalizer",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Fine-tune frequency gain (+12 dB to -12 dB)",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }

                        // Reset button
                        TextButton(
                            onClick = {
                                TactileFeedbackHelper.performTick(context, haptic)
                                viewModel.setEqualizerPreset("Flat")
                            }
                        ) {
                            Text("Reset", color = SpotifyGreen, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sliders Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bands.forEachIndexed { index, gain ->
                            val freqLabel = bandLabels.getOrElse(index) { "${index + 1}" }
                            val descLabel = bandDescriptions.getOrElse(index) { "" }

                            VerticalBandSlider(
                                gain = gain,
                                freqLabel = freqLabel,
                                descLabel = descLabel,
                                isEnabled = isEnabled,
                                onGainChange = { newGain ->
                                    TactileFeedbackHelper.performTick(context, haptic)
                                    viewModel.updateEqualizerBand(index, newGain)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Big Preset Cards Section (बटन्स वो बड़े-बड़े हों)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "✨ Instant Studio Presets (तुरंत सेट करने वाले प्रीसेट्स)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Tap any preset for soft animated transition across all bands",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(presets) { preset ->
                        val isSelected = currentPreset == preset.name
                        BigPresetCard(
                            preset = preset,
                            isSelected = isSelected,
                            onClick = {
                                TactileFeedbackHelper.performTick(context, haptic)
                                viewModel.setEqualizerPreset(preset.name)
                                viewModel.setBassBoost(preset.bass)
                                viewModel.setVirtualizer(preset.surround)
                                viewModel.setVocalClarity(preset.clarity)
                            }
                        )
                    }
                }
            }
        }

        // Acoustic Environment / Reverb
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "🏛️ Acoustic Reverb Environment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                val environments = listOf("Studio", "Concert Hall", "Stadium", "Acoustic Room", "Club")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(environments) { env ->
                        val isSelected = reverbPreset == env
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                TactileFeedbackHelper.performTick(context, haptic)
                                viewModel.setReverbPreset(env)
                            },
                            label = { Text(env, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SpotifyGreen,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkCard,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color.White.copy(alpha = 0.12f),
                                selectedBorderColor = SpotifyGreen
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Big Rotary Dial Card with Smooth Tactile Drag Gesture
 */
@Composable
fun BigRotaryKnobCard(
    title: String,
    subtitle: String,
    value: Float,
    unit: String,
    min: Float,
    max: Float,
    activeColor: Color,
    icon: ImageVector,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF181818)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDragging) activeColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "${value.toInt()}$unit",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = activeColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Big Rotary Dial Canvas
            Box(
                modifier = Modifier
                    .size(105.dp)
                    .pointerInput(min, max) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false }
                        ) { change, dragAmount ->
                            change.consume()
                            // Vertical or horizontal drag adjustment
                            val delta = (-dragAmount.y + dragAmount.x) * 0.4f
                            val newValue = (value + delta).coerceIn(min, max)
                            onValueChange(newValue)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                val animatedVal by animateFloatAsState(
                    targetValue = value,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "knob_val"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f - 10.dp.toPx()
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Background track arc (135° to 405° = 270° sweep)
                    val startAngle = 135f
                    val totalSweep = 270f

                    drawArc(
                        color = Color.White.copy(alpha = 0.1f),
                        startAngle = startAngle,
                        sweepAngle = totalSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Active sweep arc
                    val normalizedProgress = ((animatedVal - min) / (max - min)).coerceIn(0f, 1f)
                    val activeSweep = totalSweep * normalizedProgress

                    if (activeSweep > 0f) {
                        drawArc(
                            color = activeColor,
                            startAngle = startAngle,
                            sweepAngle = activeSweep,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Dial Indicator Thumb
                    val currentAngle = (startAngle + activeSweep) * (PI.toFloat() / 180f)
                    val thumbX = center.x + radius * cos(currentAngle)
                    val thumbY = center.y + radius * sin(currentAngle)

                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = Offset(thumbX, thumbY)
                    )
                }

                // Center readout button (tap to step boost)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF2A2A2A), Color(0xFF141414))
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        .clickable {
                            val next = if (value >= max - 5f) min else (value + 20f).coerceAtMost(max)
                            onValueChange(next)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${value.toInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1
            )
        }
    }
}

/**
 * Vertical Graphic Equalizer Slider with dB scale and smooth thumb
 */
@Composable
fun VerticalBandSlider(
    gain: Float,
    freqLabel: String,
    descLabel: String,
    isEnabled: Boolean,
    onGainChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedGain by animateFloatAsState(
        targetValue = gain,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "band_gain"
    )

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gain display in dB
        Text(
            text = "${if (gain > 0) "+" else ""}${gain.toInt()} dB",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (gain != 0f && isEnabled) SpotifyGreen else TextMuted
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Slider track
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(130.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF202020))
                .pointerInput(isEnabled) {
                    if (isEnabled) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            // -12dB to +12dB (total 24dB range)
                            val delta = -dragAmount.y * 0.25f
                            val newGain = (gain + delta).coerceIn(-12f, 12f)
                            onGainChange(newGain)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Center reference zero line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.White.copy(alpha = 0.2f))
            )

            // Slider progress bar
            val progress = ((animatedGain + 12f) / 24f).coerceIn(0f, 1f)

            // Thumb
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(progress)
                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(SpotifyGreen.copy(alpha = 0.7f), SpotifyGreen.copy(alpha = 0.2f))
                            )
                        )
                )

                // Draggable knob indicator
                Box(
                    modifier = Modifier
                        .align(
                            if (progress >= 0.95f) Alignment.TopCenter
                            else if (progress <= 0.05f) Alignment.BottomCenter
                            else Alignment.Center
                        )
                        .size(24.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(if (isEnabled) SpotifyGreen else TextMuted)
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Frequency Label
        Text(
            text = freqLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = descLabel,
            fontSize = 9.sp,
            color = TextMuted,
            maxLines = 1
        )
    }
}

/**
 * Big Preset Selection Card (बटन्स वो बड़े-बड़े हों)
 */
@Composable
fun BigPresetCard(
    preset: BoosterPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(155.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF222222) else DarkCard
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) preset.accentColor else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.icon,
                    fontSize = 24.sp
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(preset.accentColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = preset.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) preset.accentColor else TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = preset.desc,
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 14.sp,
                maxLines = 2
            )
        }
    }
}

/**
 * Animated Frequency Canvas spectrum analyzer
 */
@Composable
fun AnimatedFrequencyCanvas(
    isPlaying: Boolean,
    bassBoost: Float,
    bands: List<Float>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "freq_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        val barCount = 28
        val barWidth = (width / barCount) * 0.65f
        val gap = (width / barCount) * 0.35f

        for (i in 0 until barCount) {
            val progress = i.toFloat() / barCount
            val bandIndex = (progress * (bands.size - 1)).toInt().coerceIn(0, bands.size - 1)
            val bandGain = (bands.getOrElse(bandIndex) { 0f } + 12f) / 24f

            val wave = if (isPlaying) {
                val sinVal = sin(phase + i * 0.35f)
                val cosVal = cos(phase * 0.7f + i * 0.2f)
                ((sinVal + cosVal + 2f) / 4f) * (0.4f + 0.6f * bandGain) * (0.8f + (bassBoost / 250f))
            } else {
                0.12f + (bandGain * 0.2f)
            }

            val barHeight = (height * 0.85f * wave).coerceAtLeast(6.dp.toPx())
            val x = i * (barWidth + gap)
            val y = midY - (barHeight / 2f)

            val barColor = when {
                i < barCount * 0.3f -> Color(0xFF1DB954)
                i < barCount * 0.65f -> Color(0xFF00E5FF)
                else -> Color(0xFFE040FB)
            }

            drawRoundRect(
                color = barColor.copy(alpha = if (isPlaying) 0.9f else 0.4f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}
