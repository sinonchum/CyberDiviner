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
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberWhite
import kotlinx.coroutines.delay
import java.time.LocalDate

/**
 * SplashScreen -- Full-screen black. Centered GanZhi. Auto-navigate after 2500ms.
 *
 * No tap interaction. No decorations. Just the date and a single line at the bottom.
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val today = remember { LocalDate.now() }
    val reading = remember { AlmanacEngine.dailyReading(today) }

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
            // Center: GanZhi date (48sp, 汇文明朝体)
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

        // Bottom quote (14sp, 汇文明朝体)
        Text(
            text = "万物皆数，代码即宿命。",
            color = CyberWhite,
            fontSize = 14.sp,
            fontFamily = FontFamily.Serif,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        )
    }
}
