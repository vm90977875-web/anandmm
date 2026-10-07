package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.util.MusicNotificationHelper
import com.example.util.PlaybackBridge

class MusicActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("MusicActionReceiver", "Received notification action: $action")

        when (action) {
            ACTION_PLAY_PAUSE -> {
                PlaybackBridge.onPlayPause?.invoke()
            }
            ACTION_NEXT -> {
                PlaybackBridge.onNext?.invoke()
            }
            ACTION_PREV -> {
                PlaybackBridge.onPrev?.invoke()
            }
            ACTION_SKIP_10 -> {
                PlaybackBridge.onSkip10?.invoke()
            }
            ACTION_STOP -> {
                PlaybackBridge.onStop?.invoke()
                MusicNotificationHelper.cancelNotification(context)
            }
        }
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.ACTION_NEXT"
        const val ACTION_PREV = "com.example.ACTION_PREV"
        const val ACTION_SKIP_10 = "com.example.ACTION_SKIP_10"
        const val ACTION_STOP = "com.example.ACTION_STOP"
    }
}
