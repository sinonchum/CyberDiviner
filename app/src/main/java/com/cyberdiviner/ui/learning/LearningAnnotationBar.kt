package com.cyberdiviner.ui.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.theme.*

/**
 * Shared annotation bar for divination result screens.
 * Shows learning annotations for completed lessons.
 *
 * @param annotations list of (title, text) pairs from completed lessons
 */
@Composable
fun LearningAnnotationBar(
    annotations: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    if (annotations.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "学习注释",
            color = AccentRed,
            fontSize = 11.sp,
            fontFamily = HuiwenFontFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        annotations.forEach { (title, text) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GrayBorder)
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = title,
                        color = CyberWhite,
                        fontSize = 13.sp,
                        fontFamily = HuiwenFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = text,
                        color = GrayBody,
                        fontSize = 12.sp,
                        fontFamily = WenKaiFontFamily,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
