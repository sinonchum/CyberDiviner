package com.cyberdiviner.ui.ritual
import com.cyberdiviner.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.shared.CyberMenuItem
import com.cyberdiviner.ui.shared.IChingIcon
import com.cyberdiviner.ui.shared.TarotIcon
import com.cyberdiviner.ui.shared.VisionIcon
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.GrayMuted

/**
 * RitualScreen -- Tool selection (Layer 3).
 *
 * Three ritual tools: I Ching, Tarot, Vision.
 * Pure monochrome. Canvas geometric icons via CyberIcons.
 * Currently dead code (not wired in NavGraph v5.0) but ready if needed.
 */

@Composable
fun RitualScreen(
    onBack: () -> Unit,
    onIChing: () -> Unit,
    onTarot: () -> Unit,
    onVision: () -> Unit
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
            // ── Header ─────────────────────────────────────
            Text(
                text = "[ 仪规执行 ]",
                color = GrayMuted,
                fontSize = 11.sp,
                fontFamily = HuiwenFontFamily,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "RITUAL EXECUTION",
                color = GrayMuted,
                fontSize = 11.sp,
                fontFamily = MonoFontFamily,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(40.dp))

            // ── I Ching ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                IChingIcon(modifier = Modifier.padding(top = 14.dp))
                CyberMenuItem(
                    title = "周易",
                    subtitle = "I CHING",
                    description = "二进制因果推演",
                    onClick = onIChing,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // ── Tarot ──────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                TarotIcon(modifier = Modifier.padding(top = 14.dp))
                CyberMenuItem(
                    title = "塔罗",
                    subtitle = "TAROT",
                    description = "黑白木刻画",
                    onClick = onTarot,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            // ── Vision ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                VisionIcon(modifier = Modifier.padding(top = 14.dp))
                CyberMenuItem(
                    title = "视界",
                    subtitle = "VISION",
                    description = "生频率捕捉",
                    onClick = onVision,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ── Back ───────────────────────────────────────
            Text(
                text = "[ RETURN ]",
                color = GrayMuted,
                fontSize = 11.sp,
                fontFamily = MonoFontFamily,
                letterSpacing = 2.sp,
                modifier = Modifier.clickable { onBack() }
            )
        }
    }
}
