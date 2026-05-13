package com.cyberdiviner.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.engine.AlmanacEngine
import com.cyberdiviner.ui.theme.*
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * SplashScreen -- Full-screen black. Solar + Lunar date. Auto-navigate after 2500ms.
 *
 * Solar date in monospace (technical). Lunar/GanZhi in serif (汇文明朝体, traditional).
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Solar date (monospace, gray, small — technical/阳历)
            Text(
                text = today.format(solarFormatter),
                color = GrayCaption,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Lunar GanZhi date (serif/汇文明朝体, large — traditional/农历)
            Text(
                text = "${reading.yearGanzhi.stem}${reading.yearGanzhi.branch}年",
                color = CyberWhite,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${reading.monthGanzhi.stem}${reading.monthGanzhi.branch}月",
                color = CyberWhite,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${reading.dayGanzhi.stem}${reading.dayGanzhi.branch}日",
                color = CyberWhite,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center
            )
        }

        // Bottom quote (serif)
        Text(
            text = "万物皆数，代码即宿命。",
            color = GrayCaption,
            fontSize = 14.sp,
            fontFamily = FontFamily.Serif,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        )
    }
}
