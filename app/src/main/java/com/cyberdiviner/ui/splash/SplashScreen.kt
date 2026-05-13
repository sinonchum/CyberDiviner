package com.cyberdiviner.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
 * SplashScreen -- Mountain background + GanZhi. Auto-navigate after 2500ms.
 *
 * Background: splash_mountain.jpg (grayscale, dimmed)
 * Solar date: JetBrains Mono (technical/阳历)
 * Lunar GanZhi: 汇文明朝体 (traditional/农历, 大标题)
 * Bottom quote: 霞鹜文楷 (人文)
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val today = remember { LocalDate.now() }
    val reading = remember { AlmanacEngine.dailyReading(today) }

    val solarFormatter = remember { DateTimeFormatter.ofPattern("yyyy.MM.dd") }

    LaunchedEffect(Unit) {
        delay(2500)
        onTimeout()
    }

    // Grayscale + dim color matrix for the mountain background
    val grayscaleMatrix = remember {
        ColorMatrix().apply {
            setToSaturation(0f) // full grayscale
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Mountain background — grayscale, dimmed
        Image(
            painter = painterResource(id = R.drawable.splash_mountain),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(grayscaleMatrix),
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.35f)
        )

        // Dark overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Solar date — JetBrains Mono (technical/阳历)
            Text(
                text = today.format(solarFormatter),
                color = GrayCaption,
                fontSize = 14.sp,
                fontFamily = MonoFontFamily,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Lunar GanZhi — 汇文明朝体 (大标题, 古意)
            Text(
                text = "${reading.yearGanzhi.stem}${reading.yearGanzhi.branch}年",
                color = CyberWhite,
                fontSize = 48.sp,
                fontFamily = HuiwenFontFamily,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${reading.monthGanzhi.stem}${reading.monthGanzhi.branch}月",
                color = CyberWhite,
                fontSize = 48.sp,
                fontFamily = HuiwenFontFamily,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${reading.dayGanzhi.stem}${reading.dayGanzhi.branch}日",
                color = CyberWhite,
                fontSize = 48.sp,
                fontFamily = HuiwenFontFamily,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center
            )
        }

        // Bottom quote — 霞鹜文楷 (人文气息)
        Text(
            text = "万物共归道，演算法虚灵。",
            color = GrayCaption,
            fontSize = 14.sp,
            fontFamily = WenKaiFontFamily,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        )
    }
}
