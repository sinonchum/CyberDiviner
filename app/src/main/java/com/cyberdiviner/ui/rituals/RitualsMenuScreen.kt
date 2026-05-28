package com.cyberdiviner.ui.rituals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cyberdiviner.ui.shared.CyberMenuItem
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.shared.SectionHeader
import com.cyberdiviner.ui.shared.StaggeredItem

/**
 * RitualsMenuScreen -- Tab for ritual selection (bottom nav destination).
 *
 * Four options: 周易六爻, 赛博塔罗, 视界摸骨, 电子木鱼.
 * Left-aligned. No back button — navigation handled by bottom bar.
 */
@Composable
fun RitualsMenuScreen(
    onIChing: () -> Unit,
    onTarot: () -> Unit,
    onVision: () -> Unit,
    onMuyu: () -> Unit,
    onAlmanac: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(start = 32.dp, top = 0.dp, end = 32.dp, bottom = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            SectionHeader(title = "术数推演", subtitle = "RITUAL EXECUTION")
            Spacer(modifier = Modifier.height(48.dp))

            StaggeredItem(index = 0) {
                CyberMenuItem(
                    title = "周易六爻",
                    subtitle = "I-CHING",
                    description = "摇钱起卦，六爻断事",
                    onClick = onIChing
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            StaggeredItem(index = 1) {
                CyberMenuItem(
                    title = "赛博塔罗",
                    subtitle = "CYBER TAROT",
                    description = "七十八牌，阵法推演",
                    onClick = onTarot
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            StaggeredItem(index = 2) {
                CyberMenuItem(
                    title = "视界摸骨",
                    subtitle = "FACE SCAN",
                    description = "MediaPipe 面部特征分析",
                    onClick = onVision
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            StaggeredItem(index = 3) {
                CyberMenuItem(
                    title = "电子木鱼",
                    subtitle = "WOODEN FISH",
                    description = "敲击积功德",
                    onClick = onMuyu
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            StaggeredItem(index = 4) {
                CyberMenuItem(
                    title = "赛博黄历",
                    subtitle = "ALMANAC",
                    description = "干支黄历，每日宜忌",
                    onClick = onAlmanac
                )
            }
        }
    }
}
