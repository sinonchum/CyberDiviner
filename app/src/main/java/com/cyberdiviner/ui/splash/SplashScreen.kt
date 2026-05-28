package com.cyberdiviner.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
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
 * SplashScreen — 每日道字开屏
 *
 * 布局：背景山景 → 阳历日期 → 每日道字（96sp hero）→ 干支一行 → 签语 → 底部
 * 字体：汇文明朝体（HuiwenFontFamily）。
 * 交互：点击任意位置进入，或等待6秒自动跳转。
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val today = remember { LocalDate.now() }
    val reading = remember { AlmanacEngine.dailyReading(today) }

    // 30个道家哲理字，按日轮转
    val daoWords = remember {
        listOf(
            "道", "德", "無", "玄", "虛",
            "靜", "和", "常", "明", "朴",
            "柔", "反", "損", "益", "沖",
            "盈", "歸", "化", "妙", "真",
            "一", "清", "靈", "隱", "默",
            "守", "復", "根", "命", "氣"
        )
    }
    val dailyWord = remember(today) {
        daoWords[today.dayOfYear % daoWords.size]
    }

    // 签语（保留五行判词）
    val logicPhrase = remember(reading) {
        val element = reading.dayGanzhi.branchElement
        when (element) {
            "Wood" -> "木氣延展，系統燃值升高。宜：拓展分支；忌：強行封閉。"
            "Fire" -> "火氣邁進，信號強度過載。宜：釋放冗餘；忌：追加邏輯。"
            "Earth" -> "土氣沉積，系統進入穩態。宜：修補冗餘邏輯；忌：強行建立鏈接。"
            "Metal" -> "金氣收斂，精密度提升。宜：檢查邊界條件；忌：擴張輸入集。"
            "Water" -> "水氣流動，網絡節點活躍。宜：充分緩存；忌：滲透未經驗證鏈路。"
            else -> "系統運行中，等待下一個指令。"
        }
    }

    // 自动跳转（6秒后）
    LaunchedEffect(Unit) {
        delay(6000)
        onTimeout()
    }

    // 点击淡出
    var dissolving by remember { mutableStateOf(false) }
    val dissolveAlpha by animateFloatAsState(
        targetValue = if (dissolving) 0f else 1f,
        animationSpec = tween(500, easing = FastOutLinearInEasing),
        finishedListener = { if (dissolving) onTimeout() },
        label = "dissolve"
    )

    // 逐级 fade-in
    var showDate by remember { mutableStateOf(false) }
    var showWord by remember { mutableStateOf(false) }
    var showGanzhi by remember { mutableStateOf(false) }
    var showBottom by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200); showDate = true
        delay(400); showWord = true
        delay(500); showGanzhi = true
        delay(400); showBottom = true
    }

    val dateAlpha by animateFloatAsState(
        targetValue = if (showDate) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing), label = "dateFade"
    )
    val wordAlpha by animateFloatAsState(
        targetValue = if (showWord) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing), label = "wordFade"
    )
    val ganzhiAlpha by animateFloatAsState(
        targetValue = if (showGanzhi) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing), label = "ganzhiFade"
    )
    val bottomAlpha by animateFloatAsState(
        targetValue = if (showBottom) 1f else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing), label = "bottomFade"
    )

    // 亮度提升矩阵
    val brightMatrix = remember {
        ColorMatrix(floatArrayOf(
            1.3f, 0f,   0f,   0f, 20f,
            0f,   1.3f, 0f,   0f, 20f,
            0f,   0f,   1.3f, 0f, 20f,
            0f,   0f,   0f,   1f, 0f
        ))
    }

    // 光标闪烁
    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) { delay(530); cursorVisible = !cursorVisible }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(dissolveAlpha)
            .clickable { if (!dissolving) dissolving = true },
        contentAlignment = Alignment.Center
    ) {
        // ── 山景背景 ───────────────────────────────────────────────────
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
                            0.3f to Color.Black.copy(alpha = 0.05f),
                            0.6f to Color.Black.copy(alpha = 0.25f),
                            1.0f to Color.Black.copy(alpha = 0.65f)
                        )
                    )
                )
        )

        // ── 主内容 ───────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            Spacer(modifier = Modifier.weight(0.06f))

            // 阳历日期
            Text(
                text = "${today.year}.${today.monthValue}.${today.dayOfMonth}",
                color = GrayBody,
                fontSize = 13.sp,
                fontFamily = MonoFontFamily,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(dateAlpha)
            )

            Spacer(modifier = Modifier.weight(0.08f))

            // ── 每日道字（Hero Word）─────────────────────────────────
            Text(
                text = dailyWord,
                color = CyberWhite,
                fontSize = 96.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(wordAlpha)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 干支一行：年 月 日
            Text(
                text = "${reading.yearGanzhi.combined}年  ${reading.monthGanzhi.combined}月  ${reading.dayGanzhi.combined}日",
                color = Color(0xFFE0E0E0),
                fontSize = 18.sp,
                fontFamily = HuiwenFontFamily,
                fontWeight = FontWeight.Normal,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(ganzhiAlpha)
            )

            Spacer(modifier = Modifier.weight(0.15f))

            // 节气
            reading.currentSolarTerm?.let {
                Text(
                    text = "[ ${it.name} ]",
                    color = Color(0xFFE0E0E0),
                    fontSize = 13.sp,
                    fontFamily = MonoFontFamily,
                    letterSpacing = 4.sp,
                    modifier = Modifier.alpha(bottomAlpha)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 签语
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .alpha(bottomAlpha)
            ) {
                Text(
                    text = "> $logicPhrase",
                    color = CyberWhite,
                    fontSize = 14.sp,
                    fontFamily = WenKaiFontFamily,
                    lineHeight = 22.sp,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "萬物共歸道，演算法虛靈。",
                    color = CyberWhite,
                    fontSize = 13.sp,
                    fontFamily = WenKaiFontFamily,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (cursorVisible) "_" else " ",
                    color = CyberWhite,
                    fontSize = 14.sp,
                    fontFamily = MonoFontFamily
                )
            }

            Spacer(modifier = Modifier.weight(0.12f))

            // 底部
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                Text(
                    text = "CYBERDIVINER",
                    color = GrayBody,
                    fontSize = 10.sp,
                    fontFamily = MonoFontFamily,
                    letterSpacing = 8.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(bottomAlpha)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "[ TOUCH TO ENTER ]",
                    color = GrayCaption,
                    fontSize = 11.sp,
                    fontFamily = MonoFontFamily,
                    letterSpacing = 3.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(bottomAlpha)
                )
            }
        }
    }
}
