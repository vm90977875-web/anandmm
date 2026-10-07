package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.Song
import com.example.receiver.MusicActionReceiver

object MusicNotificationHelper {
    private const val CHANNEL_ID = "beatify_playback_channel"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Beatify High-Fidelity Playback"
            val descriptionText = "Music playback controls with skip, previous, pause and stop"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showPlaybackNotification(
        context: Context,
        song: Song,
        isPlaying: Boolean
    ) {
        try {
            createNotificationChannel(context)

            // Open app on click
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingAppIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Broadcast pending intents for playback actions
            fun createActionPendingIntent(action: String, requestCode: Int): PendingIntent {
                val intent = Intent(context, MusicActionReceiver::class.java).apply {
                    this.action = action
                }
                return PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            val prevPending = createActionPendingIntent(MusicActionReceiver.ACTION_PREV, 101)
            val playPausePending = createActionPendingIntent(MusicActionReceiver.ACTION_PLAY_PAUSE, 102)
            val nextPending = createActionPendingIntent(MusicActionReceiver.ACTION_NEXT, 103)
            val skip10Pending = createActionPendingIntent(MusicActionReceiver.ACTION_SKIP_10, 104)
            val stopPending = createActionPendingIntent(MusicActionReceiver.ACTION_STOP, 105)

            val statusText = if (isPlaying) "Playing • 320 kbps Studio" else "Paused • 320 kbps Studio"

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(if (isPlaying) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause)
                .setContentTitle(song.title)
                .setContentText("${song.artist} • ${song.album}")
                .setSubText(statusText)
                .setContentIntent(pendingAppIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(isPlaying)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSilent(true)
                .setShowWhen(false)
                // Action 1: Previous Song
                .addAction(
                    android.R.drawable.ic_media_previous,
                    "Previous",
                    prevPending
                )
                // Action 2: Play/Pause Toggle
                .addAction(
                    if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                    if (isPlaying) "Pause" else "Play",
                    playPausePending
                )
                // Action 3: Next (Skip) Song
                .addAction(
                    android.R.drawable.ic_media_next,
                    "Skip",
                    nextPending
                )
                // Action 4: Skip +10s Forward
                .addAction(
                    android.R.drawable.ic_media_ff,
                    "+10s",
                    skip10Pending
                )
                // Action 5: Stop playback immediately & dismiss
                .addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Stop",
                    stopPending
                )

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        } catch (e: Exception) {
            // Safe fallback if notifications fail
        }
    }

    fun cancelNotification(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            // Ignored
        }
    }
}
