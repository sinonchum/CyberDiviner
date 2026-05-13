package com.cyberdiviner.ui.rituals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.shared.CyberButton
import com.cyberdiviner.ui.shared.CyberMenuItem
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.TextMuted

/**
 * RitualsMenuScreen -- Intermediate menu for ritual selection.
 *
 * Three options: 周易六爻, 赛博塔罗, 视界摸骨.
 * Left-aligned, same layout as HomeScreen.
 */
@Composable
fun RitualsMenuScreen(
    onIChing: () -> Unit,
    onTarot: () -> Unit,
    onVision: () -> Unit,
    onBack: () -> Unit
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
        }

        // Bottom back button
        CyberButton(
            text = "[ 返回 ]",
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 48.dp)
        )
    }
}
