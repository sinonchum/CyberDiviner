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
            CyberMenuItem(
                title = "周易六爻",
                subtitle = "",
                description = "",
                onClick = onIChing
            )
            Spacer(modifier = Modifier.height(24.dp))

            CyberMenuItem(
                title = "赛博塔罗",
                subtitle = "",
                description = "",
                onClick = onTarot
            )
            Spacer(modifier = Modifier.height(24.dp))

            CyberMenuItem(
                title = "视界摸骨",
                subtitle = "",
                description = "",
                onClick = onVision
            )
            Spacer(modifier = Modifier.height(24.dp))

            CyberMenuItem(
                title = "电子木鱼",
                subtitle = "",
                description = "",
                onClick = onMuyu
            )
            Spacer(modifier = Modifier.height(24.dp))

            CyberMenuItem(
                title = "赛博黄历",
                subtitle = "",
                description = "",
                onClick = onAlmanac
            )
        }
    }
}
