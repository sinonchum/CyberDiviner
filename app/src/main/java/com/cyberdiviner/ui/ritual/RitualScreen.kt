package com.cyberdiviner.ui.ritual

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberWhite
import com.cyberdiviner.ui.theme.TextMuted

/**
 * RitualScreen -- Tool selection (Layer 3).
 *
 * Three ritual tools: I Ching, Tarot, Vision.
 * Pure monochrome. Geometric line icons drawn via Canvas.
 */

enum class RitualTool(
    val id: String,
    val label: String,
    val englishLabel: String,
    val description: String,
    val geometry: String  // ASCII representation of the geometric icon
) {
    I_CHING(
        "i_ching",
        "\u5468\u6613",
        "I CHING",
        "\u4E8C\u8FDB\u5236\u56E0\u679C\u63A8\u6F14",
        "\u2500\u2500\u2500 \u2500\u2500\u2500\n\u2500\u2500\u2500 \u2500 \u2500\u2500"
    ),
    TAROT(
        "tarot",
        "\u5854\u7F57",
        "TAROT",
        "\u9ED1\u767D\u6728\u523B\u7248\u753B",
        "\u250C\u2500\u2500\u2500\u2500\u2510\n\u2502 \u25C6 \u2502\n\u2514\u2500\u2500\u2500\u2500\u2518"
    ),
    VISION(
        "vision",
        "\u89C6\u754C",
        "VISION",
        "\u751F\u7406\u9891\u7387\u6355\u6349",
        "\u25CE \u25CE"
    )
}

@Composable
fun RitualScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "[ \u4EEA\u89C4\u6267\u884C ]",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "RITUAL EXECUTION",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Tools
            RitualTool.entries.forEach { tool ->
                RitualToolRow(tool)
                Spacer(modifier = Modifier.height(32.dp))
            }

            Spacer(modifier = Modifier.height(48.dp))

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

@Composable
private fun RitualToolRow(tool: RitualTool) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Geometric icon (ASCII art)
        Text(
            text = tool.geometry,
            color = CyberWhite,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(80.dp)
        )

        // Label + description
        Column {
            Text(
                text = tool.label,
                color = CyberWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tool.englishLabel,
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tool.description,
                color = TextMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
