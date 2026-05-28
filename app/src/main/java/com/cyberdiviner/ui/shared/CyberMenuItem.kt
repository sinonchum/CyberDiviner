package com.cyberdiviner.ui.shared
import com.cyberdiviner.ui.theme.*
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberWhite
import com.cyberdiviner.ui.theme.GrayMuted
import kotlinx.coroutines.flow.collectLatest

/**
 * CyberMenuItem — 左对齐菜单项，点击时左侧竖线变红 + 标题变红。
 *
 * 不再整块反色，改为更克制的交互反馈：
 * - 左侧 1dp 竖线：按下时 AccentRed，松开恢复 CyberWhite
 * - 标题文字：按下时 AccentRed，松开恢复 CyberWhite
 * - 震动反馈保持
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

    // 克制的色彩变化：不再整块反色
    val accentColor = if (isPressed) AccentRed else CyberWhite
    val titleColor = if (isPressed) AccentRed else CyberWhite

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CyberBlack)
            .semantics { contentDescription = title }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(start = 1.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // 左侧竖线 — 按下时变红
        Canvas(
            modifier = Modifier
                .width(2.dp)
                .height(48.dp)
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
            // 标题 — 按下时变红
            Text(
                text = title,
                color = titleColor,
                fontFamily = HuiwenFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 2.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // 英文副标题
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = GrayMuted,
                    fontFamily = MonoFontFamily,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 描述
            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    color = GrayMuted,
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
