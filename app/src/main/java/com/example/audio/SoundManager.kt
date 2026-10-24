package com.example.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoundManager(context: Context) {
    private val appContext = context.applicationContext

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 65)
    } catch (_: Exception) {
        null
    }

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    fun playPlaceSound(soundEnabled: Boolean, vibrateEnabled: Boolean) {
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
            } catch (_: Exception) { }
        }
        if (vibrateEnabled) {
            vibrate(18)
        }
    }

    fun playLineClearSound(soundEnabled: Boolean, vibrateEnabled: Boolean, linesCount: Int) {
        if (soundEnabled) {
            scope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 60)
                    delay(70)
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 80)
                } catch (_: Exception) { }
            }
        }
        if (vibrateEnabled) {
            vibrate(45)
        }
    }

    fun playComboSound(soundEnabled: Boolean, vibrateEnabled: Boolean, comboStreak: Int) {
        if (soundEnabled) {
            scope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 60)
                    delay(70)
                    toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 70)
                    delay(80)
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 100)
                } catch (_: Exception) { }
            }
        }
        if (vibrateEnabled) {
            scope.launch {
                vibrate(30)
                delay(60)
                vibrate(55)
            }
        }
    }

    fun playGameOverSound(soundEnabled: Boolean, vibrateEnabled: Boolean) {
        if (soundEnabled) {
            scope.launch {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 180)
                } catch (_: Exception) { }
            }
        }
        if (vibrateEnabled) {
            vibrate(80)
        }
    }

    private fun vibrate(durationMs: Long) {
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(durationMs)
                    }
                }
            }
        } catch (_: Exception) { }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) { }
    }
}
