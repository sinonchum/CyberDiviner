package com.cyberdiviner.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.theme.*

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: @Composable (Boolean) -> Unit  // selected -> Unit
) {
    data object Oracle : BottomNavItem(
        route = Routes.ORACLE,
        label = "叩问天机",
        icon = { selected -> OracleIcon(selected) }
    )
    data object Rituals : BottomNavItem(
        route = Routes.RITUALS,
        label = "术数推演",
        icon = { selected -> TrigramIcon(selected) }
    )
    data object Learn : BottomNavItem(
        route = Routes.LEARN,
        label = "修习之路",
        icon = { selected -> LearnIcon(selected) }
    )
    data object Archive : BottomNavItem(
        route = Routes.ARCHIVE,
        label = "因果命簿",
        icon = { selected -> ScrollIcon(selected) }
    )
}

@Composable
private fun OracleIcon(selected: Boolean) {
    val color = if (selected) CyberWhite else GrayCaption
    Canvas(modifier = Modifier.size(24.dp)) {
        val sw = 1.5.dp.toPx()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val rx = size.width * 0.42f
        val ry = size.height * 0.22f
        // Eye outline (almond shape using two arcs)
        val eyePath = Path().apply {
            moveTo(cx - rx, cy)
            quadraticBezierTo(cx, cy - ry * 2.5f, cx + rx, cy)
            quadraticBezierTo(cx, cy + ry * 2.5f, cx - rx, cy)
            close()
        }
        drawPath(eyePath, color, style = Stroke(sw, cap = StrokeCap.Square))
        // Iris circle
        drawCircle(color, radius = ry * 0.7f, center = Offset(cx, cy), style = Stroke(sw, cap = StrokeCap.Square))
    }
}

@Composable
private fun TrigramIcon(selected: Boolean) {
    val color = if (selected) CyberWhite else GrayCaption
    Canvas(modifier = Modifier.size(24.dp)) {
        val sw = 1.5.dp.toPx()
        val lineLen = size.width * 0.7f
        val cx = size.width / 2f
        for (i in 0..2) {
            val y = size.height * 0.25f + i * (size.height * 0.25f)
            val left = cx - lineLen / 2f
            val right = cx + lineLen / 2f
            if (i == 1) {
                // Yin line (broken)
                val midGap = 4.dp.toPx()
                drawLine(color, Offset(left, y), Offset(cx - midGap, y), sw, cap = StrokeCap.Square)
                drawLine(color, Offset(cx + midGap, y), Offset(right, y), sw, cap = StrokeCap.Square)
            } else {
                // Yang line (solid)
                drawLine(color, Offset(left, y), Offset(right, y), sw, cap = StrokeCap.Square)
            }
        }
    }
}

@Composable
private fun ScrollIcon(selected: Boolean) {
    val color = if (selected) CyberWhite else GrayCaption
    Canvas(modifier = Modifier.size(24.dp)) {
        val sw = 1.5.dp.toPx()
        val pad = 3.dp.toPx()
        val left = pad
        val top = pad
        val right = size.width - pad
        val bottom = size.height - pad
        // Document outline
        drawLine(color, Offset(left, top), Offset(right, top), sw, cap = StrokeCap.Square)
        drawLine(color, Offset(right, top), Offset(right, bottom), sw, cap = StrokeCap.Square)
        drawLine(color, Offset(right, bottom), Offset(left, bottom), sw, cap = StrokeCap.Square)
        drawLine(color, Offset(left, bottom), Offset(left, top), sw, cap = StrokeCap.Square)
        // Inner lines (text representation)
        val lineY1 = top + size.height * 0.28f
        val lineY2 = top + size.height * 0.5f
        val lineY3 = top + size.height * 0.72f
        val innerLeft = left + 5.dp.toPx()
        val innerRight = right - 5.dp.toPx()
        drawLine(color, Offset(innerLeft, lineY1), Offset(innerRight, lineY1), sw * 0.7f, cap = StrokeCap.Square)
        drawLine(color, Offset(innerLeft, lineY2), Offset(innerRight * 0.8f, lineY2), sw * 0.7f, cap = StrokeCap.Square)
        drawLine(color, Offset(innerLeft, lineY3), Offset(innerRight * 0.6f, lineY3), sw * 0.7f, cap = StrokeCap.Square)
    }
}

@Composable
private fun LearnIcon(selected: Boolean) {
    val color = if (selected) CyberWhite else GrayCaption
    Canvas(modifier = Modifier.size(24.dp)) {
        val sw = 1.5.dp.toPx()
        val cx = size.width / 2f
        val cy = size.height / 2f
        // Open book icon — two page shapes
        val pageW = size.width * 0.35f
        val pageH = size.height * 0.35f
        val top = cy - pageH
        val bottom = cy + pageH * 0.4f
        // Left page
        drawLine(color, Offset(cx, top), Offset(cx, bottom), sw, cap = StrokeCap.Square)
        drawLine(color, Offset(cx, top), Offset(cx - pageW, top + pageH * 0.2f), sw, cap = StrokeCap.Square)
        drawLine(color, Offset(cx - pageW, top + pageH * 0.2f), Offset(cx - pageW, bottom), sw, cap = StrokeCap.Square)
        // Right page
        drawLine(color, Offset(cx, top), Offset(cx + pageW, top + pageH * 0.2f), sw, cap = StrokeCap.Square)
        drawLine(color, Offset(cx + pageW, top + pageH * 0.2f), Offset(cx + pageW, bottom), sw, cap = StrokeCap.Square)
        // Bottom spine
        drawLine(color, Offset(cx - pageW, bottom), Offset(cx + pageW, bottom), sw * 0.7f, cap = StrokeCap.Square)
    }
}

val bottomNavItems = listOf(
    BottomNavItem.Oracle,
    BottomNavItem.Rituals,
    BottomNavItem.Learn,
    BottomNavItem.Archive
)

/**
 * Persistent bottom navigation bar — pure B&W, HuiwenFontFamily labels.
 */
@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = CyberBlack,
        contentColor = CyberWhite,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = { item.icon(selected) },
                label = {
                    Text(
                        text = item.label,
                        fontFamily = HuiwenFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                },
                selected = selected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CyberWhite,
                    unselectedIconColor = GrayCaption,
                    selectedTextColor = CyberWhite,
                    unselectedTextColor = GrayCaption,
                    indicatorColor = Color(0xFF111111)
                )
            )
        }
    }
}
