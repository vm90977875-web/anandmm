package com.example.receiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class BeatifyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences("beatify_prefs", Context.MODE_PRIVATE)
        val title = prefs.getString("WIDGET_SONG_TITLE", "Beatify Music") ?: "Beatify Music"
        val artist = prefs.getString("WIDGET_SONG_ARTIST", "Stream & Discover") ?: "Stream & Discover"
        val isPlaying = prefs.getBoolean("WIDGET_IS_PLAYING", false)

        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val appPendingIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_beatify_standard)
            views.setTextViewText(R.id.widget_standard_title, title)
            views.setTextViewText(R.id.widget_standard_artist, artist)
            views.setImageViewResource(
                R.id.widget_standard_play_btn,
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )
            views.setOnClickPendingIntent(R.id.widget_standard_root, appPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_standard_play_btn, appPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_standard_prev_btn, appPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_standard_next_btn, appPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context, title: String, artist: String, isPlaying: Boolean) {
            val prefs = context.getSharedPreferences("beatify_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("WIDGET_SONG_TITLE", title)
                .putString("WIDGET_SONG_ARTIST", artist)
                .putBoolean("WIDGET_IS_PLAYING", isPlaying)
                .apply()

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, BeatifyWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (allWidgetIds.isNotEmpty()) {
                val intent = Intent(context, BeatifyWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
