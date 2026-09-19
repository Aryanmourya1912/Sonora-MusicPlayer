package com.example.music

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
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

    /**
     * Called when the user removes the app's task from Recents
     * (requires android:stopWithTask="false" in the manifest so the
     * system always delivers this callback).
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d("Sonora", "PlaybackService.onTaskRemoved fired")

        terminatePlayback()

        // The user removed the task, so fully exit the app process.
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    private fun terminatePlayback() {
        mediaSession?.let { session ->
            try {
                session.player.apply {
                    playWhenReady = false
                    stop()
                    clearMediaItems()
                    release()
                }
            } catch (_: Exception) {
                // Ignore player cleanup errors
            }

            try {
                session.release()
            } catch (_: Exception) {
                // Ignore session cleanup errors
            }
        }
        mediaSession = null

        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {
            // Service may not be in the foreground
        }

        stopSelf()
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
