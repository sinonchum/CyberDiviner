package com.cyberdiviner.ui.learning

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.ui.theme.*

/**
 * Major Arcana card data — number, name, and theme for all 22 cards.
 */
data class MajorArcanaCard(
    val number: Int,
    val romanNumeral: String,
    val name: String,
    val theme: String
)

val majorArcanaCards = listOf(
    MajorArcanaCard(0, "0", "愚者", "新开始与未知"),
    MajorArcanaCard(1, "I", "魔术师", "创造力与意志"),
    MajorArcanaCard(2, "II", "女祭司", "直觉与内在智慧"),
    MajorArcanaCard(3, "III", "女皇", "丰饶与滋养"),
    MajorArcanaCard(4, "IV", "皇帝", "权威与结构"),
    MajorArcanaCard(5, "V", "教皇", "传统与信仰"),
    MajorArcanaCard(6, "VI", "恋人", "选择与关系"),
    MajorArcanaCard(7, "VII", "战车", "意志力与前进"),
    MajorArcanaCard(8, "VIII", "力量", "勇气与耐心"),
    MajorArcanaCard(9, "IX", "隐者", "内省与指引"),
    MajorArcanaCard(10, "X", "命运之轮", "周期与转变"),
    MajorArcanaCard(11, "XI", "正义", "平衡与因果"),
    MajorArcanaCard(12, "XII", "倒吊人", "放下与新视角"),
    MajorArcanaCard(13, "XIII", "死神", "结束与转化"),
    MajorArcanaCard(14, "XIV", "节制", "调和与耐心"),
    MajorArcanaCard(15, "XV", "恶魔", "束缚与觉察"),
    MajorArcanaCard(16, "XVI", "塔", "突变与释放"),
    MajorArcanaCard(17, "XVII", "星星", "希望与灵感"),
    MajorArcanaCard(18, "XVIII", "月亮", "幻象与潜意识"),
    MajorArcanaCard(19, "XIX", "太阳", "成功与活力"),
    MajorArcanaCard(20, "XX", "审判", "觉醒与评估"),
    MajorArcanaCard(21, "XXI", "世界", "完成与整合")
)

/** Look up a card by Chinese name */
fun findMajorArcana(name: String): MajorArcanaCard? =
    majorArcanaCards.find { it.name == name }

/** Check if a string is a major arcana card name */
fun isMajorArcanaName(name: String): Boolean =
    majorArcanaCards.any { it.name == name }

/**
 * Draw a stylized geometric symbol for a major arcana card.
 * Each card has a unique simple icon drawn with Canvas.
 */
@Composable
fun TarotCardIcon(
    cardName: String,
    modifier: Modifier = Modifier,
    iconSize: Float = 40f
) {
    val card = findMajorArcana(cardName) ?: return
    val color = CyberWhite
    val accent = AccentRed

    Canvas(modifier = modifier.size((iconSize / 2).dp)) {
        val s = size.minDimension
        val cx = s / 2f
        val cy = s / 2f
        val r = s * 0.38f

        when (card.number) {
            0 -> drawFool(cx, cy, r, color)          // 愚者: spiral/∞
            1 -> drawMagician(cx, cy, r, color)      // 魔术师: infinity above
            2 -> drawPriestess(cx, cy, r, color)     // 女祭司: crescent moon
            3 -> drawEmpress(cx, cy, r, color)       // 女皇: star/flower
            4 -> drawEmperor(cx, cy, r, color)       // 皇帝: triangle
            5 -> drawHierophant(cx, cy, r, color)    // 教皇: triple cross
            6 -> drawLovers(cx, cy, r, color)        // 恋人: two circles
            7 -> drawChariot(cx, cy, r, color)       // 战车: shield/arrow
            8 -> drawStrength(cx, cy, r, color)      // 力量: lemniscate
            9 -> drawHermit(cx, cy, r, color)        // 隐者: lantern
            10 -> drawWheel(cx, cy, r, color)        // 命运之轮: wheel
            11 -> drawJustice(cx, cy, r, color)      // 正义: scales
            12 -> drawHangedMan(cx, cy, r, color)    // 倒吊人: inverted triangle
            13 -> drawDeath(cx, cy, r, color)        // 死神: cross
            14 -> drawTemperance(cx, cy, r, color)   // 节制: two triangles
            15 -> drawDevil(cx, cy, r, color)        // 恶魔: pentagram
            16 -> drawTower(cx, cy, r, color)        // 塔: zigzag
            17 -> drawStar(cx, cy, r, color)         // 星星: 8-point star
            18 -> drawMoon(cx, cy, r, color, accent) // 月亮: crescent
            19 -> drawSun(cx, cy, r, color)          // 太阳: sun rays
            20 -> drawJudgement(cx, cy, r, color)    // 审判: trumpet
            21 -> drawWorld(cx, cy, r, color)        // 世界: wreath
        }
    }
}

