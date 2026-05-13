package com.cyberdiviner.ui.archive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.TextMuted

/**
 * ArchiveScreen -- Causal record retrieval (Layer 3).
 *
 * Displays user's divination history, soul hashes, and stored parameters.
 * File-system hierarchy metaphor.
 */

@Composable
fun ArchiveScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Text(
                text = "[ \u5B58\u6863\u68C0\u7D22 ]",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ARCHIVE RETRIEVAL",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "\u2500".repeat(40),
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(24.dp))

            // File hierarchy
            Text(
                text = "/",
                color = TextMuted,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "  \u251C\u2500\u2500 soul_hash/",
                color = TextMuted,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "  \u2502   \u251C\u2500\u2500 pending",
                color = TextMuted,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "  \u2502   \u2514\u2500\u2500 (complete interview to generate)",
                color = TextMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "  \u251C\u2500\u2500 readings/",
                color = TextMuted,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "  \u2502   \u2514\u2500\u2500 (no records yet)",
                color = TextMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "  \u251C\u2500\u2500 parameters/",
                color = TextMuted,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "  \u2502   \u2514\u2500\u2500 birth_config.json",
                color = TextMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "  \u2514\u2500\u2500 export/",
                color = TextMuted,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "      \u2514\u2500\u2500 (generate B&W PNG stubs)",
                color = TextMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.weight(1f))

            // Back
            Text(
                text = "[ RETURN ]",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                modifier = Modifier.clickable { onBack() }
            )
        }
    }
}
