package com.cyberdiviner.ui.shared
import com.cyberdiviner.ui.theme.*
import android.content.Context
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay

/**
 * TypewriterText — A Composable that displays text one character at a time
 * with a blinking cursor, evoking a CRT terminal aesthetic.
 *
 * Fires a single [HapticUtils.LIGHT] haptic at the start of typing
 * (not per-character) and invokes [onComplete] when all characters are shown.
 *
 * @param text The full string to reveal.
 * @param modifier Layout modifier.
 * @param charDelayMs Milliseconds between each character reveal.
 * @param onComplete Called once the entire text has been typed out.
 * @param style Text style — defaults to monospace.
 */
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    charDelayMs: Long = 30,
    onComplete: (() -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    val context = LocalContext.current

    // ── Typed-text state ────────────────────────────────────────────────────
    var displayedText by remember(text) { mutableStateOf("") }

    // ── Typewriter animation ────────────────────────────────────────────────
    LaunchedEffect(text) {
        displayedText = ""
        triggerLightHaptic(context)

        for (char in text) {
            displayedText += char
            delay(charDelayMs)
        }

        onComplete?.invoke()
    }

    // ── Blinking cursor ─────────────────────────────────────────────────────
    var cursorVisible by remember(text) { mutableStateOf(true) }

    LaunchedEffect(text) {
        // Keep cursor visible during typing, then let it blink after completion
        delay(text.length * charDelayMs)
        while (true) {
            cursorVisible = !cursorVisible
            delay(500)
        }
    }

    // ── Render ──────────────────────────────────────────────────────────────
    val isTyping = displayedText.length < text.length
    val cursor = if (isTyping || cursorVisible) "_" else ""

    Text(
        text = displayedText + cursor,
        modifier = modifier,
        style = style,
        fontFamily = MonoFontFamily,
        maxLines = Int.MAX_VALUE,
        overflow = TextOverflow.Clip
    )
}

/**
 * Trigger a single subtle LIGHT haptic pulse — called once at the start
 * of the typewriter animation to ground the user tactilely without
 * spamming vibration every character.
 */
private fun triggerLightHaptic(context: Context) {
    try {
        HapticUtils.vibrate(context, HapticUtils.HapticType.LIGHT)
    } catch (_: Exception) {
        // Graceful degradation — vibration is cosmetic
    }
}
