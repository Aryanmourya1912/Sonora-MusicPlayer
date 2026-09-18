package com.example.music

import android.content.Intent
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {
    // ... your existing session and player initialization ...

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        player?.pause()
        player?.stop()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
