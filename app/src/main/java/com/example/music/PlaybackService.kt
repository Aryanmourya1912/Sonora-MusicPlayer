package com.example.music

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {

```
private var mediaSession: MediaSession? = null

private val serviceHandler = Handler(Looper.getMainLooper())

/*
 * Checks whether Sonora still has a task in Android's task manager.
 *
 * This is used as a fallback for OEM launchers such as Realme UI
 * where "Clear all" may not behave exactly like swiping one app.
 */
private val taskMonitor = object : Runnable {
    override fun run() {

        if (mediaSession == null) {
            return
        }

        try {
            val activityManager =
                getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

            val appTasks = activityManager.appTasks

            /*
             * If there are no tasks belonging to this application,
             * the user has removed Sonora from the task list.
             *
             * Stop playback.
             */
            if (appTasks.isEmpty()) {
                terminatePlayback()
                return
            }

        } catch (_: Exception) {
            // Ignore temporary ActivityManager errors.
        }

        /*
         * Check again after 1 second.
         */
        serviceHandler.postDelayed(this, 1000L)
    }
}

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

    /*
     * Create the ExoPlayer used by this service.
     */
    val player = ExoPlayer.Builder(this).build()

    /*
     * Create the MediaSession around the player.
     */
    mediaSession = MediaSession.Builder(this, player)
        .build()

    /*
     * Register manual kill receiver.
     */
    ContextCompat.registerReceiver(
        this,
        killReceiver,
        IntentFilter(ACTION_KILL_SERVICE),
        ContextCompat.RECEIVER_NOT_EXPORTED
    )

    /*
     * Start monitoring the app's task.
     */
    serviceHandler.post(taskMonitor)
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

    when (intent?.action) {

        ACTION_STOP_SERVICE -> {
            terminatePlayback()
            return START_NOT_STICKY
        }
    }

    super.onStartCommand(intent, flags, startId)

    return START_NOT_STICKY
}

/*
 * Normal Android/Media3 Recents removal path.
 */
@OptIn(UnstableApi::class)
override fun onTaskRemoved(rootIntent: Intent?) {

    /*
     * Stop all Media3 players and terminate the service.
     */
    pauseAllPlayersAndStopSelf()
}

/*
 * Completely stop playback and release Media3 resources.
 */
private fun terminatePlayback() {

    try {

        /*
         * Stop our ExoPlayer.
         */
        mediaSession?.player?.apply {

            playWhenReady = false
            pause()
            stop()
            clearMediaItems()
            release()
        }

        /*
         * Release MediaSession.
         */
        mediaSession?.release()
        mediaSession = null

    } catch (_: Exception) {
        // Ignore cleanup errors.
    }

    /*
     * Stop monitoring.
     */
    serviceHandler.removeCallbacks(taskMonitor)

    /*
     * Remove foreground notification/service state.
     */
    try {
        stopForeground(STOP_FOREGROUND_REMOVE)
    } catch (_: Exception) {
        // Ignore cleanup errors.
    }

    /*
     * Stop this service.
     */
    stopSelf()
}

override fun onDestroy() {

    /*
     * Stop task monitoring first.
     */
    serviceHandler.removeCallbacks(taskMonitor)

    /*
     * Unregister receiver.
     */
    try {
        unregisterReceiver(killReceiver)
    } catch (_: Exception) {
        // Already unregistered.
    }

    /*
     * Final cleanup.
     */
    try {
        mediaSession?.player?.release()
    } catch (_: Exception) {
        // Ignore.
    }

    try {
        mediaSession?.release()
    } catch (_: Exception) {
        // Ignore.
    }

    mediaSession = null

    super.onDestroy()
}

companion object {

    const val ACTION_KILL_SERVICE =
        "com.example.music.ACTION_KILL_SERVICE"

    const val ACTION_STOP_SERVICE =
        "com.example.music.ACTION_STOP_SERVICE"
}
```

}