// ── Individual card symbol drawings ─────────────────────────────────

private fun DrawScope.drawFool(cx: Float, cy: Float, r: Float, color: Color) {
    // Spiral / winding path
    drawCircle(color, r * 0.15f, Offset(cx, cy - r * 0.3f), style = Stroke(2f))
    val path = Path().apply {
        moveTo(cx, cy + r * 0.2f)
        cubicTo(cx + r, cy + r * 0.2f, cx + r, cy - r * 0.6f, cx, cy - r * 0.6f)
        cubicTo(cx - r * 0.6f, cy - r * 0.6f, cx - r * 0.6f, cy + r * 0.1f, cx, cy + r * 0.1f)
    }
    drawPath(path, color, style = Stroke(2f, cap = StrokeCap.Round))
}

private fun DrawScope.drawMagician(cx: Float, cy: Float, r: Float, color: Color) {
    // Infinity symbol (∞)
    val path = Path().apply {
        moveTo(cx - r * 0.5f, cy - r * 0.2f)
        cubicTo(cx - r, cy - r * 0.8f, cx + r, cy - r * 0.8f, cx + r * 0.5f, cy - r * 0.2f)
        cubicTo(cx + r, cy + r * 0.4f, cx - r, cy + r * 0.4f, cx - r * 0.5f, cy - r * 0.2f)
    }
    drawPath(path, color, style = Stroke(2f, cap = StrokeCap.Round))
    // Wand
    drawLine(color, Offset(cx, cy + r * 0.3f), Offset(cx, cy + r * 0.9f), 2f, StrokeCap.Round)
}

private fun DrawScope.drawPriestess(cx: Float, cy: Float, r: Float, color: Color) {
    // Crescent moon
    drawArc(color, -30f, 240f, false, topLeft = Offset(cx - r * 0.6f, cy - r * 0.6f),
        size = Size(r * 1.2f, r * 1.2f), style = Stroke(2f, cap = StrokeCap.Round))
    // Scroll
    drawLine(color, Offset(cx - r * 0.3f, cy + r * 0.5f), Offset(cx + r * 0.3f, cy + r * 0.5f), 2f)
    drawLine(color, Offset(cx - r * 0.3f, cy + r * 0.7f), Offset(cx + r * 0.3f, cy + r * 0.7f), 2f)
}

private fun DrawScope.drawEmpress(cx: Float, cy: Float, r: Float, color: Color) {
    // 5-point star
    val points = (0..4).map { i ->
        val angle = Math.toRadians((i * 72 - 90).toDouble())
        Offset(cx + r * 0.8f * kotlin.math.cos(angle).toFloat(),
            cy + r * 0.8f * kotlin.math.sin(angle).toFloat())
    }
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        lineTo(points[2].x, points[2].y)
        lineTo(points[4].x, points[4].y)
        lineTo(points[1].x, points[1].y)
        lineTo(points[3].x, points[3].y)
        close()
    }
    drawPath(path, color, style = Stroke(2f, cap = StrokeCap.Round))
}

private fun DrawScope.drawEmperor(cx: Float, cy: Float, r: Float, color: Color) {
    // Triangle (authority)
    val path = Path().apply {
        moveTo(cx, cy - r * 0.8f)
        lineTo(cx + r * 0.7f, cy + r * 0.5f)
        lineTo(cx - r * 0.7f, cy + r * 0.5f)
        close()
    }
    drawPath(path, color, style = Stroke(2f, cap = StrokeCap.Round))
}

