package com.cyberdiviner.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
 * TerminalScreen -- The中枢 layer.
 *
 * Three高度抽象 entries. Monochrome. Minimal.
 * No emojis. No decorative icons. Pure text + geometry.
 */

enum class TerminalEntry(
    val id: String,
    val label: String,
    val englishLabel: String,
    val description: String,
    val promptChar: String
) {
    CONSULT(
        "consult",
        "\u54A8\u8BE2\u4EE3\u7406",
        "CONSULT AGENT",
        "\u8FDB\u5165 AI Agent \u8BBF\u8C08\u6D41\u7A0B\uFF0C\u901A\u8FC7\u4EA4\u4E92\u83B7\u53D6\u7528\u6237\u753B\u50CF",
        "> _"
    ),
    RITUAL(
        "ritual",
        "\u4EEA\u89C4\u6267\u884C",
        "RITUAL EXECUTION",
        "\u5468\u6613\u3001\u5854\u7F57\u3001\u89C6\u754C\u626B\u63CF\u7B49\u5177\u4F53\u6D4B\u7B97\u5DE5\u5177",
        "\u2571\u2572"
    ),
    ARCHIVE(
        "archive",
        "\u5B58\u6863\u68C0\u7D22",
        "ARCHIVE RETRIEVAL",
        "\u7528\u6237\u7684\u56E0\u679C\u8BB0\u5F55\u3001\u751F\u8FB0\u53C2\u6570\u3001\u5386\u53F2\u7535\u5B50\u5B58\u6839",
        "\u250C\u2500\u2500"
    )
}

@Composable
fun TerminalScreen(
    onConsult: () -> Unit,
    onRitual: () -> Unit,
    onArchive: () -> Unit
) {
    var selected by remember { mutableStateOf<TerminalEntry?>(null) }

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
                text = "CYBERDIVINER v4.0",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "System initialized. Select protocol.",
                color = TextMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(48.dp))

            // ── Three entries ───────────────────────────────────────
            TerminalEntry.entries.forEach { entry ->
                TerminalRow(
                    entry = entry,
                    isSelected = selected == entry,
                    onClick = {
                        selected = entry
                        when (entry) {
                            TerminalEntry.CONSULT -> onConsult()
                            TerminalEntry.RITUAL -> onRitual()
                            TerminalEntry.ARCHIVE -> onArchive()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ── Status bar ──────────────────────────────────────────
            Text(
                text = "\u2500".repeat(40),
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "CAUSAL CHAIN ACTIVE | SOUL HASH PENDING",
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun TerminalRow(
    entry: TerminalEntry,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isSelected) CyberWhite else TextMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Prompt character (left column)
        Text(
            text = entry.promptChar,
            color = textColor,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(40.dp)
        )

        // Label + description (right column)
        Column {
            // Chinese label
            Text(
                text = entry.label,
                color = textColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            // English label
            Text(
                text = entry.englishLabel,
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Description
            Text(
                text = entry.description,
                color = TextMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }
    }
}
