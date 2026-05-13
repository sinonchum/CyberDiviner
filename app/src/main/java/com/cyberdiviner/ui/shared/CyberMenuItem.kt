package com.cyberdiviner.ui.shared
import com.cyberdiviner.ui.theme.*
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberWhite
import com.cyberdiviner.ui.theme.TextMuted
import kotlinx.coroutines.flow.collectLatest

/**
 * A left-aligned menu item with Chinese title, English subtitle, and description.
 *
 * Features instant color inversion on press (entire row) and haptic feedback.
 * A geometric accent line is drawn on the left edge via [Canvas] with [StrokeCap.Square].
 * No Material ripple is used.
 *
 * @param title Chinese title displayed large and bold.
 * @param subtitle English subtitle displayed small and muted.
 * @param description Additional description displayed small and muted.
 * @param onClick Callback invoked when the item is tapped.
 * @param modifier Optional [Modifier] applied to the root [Row].
 */
@Composable
fun CyberMenuItem(
    title: String,
    subtitle: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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

    val bgColor = if (isPressed) CyberWhite else CyberBlack
    val titleColor = if (isPressed) CyberBlack else CyberWhite
    val mutedColor = if (isPressed) CyberBlack.copy(alpha = 0.5f) else TextMuted
    val accentColor = if (isPressed) CyberBlack else CyberWhite

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(start = 1.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Geometric accent line on the left edge (1.dp wide, StrokeCap.Square)
        Canvas(
            modifier = Modifier
                .width(1.dp)
                .height(48.dp)
                .padding(top = 2.dp)
        ) {
            drawRect(
                color = accentColor,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Chinese title (large, bold)
            Text(
                text = title,
                color = titleColor,
                fontFamily = HuiwenFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // English subtitle (small, muted)
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = mutedColor,
                    fontFamily = MonoFontFamily,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Description (small, muted)
            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    color = mutedColor,
                    fontFamily = WenKaiFontFamily,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
