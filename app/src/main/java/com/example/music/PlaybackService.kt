package com.example.music

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import android.media.audiofx.LoudnessEnhancer
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private val closeCommand = SessionCommand(ACTION_CLOSE_APP, Bundle.EMPTY)

    private val sessionCallback = object : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands =
                MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                    .buildUpon()
                    .add(closeCommand)
                    .build()

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == ACTION_CLOSE_APP) {
                Log.d("Sonora", "Close button tapped in notification")
                exitApp()
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return super.onCustomCommand(session, controller, customCommand, args)
        }
    }

    private val killReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_KILL_SERVICE) {
                terminatePlayback()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        EqualizerManager.init(this, player.audioSessionId)
        
        // 🟢 Initialize Hardware Volume Normalization
        try {
            loudnessEnhancer = LoudnessEnhancer(player.audioSessionId).apply {
                // Target boost (in millibels, 250 mB = 2.5 dB leveling target)
                setTargetGain(250)
                enabled = true
            }
        } catch (e: Exception) {
            Log.e("Sonora", "LoudnessEnhancer initialization error: ${e.message}")
        }

        val closeButton = CommandButton.Builder()
            .setDisplayName("Close")
            .setIconResId(R.drawable.ic_close)
            .setSessionCommand(closeCommand)
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(sessionCallback)
            .setCustomLayout(ImmutableList.of(closeButton))
            .build()

        ContextCompat.registerReceiver(
            this,
            killReceiver,
            IntentFilter(ACTION_KILL_SERVICE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            terminatePlayback()
            return START_NOT_STICKY
        }
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d("Sonora", "PlaybackService.onTaskRemoved fired")
        exitApp()
    }

    private fun exitApp() {
        Handler(Looper.getMainLooper()).post {
            terminatePlayback()
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    private fun terminatePlayback() {
        EqualizerManager.release()
        
        // 🟢 Release LoudnessEnhancer
        try {
            loudnessEnhancer?.release()
            loudnessEnhancer = null
        } catch (_: Exception) {}

        mediaSession?.let { session ->
            try {
                session.player.apply {
                    playWhenReady = false
                    stop()
                    clearMediaItems()
                    release()
                }
            } catch (_: Exception) {}

            try {
                session.release()
            } catch (_: Exception) {}
        }
        mediaSession = null

        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {}

        stopSelf()
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(killReceiver)
        } catch (_: Exception) {}

        terminatePlayback()
        super.onDestroy()
    }

    companion object {
        private const val ACTION_KILL_SERVICE = "com.example.music.ACTION_KILL_SERVICE"
        private const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        private const val ACTION_CLOSE_APP = "com.example.music.ACTION_CLOSE_APP"
    }
}