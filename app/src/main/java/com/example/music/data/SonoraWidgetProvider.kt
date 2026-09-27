package com.example.music

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture

class SonoraWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_PLAY_PAUSE =
            "com.example.music.widget.PLAY_PAUSE"

        const val ACTION_PREVIOUS =
            "com.example.music.widget.PREVIOUS"

        const val ACTION_NEXT =
            "com.example.music.widget.NEXT"

        const val ACTION_OPEN_APP =
            "com.example.music.widget.OPEN_APP"

        fun updateAllWidgets(context: Context, player: Player?) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(
                context,
                SonoraWidgetProvider::class.java
            )

            val widgetIds = manager.getAppWidgetIds(component)

            for (widgetId in widgetIds) {
                updateWidget(
                    context,
                    manager,
                    widgetId,
                    player
                )
            }
        }

        private fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int,
            player: Player?
        ) {
            val views = RemoteViews(
                context.packageName,
                R.layout.widget_sonora
            )

            val hasTrack =
                player != null &&
                player.mediaItemCount > 0 &&
                player.currentMediaItem != null

            if (hasTrack) {
                val metadata = player!!.mediaMetadata

                val title = metadata.title?.toString()
                    ?.takeIf { it.isNotBlank() }
                    ?: "Unknown Song"

                val artist = metadata.artist?.toString()
                    ?.takeIf { it.isNotBlank() }
                    ?: "Unknown Artist"

                views.setTextViewText(
                    R.id.widget_song_title,
                    title
                )

                views.setTextViewText(
                    R.id.widget_artist,
                    artist
                )

                views.setImageViewResource(
                    R.id.widget_play_pause,
                    if (player.isPlaying) {
                        android.R.drawable.ic_media_pause
                    } else {
                        android.R.drawable.ic_media_play
                    }
                )
            } else {
                views.setTextViewText(
                    R.id.widget_song_title,
                    "Sonora"
                )

                views.setTextViewText(
                    R.id.widget_artist,
                    "No music playing"
                )

                views.setImageViewResource(
                    R.id.widget_play_pause,
                    android.R.drawable.ic_media_play
                )
            }

            // Open app
            val openIntent = Intent(
                context,
                MainActivity::class.java
            )

            val openPendingIntent = PendingIntent.getActivity(
                context,
                100,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(
                R.id.widget_container,
                openPendingIntent
            )

            // Play / Pause
            views.setOnClickPendingIntent(
                R.id.widget_play_pause,
                createActionPendingIntent(
                    context,
                    ACTION_PLAY_PAUSE,
                    101
                )
            )

            // Previous
            views.setOnClickPendingIntent(
                R.id.widget_previous,
                createActionPendingIntent(
                    context,
                    ACTION_PREVIOUS,
                    102
                )
            )

            // Next
            views.setOnClickPendingIntent(
                R.id.widget_next,
                createActionPendingIntent(
                    context,
                    ACTION_NEXT,
                    103
                )
            )

            manager.updateAppWidget(
                widgetId,
                views
            )
        }

        private fun createActionPendingIntent(
            context: Context,
            action: String,
            requestCode: Int
        ): PendingIntent {

            val intent = Intent(
                context,
                SonoraWidgetProvider::class.java
            ).apply {
                this.action = action
            }

            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateAllWidgets(
            context,
            null
        )
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        super.onReceive(context, intent)

        when (intent.action) {

            ACTION_PLAY_PAUSE -> {
                sendPlayerCommand(context) { player ->
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        player.play()
                    }
                }
            }

            ACTION_PREVIOUS -> {
                sendPlayerCommand(context) { player ->
                    player.seekToPrevious()
                }
            }

            ACTION_NEXT -> {
                sendPlayerCommand(context) { player ->
                    player.seekToNext()
                }
            }
        }
    }

    private fun sendPlayerCommand(
        context: Context,
        command: (Player) -> Unit
    ) {
        val pendingResult = goAsync()

        val sessionToken = SessionToken(
            context,
            ComponentName(
                context,
                PlaybackService::class.java
            )
        )

        val controllerFuture =
            MediaController.Builder(
                context,
                sessionToken
            ).buildAsync()

        controllerFuture.addListener(
            {
                try {
                    val controller =
                        controllerFuture.get()

                    command(controller)

                    updateAllWidgets(
                        context,
                        controller
                    )

                    controller.release()

                } catch (_: Exception) {
                } finally {
                    pendingResult.finish()
                }
            },
            context.mainExecutor
        )
    }
}