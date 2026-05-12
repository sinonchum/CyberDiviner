package com.cyberdiviner.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.cyberdiviner.MainActivity
import com.cyberdiviner.engine.AlmanacEngine
import java.time.LocalDate

class AlmanacWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val today = LocalDate.now()
        val reading = AlmanacEngine.dailyReading(today)
        val luckyActivity = reading.auspiciousActivities.random()
        val dayBinary = toBinary(today.dayOfMonth)
        val monthBinary = toBinary(today.monthValue)

        provideContent {
            GlanceTheme {
                AlmanacWidgetContent(
                    dayBinary = dayBinary,
                    monthBinary = monthBinary,
                    energyLevel = reading.dailyEnergy,
                    luckyActivity = luckyActivity.name,
                    luckyActivityEn = luckyActivity.englishName,
                    ganzhi = reading.dayGanzhi.combined
                )
            }
        }
    }

    @Composable
    private fun AlmanacWidgetContent(
        dayBinary: String,
        monthBinary: String,
        energyLevel: String,
        luckyActivity: String,
        luckyActivityEn: String,
        ganzhi: String
    ) {
        val cyberBlack = ColorProvider(Color(0xFF0A0A0F))
        val neonCyan = ColorProvider(Color(0xFF00FFCC))
        val neonMagenta = ColorProvider(Color(0xFFFF00FF))
        val neonGreen = ColorProvider(Color(0xFF39FF14))
        val textSecondary = ColorProvider(Color(0xFF8888AA))
        val cyberGray = ColorProvider(Color(0xFF1A1A2E))

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(cyberBlack)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) {
                // Header
                Text(
                    text = "⚡ CYBERDIVINER ALMANAC",
                    style = TextStyle(
                        color = neonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Binary date display + energy
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "DATE.BIN",
                            style = TextStyle(
                                color = textSecondary,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = monthBinary,
                            style = TextStyle(
                                color = neonMagenta,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = dayBinary,
                            style = TextStyle(
                                color = neonCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(12.dp))

                    Column {
                        Text(
                            text = "干支: $ganzhi",
                            style = TextStyle(
                                color = neonGreen,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = "ENERGY: $energyLevel",
                            style = TextStyle(
                                color = neonMagenta,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Separator
                Text(
                    text = "━━━━━━━━━━━━━━━━━━━━━━",
                    style = TextStyle(
                        color = cyberGray,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Lucky activity
                Text(
                    text = "▸ TODAY'S LUCKY ACTIVITY:",
                    style = TextStyle(
                        color = textSecondary,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )
                Text(
                    text = "✅ $luckyActivity",
                    style = TextStyle(
                        color = neonGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
                Text(
                    text = "   ($luckyActivityEn)",
                    style = TextStyle(
                        color = textSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                Text(
                    text = "[ TAP TO OPEN ]",
                    style = TextStyle(
                        color = textSecondary,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }
        }
    }

    private fun toBinary(value: Int): String {
        return Integer.toBinaryString(value).padStart(8, '0')
    }
}
