package com.example.music

import android.content.Context
import android.content.SharedPreferences
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class EqualizerBand(
    val index: Short,
    val centerFreqHz: Int,
    val centerFreqLabel: String,
    val levelMb: Short
)

object EqualizerManager {
    private const val PREFS_NAME = "sonora_equalizer_prefs"
    private const val KEY_ENABLED = "eq_enabled"
    private const val KEY_PRESET = "eq_preset"
    private const val KEY_BASS_BOOST = "eq_bass_boost"
    private const val KEY_VIRTUALIZER = "eq_virtualizer"
    private const val KEY_BAND_PREFIX = "eq_band_"

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var currentSessionId: Int = 0

    var isEnabled by mutableStateOf(true)
        private set

    var minLevelMb by mutableStateOf((-1500).toShort())
        private set

    var maxLevelMb by mutableStateOf((1500).toShort())
        private set

    val bands = mutableStateListOf<EqualizerBand>()

    val presets = mutableStateListOf<String>()

    var currentPresetIndex by mutableIntStateOf(0)
        private set

    var bassBoostStrength by mutableFloatStateOf(0f)
        private set

    var isBassBoostSupported by mutableStateOf(true)
        private set

    var virtualizerStrength by mutableFloatStateOf(0f)
        private set

    var isVirtualizerSupported by mutableStateOf(true)
        private set

    private val defaultPresets = listOf(
        "Flat" to listOf(0, 0, 0, 0, 0),
        "Bass Boost" to listOf(600, 450, 200, 0, 0),
        "Rock" to listOf(500, 300, -100, 300, 500),
        "Pop" to listOf(-100, 200, 500, 200, -200),
        "Hip Hop" to listOf(500, 300, 0, 200, 350),
        "Jazz" to listOf(300, 200, -200, 200, 400),
        "Classical" to listOf(400, 300, -100, 300, 400),
        "Electronic" to listOf(450, 200, 0, 200, 450),
        "Vocal Boost" to listOf(-200, 0, 500, 300, -100)
    )

    init {
        // Pre-populate default 5-band layout so the UI is ready even before audio starts
        val initialLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
        val initialFreqs = listOf(60, 230, 910, 3600, 14000)
        for (i in 0 until 5) {
            bands.add(EqualizerBand(i.toShort(), initialFreqs[i], initialLabels[i], 0.toShort()))
        }
        defaultPresets.forEach { presets.add(it.first) }
        presets.add("Custom")
    }

    fun init(context: Context, audioSessionId: Int) {
        if (audioSessionId <= 0) return
        if (currentSessionId == audioSessionId && equalizer != null) return

        release()
        currentSessionId = audioSessionId

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedEnabled = prefs.getBoolean(KEY_ENABLED, true)
        val savedBass = prefs.getInt(KEY_BASS_BOOST, 0)
        val savedVirt = prefs.getInt(KEY_VIRTUALIZER, 0)
        val savedPreset = prefs.getInt(KEY_PRESET, 0)

        isEnabled = savedEnabled
        bassBoostStrength = savedBass.toFloat()
        virtualizerStrength = savedVirt.toFloat()
        currentPresetIndex = savedPreset

        try {
            val eq = Equalizer(0, audioSessionId)
            eq.enabled = savedEnabled

            val range = eq.bandLevelRange
            if (range != null && range.size >= 2) {
                minLevelMb = range[0]
                maxLevelMb = range[1]
            }

            val numBands = eq.numberOfBands
            val bandList = ArrayList<EqualizerBand>()

            for (i in 0 until numBands) {
                val bandIdx = i.toShort()
                val centerFreqMhz = eq.getCenterFreq(bandIdx)
                val freqHz = centerFreqMhz / 1000
                val label = if (freqHz >= 1000) {
                    val k = freqHz / 1000.0
                    if (k % 1.0 == 0.0) "${k.toInt()} kHz" else String.format(java.util.Locale.ROOT, "%.1f kHz", k)
                } else {
                    "$freqHz Hz"
                }

                val savedLevel = prefs.getInt("$KEY_BAND_PREFIX$i", Int.MIN_VALUE)
                val actualLevel = if (savedLevel != Int.MIN_VALUE) {
                    val clamped = savedLevel.coerceIn(minLevelMb.toInt(), maxLevelMb.toInt()).toShort()
                    eq.setBandLevel(bandIdx, clamped)
                    clamped
                } else {
                    eq.getBandLevel(bandIdx)
                }

                bandList.add(EqualizerBand(bandIdx, freqHz, label, actualLevel))
            }

            bands.clear()
            bands.addAll(bandList)

            val presetList = mutableListOf<String>()
            val numPresets = eq.numberOfPresets
            if (numPresets > 0) {
                for (p in 0 until numPresets) {
                    presetList.add(eq.getPresetName(p.toShort()))
                }
            } else {
                defaultPresets.forEach { presetList.add(it.first) }
            }
            if (!presetList.contains("Custom")) {
                presetList.add("Custom")
            }

            presets.clear()
            presets.addAll(presetList)

            equalizer = eq
        } catch (e: Exception) {
            Log.e("Sonora", "Equalizer init error: ${e.message}")
            equalizer = null
        }

        try {
            val bb = BassBoost(0, audioSessionId)
            isBassBoostSupported = bb.strengthSupported
            if (isBassBoostSupported) {
                bb.enabled = savedEnabled
                bb.setStrength(savedBass.toShort().coerceIn(0, 1000))
            }
            bassBoost = bb
        } catch (e: Exception) {
            isBassBoostSupported = false
            bassBoost = null
        }

        try {
            val virt = Virtualizer(0, audioSessionId)
            isVirtualizerSupported = virt.strengthSupported
            if (isVirtualizerSupported) {
                virt.enabled = savedEnabled
                virt.setStrength(savedVirt.toShort().coerceIn(0, 1000))
            }
            virtualizer = virt
        } catch (e: Exception) {
            isVirtualizerSupported = false
            virtualizer = null
        }
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        isEnabled = enabled
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()

        try {
            equalizer?.enabled = enabled
            if (isBassBoostSupported) bassBoost?.enabled = enabled
            if (isVirtualizerSupported) virtualizer?.enabled = enabled
        } catch (_: Exception) {}
    }

