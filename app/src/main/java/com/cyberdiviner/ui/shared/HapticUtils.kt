package com.cyberdiviner.ui.shared

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberWhite

/**
 * Utility object for haptic feedback via the Android Vibrator API.
 *
 * Provides [vibrate] with predefined [HapticType] presets for different
 * interaction intensities (light tap, heavy thud, success confirmation, etc.).
 */
object HapticUtils {
    /** Preset haptic vibration patterns. */
    enum class HapticType { LIGHT, MEDIUM, HEAVY, SUCCESS, WARNING, COIN_LAND, CARD_FLIP }

    /**
     * Triggers a vibration of the given [type] on the default vibrator.
     *
     * @param context Application or Activity context used to access the vibrator service.
     * @param type The [HapticType] preset determining duration and amplitude.
     */
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

/**
 * A Compose [Modifier] extension that adds CyberDiviner-style click behavior:
 * no Material ripple, instant visual color inversion on press, and haptic feedback.
 *
 * Usage:
 * ```
 * Box(
 *     modifier = Modifier
 *         .fillMaxWidth()
 *         .cyberClick { handleClick() }
 * )
 * ```
 *
 * The modifier:
 * - Uses [detectTapGestures] with an `onPress` callback for instant feedback.
 * - Triggers [HapticFeedbackConstants.TEXT_HANDLE_MOVE] on press.
 * - Inverts the composable's background: CyberBlack ↔ CyberWhite.
 * - Invokes [onClick] on successful tap (release inside bounds).
 *
 * @param onClick Callback invoked when the tap is completed (released inside bounds).
 * @return A [Modifier] with the cyber click behavior applied.
 */
fun Modifier.cyberClick(onClick: () -> Unit): Modifier = composed {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }
    val bgColor = if (isPressed) CyberWhite else CyberBlack

    this
        .background(bgColor)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    view.performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
                    tryAwaitRelease()
                    isPressed = false
                },
                onTap = {
                    onClick()
                }
            )
        }
}
