package com.cyberdiviner.ui.epiphany

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

/**
 * EpiphanyScreen -- 每日干支顿悟页面
 *
 * 背景：山景图 + 亮度提升。
 * 字体：汇文明朝体（HuiwenFontFamily）。
 * 文字：繁体中文。
 * 交互：点击任意位置淡出进入主界面。
 */
@Composable
fun EpiphanyScreen(
    onEnter: () -> Unit
) {
    val today = remember { LocalDate.now() }
    val reading = remember { AlmanacEngine.dailyReading(today) }
    val dayGanzhi = reading.dayGanzhi
    val solarTerm = reading.currentSolarTerm

    // Dissolve animation
    var dissolving by remember { mutableStateOf(false) }
    val dissolveAlpha by animateFloatAsState(
        targetValue = if (dissolving) 0f else 1f,
        animationSpec = tween(durationMillis = 600, easing = FastOutLinearInEasing),
        finishedListener = { if (dissolving) onEnter() },
        label = "dissolve"
    )

    // Blinking cursor
    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(530)
            cursorVisible = !cursorVisible
        }
    }

    // Sequential fade-in
    var showYear by remember { mutableStateOf(false) }
    var showMonth by remember { mutableStateOf(false) }
    var showDay by remember { mutableStateOf(false) }
    var showBottom by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200); showYear = true
        delay(300); showMonth = true
        delay(300); showDay = true
        delay(500); showBottom = true
    }

    val yearAlpha by animateFloatAsState(
        targetValue = if (showYear) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing), label = "yearFade"
    )
    val monthAlpha by animateFloatAsState(
        targetValue = if (showMonth) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing), label = "monthFade"
    )
    val dayAlpha by animateFloatAsState(
        targetValue = if (showDay) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing), label = "dayFade"
    )
    val bottomAlpha by animateFloatAsState(
        targetValue = if (showBottom) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing), label = "bottomFade"
    )

    // 亮度提升矩阵
    val brightMatrix = remember {
        ColorMatrix(floatArrayOf(
            2.0f, 0f,   0f,   0f, 50f,
            0f,   2.0f, 0f,   0f, 50f,
            0f,   0f,   2.0f, 0f, 50f,
            0f,   0f,   0f,   1f, 0f
        ))
    }

    // 签语
    val logicPhrase = remember(reading) {
        generateLogicPhrase(dayGanzhi)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(dissolveAlpha)
            .clickable {
                if (!dissolving) dissolving = true
            },
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

        // 底部渐变遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.0f),
                            0.3f to Color.Black.copy(alpha = 0.1f),
                            0.6f to Color.Black.copy(alpha = 0.35f),
                            1.0f to Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )

        // ── 主内容 ───────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // 干支区域 (60%)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(0.55f),
                verticalArrangement = Arrangement.Center
            ) {
                // 年 — 汇文明朝体
                Text(
                    text = "${reading.yearGanzhi.stem}${reading.yearGanzhi.branch}年",
                    color = CyberWhite,
                    fontSize = 42.sp,
                    fontFamily = HuiwenFontFamily,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(yearAlpha)
                )

                Spacer(modifier = Modifier.height(8.dp))
                AccentDivider(modifier = Modifier.alpha(yearAlpha))
                Spacer(modifier = Modifier.height(18.dp))

                // 月
                Text(
                    text = "${reading.monthGanzhi.stem}${reading.monthGanzhi.branch}月",
                    color = CyberWhite,
                    fontSize = 42.sp,
                    fontFamily = HuiwenFontFamily,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(monthAlpha)
                )

                Spacer(modifier = Modifier.height(8.dp))
                AccentDivider(modifier = Modifier.alpha(monthAlpha))
                Spacer(modifier = Modifier.height(18.dp))

                // 日 — 最大
                Text(
                    text = "${dayGanzhi.stem}${dayGanzhi.branch}日",
                    color = CyberWhite,
                    fontSize = 58.sp,
                    fontFamily = HuiwenFontFamily,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(dayAlpha)
                )
            }

            // 节气
            solarTerm?.let {
                Text(
                    text = "[ ${it.name} ]",
                    color = GrayMuted,
                    fontSize = 13.sp,
                    fontFamily = MonoFontFamily,
                    letterSpacing = 4.sp,
                    modifier = Modifier.alpha(bottomAlpha)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 签语 — 终端风格
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .alpha(bottomAlpha)
            ) {
                Text(
                    text = "> $logicPhrase",
                    color = GrayCaption,
                    fontSize = 14.sp,
                    fontFamily = WenKaiFontFamily,
                    lineHeight = 22.sp,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (cursorVisible) "_" else " ",
                    color = CyberWhite,
                    fontSize = 14.sp,
                    fontFamily = MonoFontFamily
                )
            }

            Spacer(modifier = Modifier.weight(0.15f))

            // 底部提示
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 56.dp)
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
                Text(
                    text = "[ TOUCH TO ENTER ]",
                    color = GrayMuted,
                    fontSize = 11.sp,
                    fontFamily = MonoFontFamily,
                    letterSpacing = 3.sp,
                    modifier = Modifier.alpha(bottomAlpha)
                )
            }
        }
    }
}

@Composable
private fun AccentDivider(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.width(64.dp).height(1.dp)
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

/**
 * 根据日干支五行生成繁体签语。
 */
private fun generateLogicPhrase(
    ganzhi: AlmanacEngine.Ganzhi
): String {
    val element = ganzhi.branchElement
    return when (element) {
        "Wood" -> "木氣延展，系統燃值升高。宜：拓展分支；忌：強行封閉。"
        "Fire" -> "火氣邁進，信號強度過載。宜：釋放冗餘；忌：追加邏輯。"
        "Earth" -> "土氣沉積，系統進入穩態。宜：修補冗餘邏輯；忌：強行建立鏈接。"
        "Metal" -> "金氣收斂，精密度提升。宜：檢查邊界條件；忌：擴張輸入集。"
        "Water" -> "水氣流動，網絡節點活躍。宜：充分緩存；忌：滲透未經驗證鏈路。"
        else -> "系統運行中，等待下一個指令。"
    }
}
