package com.example.music

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    private val killReceiver = object : BroadcastReceiver() {

        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {
            if (intent?.action == ACTION_KILL_SERVICE) {
                terminatePlayback()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this).build()

        mediaSession = MediaSession.Builder(this, player)
        .build()

        ContextCompat.registerReceiver(
            this,
            killReceiver,
            IntentFilter(ACTION_KILL_SERVICE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (intent?.action == ACTION_STOP_SERVICE) {
            terminatePlayback()
            return START_NOT_STICKY
        }

        super.onStartCommand(
            intent,
            flags,
            startId
        )

        return START_NOT_STICKY
    }

    @OptIn(UnstableApi::class)
    override fun onTaskRemoved(
        rootIntent: Intent?
    ) {
        pauseAllPlayersAndStopSelf()
    }

    private fun terminatePlayback() {
        try {
            mediaSession?.player?.apply {
                playWhenReady = false
                stop()
                clearMediaItems()
                release()
            }

            mediaSession?.release()
            mediaSession = null

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()

        } catch (_: Exception) {
            // Ignore cleanup errors
        }
    }

    override fun onDestroy() {

        try {
            unregisterReceiver(killReceiver)
        } catch (_: Exception) {
            // Receiver already unregistered
        }

        terminatePlayback()

        super.onDestroy()
    }

    companion object {
        private const val ACTION_KILL_SERVICE =
        "com.example.music.ACTION_KILL_SERVICE"

        private const val ACTION_STOP_SERVICE =
        "ACTION_STOP_SERVICE"
    }
}