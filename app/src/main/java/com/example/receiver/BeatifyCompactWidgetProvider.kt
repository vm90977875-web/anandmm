package com.example.receiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class BeatifyCompactWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences("beatify_prefs", Context.MODE_PRIVATE)
        val title = prefs.getString("WIDGET_SONG_TITLE", "Beatify") ?: "Beatify"
        val artist = prefs.getString("WIDGET_SONG_ARTIST", "Now Playing") ?: "Now Playing"
        val isPlaying = prefs.getBoolean("WIDGET_IS_PLAYING", false)

        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            1,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_beatify_compact)
            views.setTextViewText(R.id.widget_compact_title, title)
            views.setTextViewText(R.id.widget_compact_artist, artist)
            views.setImageViewResource(
                R.id.widget_compact_play_btn,
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )
            views.setOnClickPendingIntent(R.id.widget_compact_root, appPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_compact_play_btn, appPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
