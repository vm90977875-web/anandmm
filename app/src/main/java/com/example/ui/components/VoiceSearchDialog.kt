package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.DarkCard
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.util.TactileFeedbackHelper
import java.util.Locale

@Composable
fun VoiceSearchDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSpeechResult: (String) -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var soundLevel by remember { mutableFloatStateOf(0f) }
    var statusMessage by remember { mutableStateOf("Listening... बोलिए (गाना या कलाकार)...") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (!isGranted) {
            statusMessage = "Microphone permission required for voice search."
        }
    }

    // SpeechRecognizer setup
    DisposableEffect(hasAudioPermission) {
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return@DisposableEffect onDispose {}
        }

        val isSpeechAvailable = SpeechRecognizer.isRecognitionAvailable(context)
        if (!isSpeechAvailable) {
            statusMessage = "Speech recognizer not supported on this device. Tap a suggestion below."
            return@DisposableEffect onDispose {}
        }

        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                statusMessage = "Listening... Speak song or artist name!"
            }

            override fun onBeginningOfSpeech() {
                statusMessage = "Hearing your voice..."
            }

            override fun onRmsChanged(rmsdB: Float) {
                soundLevel = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                statusMessage = "Searching tracks..."
            }

            override fun onError(error: Int) {
                isListening = false
                val msg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try speaking closer or tap below."
                    SpeechRecognizer.ERROR_NETWORK -> "Network issue. Please check internet."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout. Tap mic to try again."
                    else -> "Tap microphone or suggestion below."
                }
                statusMessage = msg
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val topMatch = matches?.firstOrNull()?.trim()
                if (!topMatch.isNullOrBlank()) {
                    recognizedText = topMatch
                    TactileFeedbackHelper.performHeartbeatHaptic(context, haptic)
                    onSpeechResult(topMatch)
                    onDismiss()
                } else {
                    statusMessage = "No song detected. Try again or tap a suggestion."
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!partial.isNullOrBlank()) {
                    recognizedText = partial
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        try {
            recognizer.startListening(intent)
        } catch (e: Exception) {
            statusMessage = "Error starting voice search. Tap suggestion below."
        }

        onDispose {
            try {
                recognizer.stopListening()
                recognizer.cancel()
                recognizer.destroy()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // Animated listening pulse waves
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_waves")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { onDismiss() }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .testTag("voice_search_dialog"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF161616)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Close button top right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.GraphicEq,
                                contentDescription = null,
                                tint = SpotifyGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Voice to Song Search",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Big Pulsating Mic Button
                    Box(
                        modifier = Modifier.size(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer glowing waves
                        if (isListening) {
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(SpotifyGreen.copy(alpha = 0.15f))
                            )
                            Box(
                                modifier = Modifier
                                    .size(115.dp)
                                    .scale(1f + soundLevel * 0.4f)
                                    .clip(CircleShape)
                                    .background(SpotifyGreen.copy(alpha = 0.25f))
                            )
                        }

                        // Center Mic circle
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .shadow(12.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            if (isListening) SpotifyGreen else Color(0xFF333333),
                                            if (isListening) Color(0xFF15883e) else Color(0xFF1E1E1E)
                                        )
                                    )
                                )
                                .border(
                                    2.dp,
                                    if (isListening) Color.White else Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = "Microphone",
                                tint = if (isListening) Color.Black else Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Recognized live transcript
                    if (recognizedText.isNotBlank()) {
                        Text(
                            text = "\"$recognizedText\"",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpotifyGreen,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Status prompt
                    Text(
                        text = statusMessage,
                        fontSize = 14.sp,
                        color = if (isListening) TextPrimary else TextMuted,
                        textAlign = TextAlign.Center,
                        fontWeight = if (isListening) FontWeight.SemiBold else FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Voice Suggestions Chips
                    Text(
                        text = "Or tap any trending song search:",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val quickVoicePills = listOf(
                        "Kesariya",
                        "Arijit Singh",
                        "Badshah",
                        "Diljit Dosanjh",
                        "Apna Bana Le",
                        "Karan Aujla",
                        "The Weeknd"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                quickVoicePills.take(3).forEach { term ->
                                    SuggestionPill(term = term) {
                                        TactileFeedbackHelper.performHeartbeatHaptic(context, haptic)
                                        onSpeechResult(term)
                                        onDismiss()
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                quickVoicePills.drop(3).take(3).forEach { term ->
                                    SuggestionPill(term = term) {
                                        TactileFeedbackHelper.performHeartbeatHaptic(context, haptic)
                                        onSpeechResult(term)
                                        onDismiss()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionPill(
    term: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "🎵",
                fontSize = 11.sp
            )
            Text(
                text = term,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}
