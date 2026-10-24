package com.example.data

import android.content.Context
import android.content.SharedPreferences

class ScorePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("block_puzzle_prefs", Context.MODE_PRIVATE)

    fun getBestScore(): Int {
        return prefs.getInt(KEY_BEST_SCORE, 0)
    }

    fun saveBestScore(score: Int) {
        val current = getBestScore()
        if (score > current) {
            prefs.edit().putInt(KEY_BEST_SCORE, score).apply()
        }
    }

    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun isVibrationEnabled(): Boolean {
        return prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }

    companion object {
        private const val KEY_BEST_SCORE = "key_best_score"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "key_vibration_enabled"
    }
}
