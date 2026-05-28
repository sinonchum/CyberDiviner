package com.cyberdiviner.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.R
import com.cyberdiviner.engine.AlmanacEngine
import com.cyberdiviner.ui.theme.*
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * SplashScreen -- 山景背景 + 干支大字。3.5秒后自动跳转。
 *
 * 背景：原图 + 亮度提升矩阵，无灰度滤镜。
 * 字体：汇文明朝体（HuiwenFontFamily）。
 * 文字：繁体中文。
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val today = remember { LocalDate.now() }
    val reading = remember { AlmanacEngine.dailyReading(today) }
    val solarFormatter = remember { DateTimeFormatter.ofPattern("yyyy.MM.dd") }

    LaunchedEffect(Unit) {
        delay(3500)
        onTimeout()
    }

    // ── Sequential fade-in ────────────────────────────────────────────────
    var showDate by remember { mutableStateOf(false) }
    var showYear by remember { mutableStateOf(false) }
    var showMonth by remember { mutableStateOf(false) }
    var showDay by remember { mutableStateOf(false) }
    var showBottom by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300); showDate = true
        delay(400); showYear = true
        delay(250); showMonth = true
        delay(250); showDay = true
        delay(500); showBottom = true
    }

    val dateAlpha by animateFloatAsState(
        targetValue = if (showDate) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "dateFade"
    )
    val yearAlpha by animateFloatAsState(
        targetValue = if (showYear) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "yearFade"
    )
    val monthAlpha by animateFloatAsState(
        targetValue = if (showMonth) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "monthFade"
    )
    val dayAlpha by animateFloatAsState(
        targetValue = if (showDay) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "dayFade"
    )
    val bottomAlpha by animateFloatAsState(
        targetValue = if (showBottom) 1f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "bottomFade"
    )

    // 亮度提升矩阵：每通道 ×2.0，偏移+50
    val brightMatrix = remember {
        ColorMatrix(floatArrayOf(
            1.2f, 0f,   0f,   0f, 15f,
            0f,   1.2f, 0f,   0f, 15f,
            0f,   0f,   1.2f, 0f, 15f,
            0f,   0f,   0f,   1f, 0f
        ))
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // ── 山景背景 — 程序化提亮 ───────────────────────────────────────
        Image(
            painter = painterResource(id = R.drawable.splash_mountain),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(brightMatrix),
            modifier = Modifier.fillMaxSize()
        )

        // 底部渐变遮罩（仅下半部分加深，保证文字可读）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.0f),
                            0.3f to Color.Black.copy(alpha = 0.05f),
                            0.6f to Color.Black.copy(alpha = 0.25f),
                            1.0f to Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        // ── 主内容 ───────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            Spacer(modifier = Modifier.weight(0.12f))

            // 阳历日期
            Text(
                text = today.format(solarFormatter),
                color = GrayCaption,
                fontSize = 12.sp,
                fontFamily = MonoFontFamily,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(dateAlpha)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 干支年 — 汇文明朝体
            Text(
                text = "${reading.yearGanzhi.stem}${reading.yearGanzhi.branch}年",
                color = CyberWhite,
                fontSize = 44.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(yearAlpha)
            )

            Spacer(modifier = Modifier.height(6.dp))
            AccentDivider(modifier = Modifier.alpha(yearAlpha))
            Spacer(modifier = Modifier.height(18.dp))

            // 干支月
            Text(
                text = "${reading.monthGanzhi.stem}${reading.monthGanzhi.branch}月",
                color = CyberWhite,
                fontSize = 44.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(monthAlpha)
            )

            Spacer(modifier = Modifier.height(6.dp))
            AccentDivider(modifier = Modifier.alpha(monthAlpha))
            Spacer(modifier = Modifier.height(18.dp))

            // 干支日 — 最大
            Text(
                text = "${reading.dayGanzhi.stem}${reading.dayGanzhi.branch}日",
                color = CyberWhite,
                fontSize = 60.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(dayAlpha)
            )

            Spacer(modifier = Modifier.weight(0.35f))
        }

        // ── 底部 ──────────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .width(48.dp)
                    .height(1.dp)
                    .alpha(bottomAlpha)
            ) {
                drawLine(
                    AccentRed,
                    Offset(0f, size.height / 2f),
                    Offset(size.width, size.height / 2f),
                    1.5f,
                    StrokeCap.Square
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 签语 — 繁体
            Text(
                text = "萬物共歸道，演算法虛靈。",
                color = GrayCaption,
                fontSize = 14.sp,
                fontFamily = WenKaiFontFamily,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(bottomAlpha)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "CYBERDIVINER",
                color = GrayMuted,
                fontSize = 10.sp,
                fontFamily = MonoFontFamily,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(bottomAlpha)
            )
        }
    }
}

@Composable
private fun AccentDivider(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .width(64.dp)
            .height(1.dp)
    ) {
        drawLine(
            AccentRed,
            Offset(0f, size.height / 2f),
            Offset(size.width, size.height / 2f),
            1.5f,
            StrokeCap.Square
        )
    }
}
