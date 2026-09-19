package com.example.music

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    private val killReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            terminatePlayback()
        }
    }

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()

        ContextCompat.registerReceiver(
            this,
            killReceiver,
            IntentFilter("com.example.music.ACTION_KILL_SERVICE"),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "ACTION_STOP_SERVICE") {
            terminatePlayback()
            return START_NOT_STICKY
        }
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    // Triggered reliably when swiping away or tapping "Close" / "Clear All" in Recents
    override fun onTaskRemoved(rootIntent: Intent?) {
        terminatePlayback()
        super.onTaskRemoved(rootIntent)
    }

    private fun terminatePlayback() {
        try {
            mediaSession?.player?.apply {
                playWhenReady = false
                pause()
                stop()
                clearMediaItems()
                release()
            }
            mediaSession?.release()
            mediaSession = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } catch (_: Exception) {}

        android.os.Process.killProcess(android.os.Process.myPid())
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(killReceiver)
        } catch (_: Exception) {}

        terminatePlayback()
        super.onDestroy()
    }
}