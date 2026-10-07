package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object TactileFeedbackHelper {

    /**
     * Produces a distinct double-thump "heartbeat" tactile sensation (💓 lub-dub)
     * when a user likes a song, giving authentic physical haptic feedback.
     */
    fun performHeartbeatHaptic(context: Context, composeHaptic: HapticFeedback? = null) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Timing: 0ms initial wait, 45ms pulse 1, 60ms rest, 75ms pulse 2
                    val timings = longArrayOf(0, 45, 60, 75)
                    // Amplitudes: 0, medium pulse (160), rest (0), strong pulse (255)
                    val amplitudes = intArrayOf(0, 160, 0, 255)
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vibrator.vibrate(effect)
                    return
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 45, 60, 75), -1)
                    return
                }
            }
        } catch (e: Exception) {
            // Fallback to compose haptic feedback
        }

        try {
            composeHaptic?.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            // Ignored
        }
    }

    /**
     * Subtle tick for rotary knob adjustment or slider movement
     */
    fun performTick(context: Context, composeHaptic: HapticFeedback? = null) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    return
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(18, 120))
                    return
                }
            }
        } catch (e: Exception) {
            // Fallback
        }

        try {
            composeHaptic?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (e: Exception) {
            // Ignored
        }
    }
}
