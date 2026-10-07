package com.example.player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class RepeatMode {
    OFF, ALL, ONE
}

class AudioSynthesizerPlayer(private val scope: CoroutineScope) {
    private var audioTrack: AudioTrack? = null
    private var synthesisJob: Job? = null

    @Volatile
    var isPlaying: Boolean = false
        private set

    @Volatile
    var currentPositionMs: Long = 0L
        private set

    var durationMs: Long = 240000L
    var onSongCompleted: (() -> Unit)? = null
    var onPositionUpdated: ((Long) -> Unit)? = null

    private val sampleRate = 22050
    private var currentPitchBase = 220.0

    fun playSong(duration: Long, songId: String, startPositionMs: Long = 0L) {
        durationMs = duration
        currentPositionMs = startPositionMs

        // Pick a harmonic base frequency based on song id
        val hash = songId.hashCode()
        currentPitchBase = when (Math.abs(hash) % 5) {
            0 -> 261.63 // C4
            1 -> 293.66 // D4
            2 -> 329.63 // E4
            3 -> 349.23 // F4
            else -> 392.00 // G4
        }

        stopAudioThread()
        isPlaying = true
        startAudioThread()
    }

    fun resume() {
        if (!isPlaying) {
            isPlaying = true
            startAudioThread()
        }
    }

    fun pause() {
        isPlaying = false
        stopAudioThread()
    }

    fun seekTo(positionMs: Long) {
        currentPositionMs = positionMs.coerceIn(0L, durationMs)
        onPositionUpdated?.invoke(currentPositionMs)
    }

    private fun startAudioThread() {
        synthesisJob = scope.launch(Dispatchers.Default) {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()
            } catch (e: Exception) {
                Log.w("AudioPlayer", "AudioTrack init failed, running silent time progression", e)
            }

            val buffer = ShortArray(1024)
            var phase = 0.0
            var tickCounter = 0

            // Pentatonic scale multipliers
            val scale = doubleArrayOf(1.0, 1.125, 1.25, 1.5, 1.666, 2.0)

            while (isActive && isPlaying) {
                if (currentPositionMs >= durationMs) {
                    isPlaying = false
                    launch(Dispatchers.Main) {
                        onSongCompleted?.invoke()
                    }
                    break
                }

                // Generate pleasant rhythmic ambient chord
                val noteIndex = ((currentPositionMs / 400) % scale.size).toInt()
                val freq = currentPitchBase * scale[noteIndex]
                val phaseIncrement = (2.0 * Math.PI * freq) / sampleRate

                for (i in buffer.indices) {
                    val sample = (sin(phase) * 0.25 * 32767.0).toInt().toShort()
                    buffer[i] = sample
                    phase += phaseIncrement
                    if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI
                }

                try {
                    audioTrack?.write(buffer, 0, buffer.size)
                } catch (e: Exception) {
                    // Fallback
                }

                val bufferDurationMs = (buffer.size * 1000L) / sampleRate
                currentPositionMs += bufferDurationMs
                tickCounter++

                if (tickCounter % 3 == 0) {
                    val pos = currentPositionMs
                    launch(Dispatchers.Main) {
                        onPositionUpdated?.invoke(pos)
                    }
                }
            }

            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (e: Exception) {
                // Ignore cleanup error
            }
            audioTrack = null
        }
    }

    private fun stopAudioThread() {
        synthesisJob?.cancel()
        synthesisJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignore
        }
        audioTrack = null
    }

    fun release() {
        stopAudioThread()
    }
}
