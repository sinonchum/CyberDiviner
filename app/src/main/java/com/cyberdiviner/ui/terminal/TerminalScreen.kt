package com.cyberdiviner.ui.terminal
import com.cyberdiviner.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.shared.CyberMenuItem
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.GrayMuted
import com.cyberdiviner.BuildConfig

/**
 * TerminalScreen — The中枢 layer.
 *
 * Three高度抽象 entries. Monochrome. Minimal.
 * Pure CyberMenuItem composables + Canvas geometric icons.
 */

@Composable
fun TerminalScreen(
    onConsult: () -> Unit,
    onRitual: () -> Unit,
    onArchive: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            // ── Terminal header ─────────────────────────────────────
            Text(
                text = "CYBERDIVINER v${BuildConfig.VERSION_NAME}",
                color = GrayMuted,
                fontSize = 11.sp,
                fontFamily = MonoFontFamily,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "System initialized. Select protocol.",
                color = GrayMuted,
                fontSize = 12.sp,
                fontFamily = MonoFontFamily
            )
            Spacer(modifier = Modifier.height(48.dp))

            // ── Menu item: CONSULT AGENT ────────────────────────────
            CyberMenuItem(
                title = "咨询代理",
                subtitle = "CONSULT AGENT",
                description = "进入天机访谈流程，通过交互获取你的命盘",
                onClick = onConsult
            )
            Spacer(modifier = Modifier.height(24.dp))

            // ── Menu item: RITUAL EXECUTION ─────────────────────────
            CyberMenuItem(
                title = "仪规执行",
                subtitle = "RITUAL EXECUTION",
                description = "周易、塔罗、视界扫描等具体测算工具",
                onClick = onRitual
            )
            Spacer(modifier = Modifier.height(24.dp))

            // ── Menu item: ARCHIVE RETRIEVAL ────────────────────────
            CyberMenuItem(
                title = "存档检索",
                subtitle = "ARCHIVE RETRIEVAL",
                description = "用户的因果记录、生辰参数、历史电子存根",
                onClick = onArchive
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── Footer status line ──────────────────────────────────
            Text(
                text = "CAUSAL CHAIN ACTIVE  |  SOUL HASH PENDING  |  NODE v${BuildConfig.VERSION_NAME}",
                color = GrayMuted,
                fontSize = 10.sp,
                fontFamily = MonoFontFamily,
                letterSpacing = 2.sp
            )
        }
    }
}
