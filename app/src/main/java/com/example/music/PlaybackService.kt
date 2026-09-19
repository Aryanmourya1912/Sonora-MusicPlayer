package com.example.music

import android.content.Intent
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    // Fires reliably when swiping app away or clicking "Clear All" in Android Recents
    override fun onTaskRemoved(rootIntent: Intent?) {
        mediaSession?.player?.apply {
            pause()
            stop()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        super.onTaskRemoved(rootIntent)
        
        // Hard kill the process to bypass manufacturer background restrictions
        System.exit(0)
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.pause()
            player.stop()
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}