private fun DrawScope.drawHierophant(cx: Float, cy: Float, r: Float, color: Color) {
    // Triple cross
    drawLine(color, Offset(cx, cy - r * 0.8f), Offset(cx, cy + r * 0.8f), 2f, StrokeCap.Round)
    drawLine(color, Offset(cx - r * 0.3f, cy - r * 0.4f), Offset(cx + r * 0.3f, cy - r * 0.4f), 2f)
    drawLine(color, Offset(cx - r * 0.4f, cy), Offset(cx + r * 0.4f, cy), 2f)
    drawLine(color, Offset(cx - r * 0.5f, cy + r * 0.4f), Offset(cx + r * 0.5f, cy + r * 0.4f), 2f)
}

private fun DrawScope.drawLovers(cx: Float, cy: Float, r: Float, color: Color) {
    // Two overlapping circles
    drawCircle(color, r * 0.45f, Offset(cx - r * 0.25f, cy), style = Stroke(2f))
    drawCircle(color, r * 0.45f, Offset(cx + r * 0.25f, cy), style = Stroke(2f))
}

private fun DrawScope.drawChariot(cx: Float, cy: Float, r: Float, color: Color) {
    // Shield / arrow pointing up
    val path = Path().apply {
        moveTo(cx, cy - r * 0.8f)
        lineTo(cx + r * 0.5f, cy)
        lineTo(cx + r * 0.3f, cy + r * 0.6f)
        lineTo(cx - r * 0.3f, cy + r * 0.6f)
        lineTo(cx - r * 0.5f, cy)
        close()
    }
    drawPath(path, color, style = Stroke(2f, cap = StrokeCap.Round))
    drawLine(color, Offset(cx, cy - r * 0.3f), Offset(cx, cy + r * 0.3f), 2f, StrokeCap.Round)
}

private fun DrawScope.drawStrength(cx: Float, cy: Float, r: Float, color: Color) {
    // Lemniscate (infinity)
    val path = Path().apply {
        moveTo(cx - r * 0.6f, cy)
        cubicTo(cx - r, cy - r * 0.7f, cx, cy - r * 0.7f, cx, cy)
        cubicTo(cx, cy + r * 0.7f, cx + r, cy + r * 0.7f, cx + r * 0.6f, cy)
        cubicTo(cx + r, cy - r * 0.7f, cx, cy - r * 0.7f, cx, cy)
        cubicTo(cx, cy + r * 0.7f, cx - r, cy + r * 0.7f, cx - r * 0.6f, cy)
    }
    drawPath(path, color, style = Stroke(2f, cap = StrokeCap.Round))
}

private fun DrawScope.drawHermit(cx: Float, cy: Float, r: Float, color: Color) {
    // Lantern
    drawLine(color, Offset(cx, cy - r * 0.8f), Offset(cx, cy - r * 0.3f), 2f, StrokeCap.Round)
    val path = Path().apply {
        moveTo(cx - r * 0.35f, cy - r * 0.3f)
        lineTo(cx + r * 0.35f, cy - r * 0.3f)
        lineTo(cx + r * 0.25f, cy + r * 0.4f)
        lineTo(cx - r * 0.25f, cy + r * 0.4f)
        close()
    }
    drawPath(path, color, style = Stroke(2f, cap = StrokeCap.Round))
    drawCircle(color, r * 0.12f, Offset(cx, cy + r * 0.05f))
    drawLine(color, Offset(cx, cy + r * 0.4f), Offset(cx, cy + r * 0.7f), 2f, StrokeCap.Round)
}

private fun DrawScope.drawWheel(cx: Float, cy: Float, r: Float, color: Color) {
    // Wheel with spokes
    drawCircle(color, r * 0.7f, Offset(cx, cy), style = Stroke(2f))
    drawCircle(color, r * 0.15f, Offset(cx, cy), style = Stroke(2f))
    for (i in 0..3) {
        val angle = Math.toRadians((i * 45).toDouble())
        drawLine(color, Offset(cx, cy),
            Offset(cx + r * 0.7f * kotlin.math.cos(angle).toFloat(),
                cy + r * 0.7f * kotlin.math.sin(angle).toFloat()), 1.5f)
    }
}

