package com.cyberdiviner.ui.shared

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.theme.AccentRed
import com.cyberdiviner.ui.theme.GrayCaption
import com.cyberdiviner.ui.theme.HuiwenFontFamily
import com.cyberdiviner.ui.theme.MonoFontFamily
import com.cyberdiviner.ui.theme.GrayMuted
import com.cyberdiviner.ui.localization.LocalAppLanguage
import com.cyberdiviner.ui.settings.AppLanguage

object DesignTokens {
    val ScreenHorizontalPadding = 32.dp
    val CardPadding = 24.dp
    val ItemSpacing = 12.dp
    val SectionSpacing = 48.dp
    val HeaderBottomMargin = 24.dp
}

/**
 * Bridgewater-style section header.
 * Chinese title in HuiwenFontFamily + red underline + English subtitle in MonoFontFamily.
 */
@Composable
fun SectionHeader(
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val lang = LocalAppLanguage.current
        Text(
            text = title,
            color = GrayCaption,
            fontFamily = if (lang == AppLanguage.BILINGUAL_EN) MonoFontFamily else HuiwenFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )
        Canvas(
            modifier = Modifier
                .width(120.dp)
                .padding(top = 4.dp)
                .height(2.dp)
        ) {
            drawRect(
                color = AccentRed,
                topLeft = Offset.Zero,
                size = Size(size.width, size.height)
            )
        }
        if (subtitle.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = GrayCaption,
                fontSize = 11.sp,
                fontFamily = if (lang == AppLanguage.BILINGUAL_EN) HuiwenFontFamily else MonoFontFamily,
                letterSpacing = 2.sp
            )
        }
    }
}

/**
 * Horizontal divider line — 1dp AccentRed or GrayBorder.
 */
@Composable
fun DividerLine(
    color: Color = AccentRed,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawRect(
            color = color,
            topLeft = Offset.Zero,
            size = Size(size.width, size.height)
        )
    }
}

/**
 * JetBrains Mono status line — small, muted, letter-spaced.
 */
@Composable
fun StatusLine(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = GrayMuted,
        fontSize = 10.sp,
        fontFamily = MonoFontFamily,
        letterSpacing = 2.sp,
        modifier = modifier
    )
}

/**
 * Staggered fade-in animation for menu items.
 * Each item fades in with a delay based on its index.
 */
@Composable
fun StaggeredItem(
    index: Int,
    baseDelayMs: Long = 80,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = (index * baseDelayMs).toInt(),
            easing = FastOutSlowInEasing
        ),
        label = "stagger_$index"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 20f,
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = (index * baseDelayMs).toInt(),
            easing = FastOutSlowInEasing
        ),
        label = "stagger_offset_$index"
    )
    LaunchedEffect(Unit) { visible = true }
    Box(
        modifier = Modifier
            .graphicsLayer {
                this.alpha = alpha
                translationY = offsetY
            }
    ) {
        content()
    }
}

@Composable
fun BackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = "< 返回",
        color = GrayCaption,
        fontSize = 13.sp,
        fontFamily = HuiwenFontFamily,
        modifier = modifier
            .clickable { onBack() }
            .padding(4.dp)
    )
}