    fun setBandLevel(context: Context, bandIndex: Short, levelMb: Short) {
        val clamped = levelMb.coerceIn(minLevelMb, maxLevelMb)
        try {
            equalizer?.setBandLevel(bandIndex, clamped)
        } catch (_: Exception) {}

        val idx = bands.indexOfFirst { it.index == bandIndex }
        if (idx >= 0) {
            bands[idx] = bands[idx].copy(levelMb = clamped)
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt("$KEY_BAND_PREFIX$bandIndex", clamped.toInt())
            .apply()

        val customIdx = presets.indexOf("Custom").takeIf { it >= 0 } ?: (presets.size - 1)
        if (currentPresetIndex != customIdx) {
            currentPresetIndex = customIdx
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putInt(KEY_PRESET, customIdx)
                .apply()
        }
    }

    fun applyPreset(context: Context, presetIndex: Int) {
        if (presetIndex < 0 || presetIndex >= presets.size) return
        currentPresetIndex = presetIndex
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_PRESET, presetIndex)
            .apply()

        val customIdx = presets.indexOf("Custom")
        if (presetIndex == customIdx) return

        val eq = equalizer
        val numNativePresets = eq?.numberOfPresets ?: 0

        if (numNativePresets > 0 && presetIndex < numNativePresets) {
            try {
                eq?.usePreset(presetIndex.toShort())
                for (i in bands.indices) {
                    val bandIdx = bands[i].index
                    val lvl = eq?.getBandLevel(bandIdx) ?: 0.toShort()
                    bands[i] = bands[i].copy(levelMb = lvl)
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                        .putInt("$KEY_BAND_PREFIX$bandIdx", lvl.toInt())
                        .apply()
                }
            } catch (e: Exception) {
                Log.e("Sonora", "usePreset error: ${e.message}")
            }
        } else {
            val presetName = presets[presetIndex]
            val pair = defaultPresets.find { it.first.equals(presetName, ignoreCase = true) }
            if (pair != null) {
                val levels = pair.second
                for (i in bands.indices) {
                    val lvl = (if (i < levels.size) levels[i] else 0).toShort()
                    val clamped = lvl.coerceIn(minLevelMb, maxLevelMb)
                    try {
                        eq?.setBandLevel(bands[i].index, clamped)
                    } catch (_: Exception) {}
                    bands[i] = bands[i].copy(levelMb = clamped)
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                        .putInt("$KEY_BAND_PREFIX${bands[i].index}", clamped.toInt())
                        .apply()
                }
            }
        }
    }

    fun setBassBoost(context: Context, strength: Float) {
        val clamped = strength.coerceIn(0f, 1000f)
        bassBoostStrength = clamped
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_BASS_BOOST, clamped.toInt())
            .apply()

        if (isBassBoostSupported) {
            try {
                bassBoost?.setStrength(clamped.toInt().toShort())
            } catch (_: Exception) {}
        }
    }

    fun setVirtualizer(context: Context, strength: Float) {
        val clamped = strength.coerceIn(0f, 1000f)
        virtualizerStrength = clamped
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putInt(KEY_VIRTUALIZER, clamped.toInt())
            .apply()

        if (isVirtualizerSupported) {
            try {
                virtualizer?.setStrength(clamped.toInt().toShort())
            } catch (_: Exception) {}
        }
    }

    fun resetToFlat(context: Context) {
        val flatIdx = presets.indexOfFirst { it.equals("Flat", ignoreCase = true) }
        if (flatIdx >= 0) {
            applyPreset(context, flatIdx)
        } else {
            for (i in bands.indices) {
                setBandLevel(context, bands[i].index, 0.toShort())
            }
        }
        setBassBoost(context, 0f)
        setVirtualizer(context, 0f)
    }

    fun release() {
        try {
            equalizer?.release()
        } catch (_: Exception) {}
        equalizer = null

        try {
            bassBoost?.release()
        } catch (_: Exception) {}
        bassBoost = null

        try {
            virtualizer?.release()
        } catch (_: Exception) {}
        virtualizer = null
        currentSessionId = 0
    }
}