private fun DrawScope.drawJustice(cx: Float, cy: Float, r: Float, color: Color) {
    // Scales
    drawLine(color, Offset(cx, cy - r * 0.6f), Offset(cx, cy + r * 0.6f), 2f, StrokeCap.Round)
    drawLine(color, Offset(cx - r * 0.6f, cy - r * 0.3f), Offset(cx + r * 0.6f, cy - r * 0.3f), 2f)
    // Left pan
    val leftPan = Path().apply {
        moveTo(cx - r * 0.6f, cy - r * 0.3f)
        lineTo(cx - r * 0.8f, cy)
        lineTo(cx - r * 0.4f, cy)
        close()
    }
    drawPath(leftPan, color, style = Stroke(1.5f))
    // Right pan
    val rightPan = Path().apply {
        moveTo(cx + r * 0.6f, cy - r * 0.3f)
        lineTo(cx + r * 0.4f, cy)
        lineTo(cx + r * 0.8f, cy)
        close()
    }
    drawPath(rightPan, color, style = Stroke(1.5f))
}

private fun DrawScope.drawHangedMan(cx: Float, cy: Float, r: Float, color: Color) {
    // Inverted triangle
    val path = Path().apply {
        moveTo(cx, cy + r * 0.8f)
        lineTo(cx + r * 0.6f, cy - r * 0.4f)
        lineTo(cx - r * 0.6f, cy - r * 0.4f)
        close()
    }
    drawPath(path, color, style = Stroke(2f, cap = StrokeCap.Round))
    drawLine(color, Offset(cx, cy - r * 0.4f), Offset(cx, cy - r * 0.8f), 2f, StrokeCap.Round)
}

private fun DrawScope.drawDeath(cx: Float, cy: Float, r: Float, color: Color) {
    // Cross
    drawLine(color, Offset(cx, cy - r * 0.8f), Offset(cx, cy + r * 0.6f), 2.5f, StrokeCap.Round)
    drawLine(color, Offset(cx - r * 0.5f, cy - r * 0.2f), Offset(cx + r * 0.5f, cy - r * 0.2f), 2.5f)
}

private fun DrawScope.drawTemperance(cx: Float, cy: Float, r: Float, color: Color) {
    // Two overlapping triangles
    val up = Path().apply {
        moveTo(cx, cy - r * 0.7f); lineTo(cx + r * 0.5f, cy + r * 0.3f); lineTo(cx - r * 0.5f, cy + r * 0.3f); close()
    }
    val down = Path().apply {
        moveTo(cx, cy + r * 0.7f); lineTo(cx + r * 0.5f, cy - r * 0.3f); lineTo(cx - r * 0.5f, cy - r * 0.3f); close()
    }
    drawPath(up, color, style = Stroke(1.5f))
    drawPath(down, color, style = Stroke(1.5f))
}

private fun DrawScope.drawDevil(cx: Float, cy: Float, r: Float, color: Color) {
    // Pentagram
    val points = (0..4).map { i ->
        val angle = Math.toRadians((i * 72 - 90).toDouble())
        Offset(cx + r * 0.7f * kotlin.math.cos(angle).toFloat(),
            cy + r * 0.7f * kotlin.math.sin(angle).toFloat())
    }
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        lineTo(points[2].x, points[2].y)
        lineTo(points[4].x, points[4].y)
        lineTo(points[1].x, points[1].y)
        lineTo(points[3].x, points[3].y)
        close()
    }
    drawPath(path, color, style = Stroke(2f, cap = StrokeCap.Round))
}

private fun DrawScope.drawTower(cx: Float, cy: Float, r: Float, color: Color) {
    // Tower with zigzag (lightning)
    val path = Path().apply {
        moveTo(cx - r * 0.3f, cy + r * 0.8f)
        lineTo(cx - r * 0.3f, cy - r * 0.2f)
        lineTo(cx - r * 0.5f, cy - r * 0.2f)
        lineTo(cx, cy - r * 0.8f)
        lineTo(cx + r * 0.5f, cy - r * 0.2f)
        lineTo(cx + r * 0.3f, cy - r * 0.2f)
        lineTo(cx + r * 0.3f, cy + r * 0.8f)
        close()
    }
    drawPath(path, color, style = Stroke(2f, cap = StrokeCap.Round))
    // Lightning bolt
    val bolt = Path().apply {
        moveTo(cx + r * 0.1f, cy - r * 0.5f)
        lineTo(cx - r * 0.05f, cy - r * 0.1f)
        lineTo(cx + r * 0.15f, cy - r * 0.1f)
        lineTo(cx, cy + r * 0.3f)
    }
    drawPath(bolt, AccentRed, style = Stroke(2f, cap = StrokeCap.Round))
}

