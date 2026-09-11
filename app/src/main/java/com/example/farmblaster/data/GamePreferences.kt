package com.example.farmblaster.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages offline persistent storage for high scores, game statistics, and player preferences.
 */
class GamePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var highScore: Int
        get() = prefs.getInt(KEY_HIGH_SCORE, 0)
        set(value) = prefs.edit().putInt(KEY_HIGH_SCORE, value).apply()

    var highestWave: Int
        get() = prefs.getInt(KEY_HIGHEST_WAVE, 1)
        set(value) = prefs.edit().putInt(KEY_HIGHEST_WAVE, value).apply()

    var totalKills: Int
        get() = prefs.getInt(KEY_TOTAL_KILLS, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_KILLS, value).apply()

    var bestCombo: Int
        get() = prefs.getInt(KEY_BEST_COMBO, 0)
        set(value) = prefs.edit().putInt(KEY_BEST_COMBO, value).apply()

    var bossesDefeated: Int
        get() = prefs.getInt(KEY_BOSSES_DEFEATED, 0)
        set(value) = prefs.edit().putInt(KEY_BOSSES_DEFEATED, value).apply()

    var isMusicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_MUSIC_ENABLED, value).apply()

    var isSfxEnabled: Boolean
        get() = prefs.getBoolean(KEY_SFX_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SFX_ENABLED, value).apply()

    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, value).apply()

    var graphicsQuality: GraphicsQuality
        get() {
            val name = prefs.getString(KEY_GRAPHICS_QUALITY, GraphicsQuality.HIGH.name)
            return try {
                GraphicsQuality.valueOf(name ?: GraphicsQuality.HIGH.name)
            } catch (e: Exception) {
                GraphicsQuality.HIGH
            }
        }
        set(value) = prefs.edit().putString(KEY_GRAPHICS_QUALITY, value.name).apply()

    fun updateHighScoreIfNeeded(score: Int): Boolean {
        if (score > highScore) {
            highScore = score
            return true
        }
        return false
    }

    fun updateHighestWaveIfNeeded(wave: Int) {
        if (wave > highestWave) {
            highestWave = wave
        }
    }

    fun updateBestComboIfNeeded(combo: Int) {
        if (combo > bestCombo) {
            bestCombo = combo
        }
    }

    fun incrementKills(count: Int = 1) {
        totalKills += count
    }

    fun incrementBosses() {
        bossesDefeated += 1
    }

    fun resetHighScores() {
        prefs.edit()
            .putInt(KEY_HIGH_SCORE, 0)
            .putInt(KEY_HIGHEST_WAVE, 1)
            .putInt(KEY_TOTAL_KILLS, 0)
            .putInt(KEY_BEST_COMBO, 0)
            .putInt(KEY_BOSSES_DEFEATED, 0)
            .apply()
    }

    enum class GraphicsQuality {
        LOW, MEDIUM, HIGH
    }

    companion object {
        private const val PREFS_NAME = "farm_blaster_prefs"
        private const val KEY_HIGH_SCORE = "high_score"
        private const val KEY_HIGHEST_WAVE = "highest_wave"
        private const val KEY_TOTAL_KILLS = "total_kills"
        private const val KEY_BEST_COMBO = "best_combo"
        private const val KEY_BOSSES_DEFEATED = "bosses_defeated"
        private const val KEY_MUSIC_ENABLED = "music_enabled"
        private const val KEY_SFX_ENABLED = "sfx_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_GRAPHICS_QUALITY = "graphics_quality"
    }
}
