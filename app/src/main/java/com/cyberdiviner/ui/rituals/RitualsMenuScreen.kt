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
import com.cyberdiviner.ui.localization.CyberCopy
import com.cyberdiviner.ui.localization.LocalAppLanguage

/**
 * RitualsMenuScreen -- Tab for ritual selection (bottom nav destination).
 *
 * Four options: 周易六爻, 赛博塔罗, 视界摸骨, 电子颂钵.
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
    val lang = LocalAppLanguage.current

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
            SectionHeader(title = CyberCopy.ritualsTitle(lang), subtitle = CyberCopy.ritualsSubtitle(lang))
            Spacer(modifier = Modifier.height(48.dp))

            StaggeredItem(index = 0) {
                CyberMenuItem(
                    title = CyberCopy.ritualIChing(lang),
                    subtitle = "",
                    description = CyberCopy.ritualIChingDesc(lang),
                    onClick = onIChing
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            StaggeredItem(index = 1) {
                CyberMenuItem(
                    title = CyberCopy.ritualTarot(lang),
                    subtitle = "",
                    description = CyberCopy.ritualTarotDesc(lang),
                    onClick = onTarot
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            StaggeredItem(index = 2) {
                CyberMenuItem(
                    title = CyberCopy.ritualVision(lang),
                    subtitle = "",
                    description = CyberCopy.ritualVisionDesc(lang),
                    onClick = onVision
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            StaggeredItem(index = 3) {
                CyberMenuItem(
                    title = CyberCopy.ritualBowl(lang),
                    subtitle = "",
                    description = CyberCopy.ritualBowlDesc(lang),
                    onClick = onMuyu
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            StaggeredItem(index = 4) {
                CyberMenuItem(
                    title = CyberCopy.ritualAlmanac(lang),
                    subtitle = "",
                    description = CyberCopy.ritualAlmanacDesc(lang),
                    onClick = onAlmanac
                )
            }
        }
    }
}