private fun DrawScope.drawStar(cx: Float, cy: Float, r: Float, color: Color) {
    // 8-point star
    for (i in 0..7) {
        val angle = Math.toRadians((i * 45).toDouble())
        val len = if (i % 2 == 0) r * 0.8f else r * 0.4f
        drawLine(color, Offset(cx, cy),
            Offset(cx + len * kotlin.math.cos(angle).toFloat(),
                cy + len * kotlin.math.sin(angle).toFloat()), 2f, StrokeCap.Round)
    }
    drawCircle(color, r * 0.12f, Offset(cx, cy))
}

private fun DrawScope.drawMoon(cx: Float, cy: Float, r: Float, color: Color, accent: Color) {
    // Crescent moon
    drawCircle(color, r * 0.7f, Offset(cx, cy), style = Stroke(2f))
    drawCircle(Color.Black, r * 0.55f, Offset(cx + r * 0.3f, cy - r * 0.1f))
    // Stars
    drawCircle(accent, r * 0.06f, Offset(cx - r * 0.3f, cy - r * 0.4f))
    drawCircle(accent, r * 0.04f, Offset(cx - r * 0.5f, cy - r * 0.2f))
}

private fun DrawScope.drawSun(cx: Float, cy: Float, r: Float, color: Color) {
    // Sun with rays
    drawCircle(color, r * 0.35f, Offset(cx, cy), style = Stroke(2f))
    for (i in 0..7) {
        val angle = Math.toRadians((i * 45).toDouble())
        drawLine(color,
            Offset(cx + r * 0.45f * kotlin.math.cos(angle).toFloat(),
                cy + r * 0.45f * kotlin.math.sin(angle).toFloat()),
            Offset(cx + r * 0.7f * kotlin.math.cos(angle).toFloat(),
                cy + r * 0.7f * kotlin.math.sin(angle).toFloat()),
            2f, StrokeCap.Round)
    }
}

private fun DrawScope.drawJudgement(cx: Float, cy: Float, r: Float, color: Color) {
    // Trumpet / horn shape
    val path = Path().apply {
        moveTo(cx - r * 0.5f, cy)
        lineTo(cx - r * 0.2f, cy - r * 0.15f)
        lineTo(cx + r * 0.3f, cy - r * 0.5f)
        lineTo(cx + r * 0.3f, cy + r * 0.5f)
        lineTo(cx - r * 0.2f, cy + r * 0.15f)
        close()
    }
    drawPath(path, color, style = Stroke(2f, cap = StrokeCap.Round))
    drawLine(color, Offset(cx - r * 0.5f, cy), Offset(cx - r * 0.8f, cy), 2f, StrokeCap.Round)
}

private fun DrawScope.drawWorld(cx: Float, cy: Float, r: Float, color: Color) {
    // Wreath / circle with cross
    drawCircle(color, r * 0.7f, Offset(cx, cy), style = Stroke(2f))
    drawLine(color, Offset(cx, cy - r * 0.5f), Offset(cx, cy + r * 0.5f), 1.5f)
    drawLine(color, Offset(cx - r * 0.4f, cy), Offset(cx + r * 0.4f, cy), 1.5f)
    // Corner dots
    drawCircle(color, r * 0.08f, Offset(cx - r * 0.5f, cy - r * 0.5f))
    drawCircle(color, r * 0.08f, Offset(cx + r * 0.5f, cy - r * 0.5f))
    drawCircle(color, r * 0.08f, Offset(cx - r * 0.5f, cy + r * 0.5f))
    drawCircle(color, r * 0.08f, Offset(cx + r * 0.5f, cy + r * 0.5f))
}
