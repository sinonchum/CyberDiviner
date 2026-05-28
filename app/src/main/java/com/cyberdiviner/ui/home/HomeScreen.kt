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
import com.cyberdiviner.ui.theme.*
import com.cyberdiviner.ui.theme.GrayCaption
import com.cyberdiviner.ui.shared.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

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
            .padding(start = 48.dp, top = 0.dp, end = 48.dp, bottom = 0.dp)
    ) {
        // CONFIG button — top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp)
                .clickable { onConfig() }
        ) {
            GearSettingsIcon(iconSize = 16.dp)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            SectionHeader(title = "CYBERDIVINER", subtitle = "")

            Spacer(modifier = Modifier.height(48.dp))

            StaggeredItem(index = 0) {
                CyberMenuItem(
                    title = "叩问天机",
                    subtitle = "",
                    description = "",
                    onClick = onOracle
                )
            }
            Spacer(modifier = Modifier.height(48.dp))

            StaggeredItem(index = 1) {
                CyberMenuItem(
                    title = "术数推演",
                    subtitle = "",
                    description = "",
                    onClick = onRituals
                )
            }
            Spacer(modifier = Modifier.height(48.dp))

            StaggeredItem(index = 2) {
                CyberMenuItem(
                    title = "因果命簿",
                    subtitle = "",
                    description = "",
                    onClick = onArchive
                )
            }

            DividerLine(modifier = Modifier.padding(top = 48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            StatusLine(text = "CYBERDIVINER v${com.cyberdiviner.BuildConfig.VERSION_NAME}")
        }
    }
}
