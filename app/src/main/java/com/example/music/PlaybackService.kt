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

<<<<<<< HEAD
    // Triggered when swiped away or cleared via Recents "Close / Clear All"
=======
>>>>>>> 43af0804c1d7fdb68456f0225575c0d2606a170e
    override fun onTaskRemoved(rootIntent: Intent?) {
        mediaSession?.player?.apply {
            pause()
            stop()
        }
<<<<<<< HEAD
        stopForeground(STOP_FOREGROUND_REMOVE)
=======
>>>>>>> 43af0804c1d7fdb68456f0225575c0d2606a170e
        stopSelf()
        super.onTaskRemoved(rootIntent)
        android.os.Process.killProcess(android.os.Process.myPid())
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