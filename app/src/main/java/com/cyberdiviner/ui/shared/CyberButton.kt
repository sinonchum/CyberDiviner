package com.cyberdiviner.ui.shared
import com.cyberdiviner.ui.theme.*
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberGray
import com.cyberdiviner.ui.theme.CyberWhite
import com.cyberdiviner.ui.theme.TextMuted
import kotlinx.coroutines.flow.collectLatest

/**
 * A CyberDiviner button with instant color inversion on press and haptic feedback.
 *
 * No Material ripple effect is used. Instead, the button detects press/release via
 * [MutableInteractionSource] and inverts its background and text colors instantly.
 * A [HapticFeedbackConstants.TEXT_HANDLE_MOVE] haptic is triggered on press.
 *
 * @param text The button label text.
 * @param onClick Callback invoked when the button is tapped (released after press).
 * @param modifier Optional [Modifier] applied to the root [Box].
 * @param enabled Whether the button is interactive. Disabled buttons show muted styling.
 */
@Composable
fun CyberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val view = LocalView.current

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isPressed = true
                    view.performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
                }
                is PressInteraction.Release,
                is PressInteraction.Cancel -> {
                    isPressed = false
                }
            }
        }
    }

    val bgColor = when {
        !enabled -> CyberGray
        isPressed -> CyberWhite
        else -> CyberBlack
    }

    val textColor = when {
        !enabled -> TextMuted
        isPressed -> CyberBlack
        else -> CyberWhite
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontFamily = HuiwenFontFamily,
            fontSize = 14.sp,
            letterSpacing = 2.sp
        )
    }
}
