package com.example.music

import android.content.Context
import android.content.SharedPreferences
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log

object EqualizerManager {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var currentAudioSessionId: Int = 0
    private var prefs: SharedPreferences? = null

    var isEnabled: Boolean = true
        private set

    var currentPreset: String = "Flat"
        private set

    // 5 frequency band gain levels in dB (-12 dB to +12 dB)
    val bandLevels = floatArrayOf(0f, 0f, 0f, 0f, 0f)

    // Extra enhancements: Bass Boost (0 to 1000) & Virtualizer (0 to 1000)
    var bassBoostStrength: Int = 0
        private set
    var virtualizerStrength: Int = 0
        private set

    val bandLabels = listOf(
        "60 Hz" to "Sub-Bass",
        "230 Hz" to "Bass",
        "910 Hz" to "Midrange",
        "3.6 kHz" to "Presence",
        "14 kHz" to "Treble"
    )

    fun init(context: Context) {
        prefs = context.getSharedPreferences("sonora_equalizer", Context.MODE_PRIVATE)
        loadSettings()
    }

    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        if (audioSessionId == currentAudioSessionId && equalizer != null) return

        currentAudioSessionId = audioSessionId
        try {
            releaseEffects()

            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = isEnabled
            }

            try {
                bassBoost = BassBoost(0, audioSessionId).apply {
                    enabled = isEnabled && strengthSupported
                    if (strengthSupported) setStrength(bassBoostStrength.toShort())
                }
            } catch (_: Exception) {}

            try {
                virtualizer = Virtualizer(0, audioSessionId).apply {
                    enabled = isEnabled && strengthSupported
                    if (strengthSupported) setStrength(virtualizerStrength.toShort())
                }
            } catch (_: Exception) {}

            applyAllSettings()
            Log.d("SonoraEQ", "Equalizer attached to AudioSession: $audioSessionId")
        } catch (e: Exception) {
            Log.e("SonoraEQ", "Failed to initialize Equalizer effects", e)
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled && (bassBoost?.strengthSupported == true)
            virtualizer?.enabled = enabled && (virtualizer?.strengthSupported == true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        saveSettings()
    }

    fun setBandLevel(bandIndex: Int, dbValue: Float) {
        if (bandIndex in bandLevels.indices) {
            bandLevels[bandIndex] = dbValue.coerceIn(-12f, 12f)
            try {
                val mB = (bandLevels[bandIndex] * 100).toInt().toShort()
                equalizer?.setBandLevel(bandIndex.toShort(), mB)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            currentPreset = "Custom"
            saveSettings()
        }
    }

    fun setBassBoost(strength: Int) {
        bassBoostStrength = strength.coerceIn(0, 1000)
        try {
            if (bassBoost?.strengthSupported == true) {
                bassBoost?.setStrength(bassBoostStrength.toShort())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        saveSettings()
    }

    fun setVirtualizer(strength: Int) {
        virtualizerStrength = strength.coerceIn(0, 1000)
        try {
            if (virtualizer?.strengthSupported == true) {
                virtualizer?.setStrength(virtualizerStrength.toShort())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        saveSettings()
    }

    fun applyPreset(presetName: String) {
        currentPreset = presetName
        val targetLevels = when (presetName) {
            "Bass Booster" -> floatArrayOf(6f, 4f, 1f, 0f, -1f)
            "Vocal Booster" -> floatArrayOf(-2f, 1f, 5f, 3f, 0f)
            "Rock" -> floatArrayOf(4f, 2f, -1f, 3f, 5f)
            "Pop" -> floatArrayOf(-1f, 2f, 4f, 2f, -1f)
            "Jazz" -> floatArrayOf(3f, 1f, -2f, 2f, 4f)
            "Classical" -> floatArrayOf(4f, 2f, -1f, 2f, 3f)
            "Electronic" -> floatArrayOf(5f, 3f, 0f, 2f, 4f)
            else -> floatArrayOf(0f, 0f, 0f, 0f, 0f) // Flat
        }

        for (i in 0 until 5) {
            bandLevels[i] = targetLevels[i]
            try {
                val mB = (bandLevels[i] * 100).toInt().toShort()
                equalizer?.setBandLevel(i.toShort(), mB)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        saveSettings()
    }

    private fun applyAllSettings() {
        try {
            equalizer?.enabled = isEnabled
            val numBands = equalizer?.numberOfBands?.toInt() ?: 5
            for (i in 0 until numBands.coerceAtMost(5)) {
                val mB = (bandLevels[i] * 100).toInt().toShort()
                equalizer?.setBandLevel(i.toShort(), mB)
            }
            if (bassBoost?.strengthSupported == true) {
                bassBoost?.setStrength(bassBoostStrength.toShort())
            }
            if (virtualizer?.strengthSupported == true) {
                virtualizer?.setStrength(virtualizerStrength.toShort())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveSettings() {
        prefs?.edit()?.apply {
            putBoolean("eq_enabled", isEnabled)
            putString("eq_preset", currentPreset)
            for (i in 0 until 5) {
                putFloat("eq_band_$i", bandLevels[i])
            }
            putInt("eq_bass_boost", bassBoostStrength)
            putInt("eq_virtualizer", virtualizerStrength)
            apply()
        }
    }

    private fun loadSettings() {
        prefs?.let { p ->
            isEnabled = p.getBoolean("eq_enabled", true)
            currentPreset = p.getString("eq_preset", "Flat") ?: "Flat"
            for (i in 0 until 5) {
                bandLevels[i] = p.getFloat("eq_band_$i", 0f)
            }
            bassBoostStrength = p.getInt("eq_bass_boost", 0)
            virtualizerStrength = p.getInt("eq_virtualizer", 0)
        }
    }

    fun releaseEffects() {
        try {
            equalizer?.release()
        } catch (_: Exception) {}
        try {
            bassBoost?.release()
        } catch (_: Exception) {}
        try {
            virtualizer?.release()
        } catch (_: Exception) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
    }
}