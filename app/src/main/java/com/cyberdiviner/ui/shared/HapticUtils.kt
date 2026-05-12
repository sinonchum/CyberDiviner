package com.cyberdiviner.ui.shared

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticUtils {
    enum class HapticType { LIGHT, MEDIUM, HEAVY, SUCCESS, WARNING, COIN_LAND, CARD_FLIP }

    fun vibrate(context: Context, type: HapticType) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val effect = when (type) {
            HapticType.LIGHT -> VibrationEffect.createOneShot(30, 40)
            HapticType.MEDIUM -> VibrationEffect.createOneShot(50, 80)
            HapticType.HEAVY -> VibrationEffect.createOneShot(100, 200)
            HapticType.SUCCESS -> VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
            HapticType.WARNING -> VibrationEffect.createWaveform(longArrayOf(0, 50, 30, 50), -1)
            HapticType.COIN_LAND -> VibrationEffect.createOneShot(80, 120)
            HapticType.CARD_FLIP -> VibrationEffect.createOneShot(20, 30)
        }
        vibrator.vibrate(effect)
    }
}
