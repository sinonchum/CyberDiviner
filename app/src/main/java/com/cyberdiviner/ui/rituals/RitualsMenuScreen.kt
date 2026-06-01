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
import com.cyberdiviner.ui.settings.AppLanguage

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
            SectionHeader(title = if (lang == AppLanguage.BILINGUAL_EN) "RITUAL EXECUTION" else "术数推演", subtitle = if (lang == AppLanguage.BILINGUAL_EN) "术数推演" else "RITUAL EXECUTION")
            Spacer(modifier = Modifier.height(48.dp))

            StaggeredItem(index = 0) {
                CyberMenuItem(
                    title = if (lang == AppLanguage.BILINGUAL_EN) "I-CHING" else "周易六爻",
                    subtitle = if (lang == AppLanguage.BILINGUAL_EN) "周易六爻" else "I-CHING",
                    description = CyberCopy.ritualIChingDesc(lang),
                    onClick = onIChing
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            StaggeredItem(index = 1) {
                CyberMenuItem(
                    title = if (lang == AppLanguage.BILINGUAL_EN) "CYBER TAROT" else "赛博塔罗",
                    subtitle = if (lang == AppLanguage.BILINGUAL_EN) "赛博塔罗" else "CYBER TAROT",
                    description = CyberCopy.ritualTarotDesc(lang),
                    onClick = onTarot
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            StaggeredItem(index = 2) {
                CyberMenuItem(
                    title = if (lang == AppLanguage.BILINGUAL_EN) "FACE SCAN" else "视界摸骨",
                    subtitle = if (lang == AppLanguage.BILINGUAL_EN) "视界摸骨" else "FACE SCAN",
                    description = CyberCopy.ritualVisionDesc(lang),
                    onClick = onVision
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            StaggeredItem(index = 3) {
                CyberMenuItem(
                    title = if (lang == AppLanguage.BILINGUAL_EN) "SINGING BOWL" else "电子颂钵",
                    subtitle = if (lang == AppLanguage.BILINGUAL_EN) "电子颂钵" else "SINGING BOWL",
                    description = CyberCopy.ritualBowlDesc(lang),
                    onClick = onMuyu
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            StaggeredItem(index = 4) {
                CyberMenuItem(
                    title = if (lang == AppLanguage.BILINGUAL_EN) "ALMANAC" else "赛博黄历",
                    subtitle = if (lang == AppLanguage.BILINGUAL_EN) "赛博黄历" else "ALMANAC",
                    description = CyberCopy.ritualAlmanacDesc(lang),
                    onClick = onAlmanac
                )
            }
        }
    }
}
