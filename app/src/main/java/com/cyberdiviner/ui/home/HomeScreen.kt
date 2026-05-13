package com.cyberdiviner.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.shared.CyberMenuItem
import com.cyberdiviner.ui.theme.AccentRed
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.GrayCaption

/**
 * HomeScreen -- The central hub. Three Chinese-only menu entries.
 *
 * Left-aligned, vertically centered. No English.大面积黑色留白.
 * Bridgewater-inspired: uppercase monospace title, red underline, generous spacing.
 */
@Composable
fun HomeScreen(
    onOracle: () -> Unit,
    onRituals: () -> Unit,
    onArchive: () -> Unit,
    onConfig: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(start = 32.dp, top = 0.dp, end = 32.dp, bottom = 0.dp)
    ) {
        // CONFIG button — top-right
        Text(
            text = "[ CONFIG ]",
            color = GrayCaption,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp)
                .clickable { onConfig() }
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            // Title — uppercase monospace
            Text(
                text = "CYBERDIVINER",
                color = GrayCaption,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            // Red underline
            Canvas(
                modifier = Modifier
                    .width(160.dp)
                    .padding(top = 4.dp)
                    .height(2.dp)
            ) {
                drawRect(
                    color = AccentRed,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            CyberMenuItem(
                title = "叩问天机",
                subtitle = "",
                description = "",
                onClick = onOracle
            )
            Spacer(modifier = Modifier.height(48.dp))

            CyberMenuItem(
                title = "术数推演",
                subtitle = "",
                description = "",
                onClick = onRituals
            )
            Spacer(modifier = Modifier.height(48.dp))

            CyberMenuItem(
                title = "因果命簿",
                subtitle = "",
                description = "",
                onClick = onArchive
            )

            // Thin red divider
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp)
                    .height(1.dp)
            ) {
                drawRect(
                    color = AccentRed,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "CYBERDIVINER v1.0",
                color = GrayCaption,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        }
    }
}
