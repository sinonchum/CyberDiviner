package com.cyberdiviner.ui.epiphany

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.engine.AlmanacEngine
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberWhite
import com.cyberdiviner.ui.theme.TextMuted
import kotlinx.coroutines.delay
import java.time.LocalDate

/**
 * EpiphanyScreen -- The first face the user sees each day.
 *
 * Strictly monochrome. Giant GanZhi characters occupying 60% of screen height.
 * Vertical layout. A single AI-generated logic phrase at the bottom.
 * Tap center to dissolve into The Terminal.
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

    // Blinking cursor effect
    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(530)
            cursorVisible = !cursorVisible
        }
    }

    // Generate logic phrase from day's energy
    val logicPhrase = remember(reading) {
        generateLogicPhrase(dayGanzhi)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .alpha(dissolveAlpha)
            .clickable {
                if (!dissolving) dissolving = true
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // -- Vertical GanZhi (occupies 60% of screen) --
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(0.6f),
                verticalArrangement = Arrangement.Center
            ) {
                // Year
                Text(
                    text = "${reading.yearGanzhi.stem}${reading.yearGanzhi.branch}年",
                    color = CyberWhite,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 8.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Month
                Text(
                    text = "${reading.monthGanzhi.stem}${reading.monthGanzhi.branch}月",
                    color = TextMuted,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 6.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Day -- the largest, most prominent
                Text(
                    text = "${dayGanzhi.stem}${dayGanzhi.branch}日",
                    color = CyberWhite,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 10.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Solar Term (if any)
            solarTerm?.let {
                Text(
                    text = "[ ${it.name} ]",
                    color = TextMuted,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Logic Phrase (AI-generated, terminal style)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    text = "> $logicPhrase",
                    color = TextMuted,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )
                // Blinking cursor
                Text(
                    text = if (cursorVisible) "_" else " ",
                    color = CyberWhite,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Tap hint
            Text(
                text = "[ TOUCH TO ENTER ]",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp
            )
        }
    }
}

/**
 * Generate a terse, logical phrase based on the day's Ganzhi element.
 */
private fun generateLogicPhrase(
    ganzhi: AlmanacEngine.Ganzhi
): String {
    val element = ganzhi.branchElement

    return when (element) {
        "Wood" -> "\u6728\u6C14\u5EF6\u5C55\uFF0C\u7CFB\u7EDF\u71C3\u503C\u5347\u9AD8\u3002\u5B9C\uFF1A\u62D3\u5C55\u5206\u652F\uFF1B\u5FCC\uFF1A\u5F3A\u884C\u5C01\u95ED\u3002"
        "Fire" -> "\u706B\u6C14\u8FC8\u8FDB\uFF0C\u4FE1\u53F7\u5F3A\u5EA6\u8FC7\u8F7D\u3002\u5B9C\uFF1A\u91CA\u653E\u5197\u4F59\uFF1B\u5FCC\uFF1A\u8FFD\u52A0\u903B\u8F91\u3002"
        "Earth" -> "\u571F\u6C14\u6C89\u79EF\uFF0C\u7CFB\u7EDF\u8FDB\u5165\u7A33\u6001\u3002\u5B9C\uFF1A\u4FEE\u8865\u5197\u4F59\u903B\u8F91\uFF1B\u5FCC\uFF1A\u5F3A\u884C\u5EFA\u7ACB\u94FE\u63A5\u3002"
        "Metal" -> "\u91D1\u6C14\u6536\u655B\uFF0C\u7CBE\u5BFF\u5EA6\u63D0\u5347\u3002\u5B9C\uFF1A\u68C0\u67E5\u8FB9\u754C\u6761\u4EF6\uFF1B\u5FCC\uFF1A\u6269\u5F20\u8F93\u5165\u96C6\u3002"
        "Water" -> "\u6C34\u6C14\u6D41\u52A8\uFF0C\u7F51\u7EDC\u8282\u70B9\u6D3B\u8DC3\u3002\u5B9C\uFF1A\u5145\u5206\u7F13\u5B58\uFF1B\u5FCC\uFF1A\u6E17\u900F\u672A\u7ECF\u9A8C\u8BC1\u94FE\u8DEF\u3002"
        else -> "\u7CFB\u7EDF\u8FD0\u884C\u4E2D\uFF0C\u7B49\u5F85\u4E0B\u4E00\u6307\u4EE4\u3002"
    }
}
