package com.cyberdiviner.ui.shared

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.sin

/**
 * Generates cyberpunk-themed fortune-telling poster images optimised
 * for social-media sharing (1080 × 1920, 9:16 ratio).
 *
 * All rendering is done with Android Canvas — no external image libraries.
 */
object PosterGenerator {

    // ── Poster dimensions ────────────────────────────────────────────────
    private const val WIDTH = 1080
    private const val HEIGHT = 1920
    private const val PAD = 80

    // ── Brand colours (int form) ─────────────────────────────────────────
    private val BG_COLOR = 0xFF0A0A0Fu.toInt()
    private val NEON_CYAN = 0xFFFFFFFFu.toInt()
    private val NEON_MAGENTA = 0xFF999999u.toInt()
    private val NEON_GREEN = 0xFF555555u.toInt()
    private val TEXT_PRIMARY = 0xFFE0E0E0u.toInt()
    private val TEXT_SECONDARY = 0xFF8888AAu.toInt()
    private val FORTUNE_GOLD = 0xFFFFD700u.toInt()
    private val SURFACE_COLOR = 0xFF16213Eu.toInt()

    // ── Public API ───────────────────────────────────────────────────────

    /**
     * Generate a shareable fortune poster.
     *
     * @param context    Android context for resource loading
     * @param fortuneText The fortune / prediction text
     * @param hexagramName Name of the hexagram (e.g. "乾", "坤")
     * @param energyLevel  0.0 – 1.0 normalised energy bar value
     * @param date        Date string shown on the poster (defaults to today)
     */
    fun generateFortunePoster(
        context: Context,
        fortuneText: String,
        hexagramName: String,
        energyLevel: Float = 0.75f,
        date: String = LocalDate.now().format(
            DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINESE)
        )
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(BG_COLOR)

        // Decorative layers
        drawScanLines(canvas)
        drawCornerBrackets(canvas)

        // Content
        drawBinaryDate(canvas, date)
        drawTitle(canvas)
        drawDivider(canvas, 380f)
        drawHexagramBadge(canvas, hexagramName)
        drawEnergyBar(canvas, energyLevel)
        drawFortuneText(canvas, fortuneText)
        drawBranding(canvas)

        return bitmap
    }

    /**
     * Share the poster bitmap via a system chooser.
     */
    fun sharePoster(context: Context, bitmap: Bitmap) {
        val file = saveTempFile(context, bitmap)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "🔮 赛博算命 — 来自 CyberDiviner")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享算命结果"))
    }

    /**
     * Save the poster to the device gallery via MediaStore.
     */
    fun savePoster(context: Context, bitmap: Bitmap): Boolean {
        val filename = "cyberdiviner_${System.currentTimeMillis()}.png"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CyberDiviner")
            }
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            ) ?: return false
            context.contentResolver.openOutputStream(uri)?.use { os ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            }
            true
        } else {
            @Suppress("DEPRECATION")
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val subdir = File(dir, "CyberDiviner")
            subdir.mkdirs()
            val file = File(subdir, filename)
            FileOutputStream(file).use { os ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            }
            // Notify media scanner
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, android.net.Uri.fromFile(file))
            context.sendBroadcast(intent)
            true
        }
    }

    // ── Drawing helpers ──────────────────────────────────────────────────

    private fun drawScanLines(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(12, 0, 255, 204) // very faint cyan
            strokeWidth = 1f
        }
        var y = 0f
        while (y < HEIGHT) {
            canvas.drawLine(0f, y, WIDTH.toFloat(), y, paint)
            y += 4f
        }
    }

    private fun drawCornerBrackets(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NEON_CYAN
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        val len = 60f
        val inset = 40f
        // Top-left
        canvas.drawLine(inset, inset, inset + len, inset, paint)
        canvas.drawLine(inset, inset, inset, inset + len, paint)
        // Top-right
        canvas.drawLine(WIDTH - inset, inset, WIDTH - inset - len, inset, paint)
        canvas.drawLine(WIDTH - inset, inset, WIDTH - inset, inset + len, paint)
        // Bottom-left
        canvas.drawLine(inset, HEIGHT - inset, inset + len, HEIGHT - inset, paint)
        canvas.drawLine(inset, HEIGHT - inset, inset, HEIGHT - inset - len, paint)
        // Bottom-right
        canvas.drawLine(WIDTH - inset, HEIGHT - inset, WIDTH - inset - len, HEIGHT - inset, paint)
        canvas.drawLine(WIDTH - inset, HEIGHT - inset, WIDTH - inset, HEIGHT - inset - len, paint)
    }

    private fun drawBinaryDate(canvas: Canvas, date: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_SECONDARY
            textSize = 28f
            typeface = Typeface.MONOSPACE
        }
        // Render date in a binary-ish style
        val binary = date.toByteArray().joinToString("") { "%08d".format(it.toString(2).toInt()) }
        val truncated = binary.take(64) // limit width
        canvas.drawText(truncated, PAD.toFloat(), 120f, paint)

        // Human-readable below
        paint.apply {
            color = TEXT_SECONDARY
            textSize = 26f
        }
        canvas.drawText(date, PAD.toFloat(), 155f, paint)
    }

    private fun drawTitle(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 96f
            typeface = Typeface.DEFAULT_BOLD
            shader = LinearGradient(
                0f, 200f, 0f, 320f,
                NEON_CYAN, NEON_MAGENTA, Shader.TileMode.CLAMP
            )
        }
        canvas.drawText("赛博算命", PAD.toFloat(), 310f, paint)
    }

    private fun drawDivider(canvas: Canvas, y: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                PAD.toFloat(), y, WIDTH - PAD.toFloat(), y,
                Color.TRANSPARENT, NEON_CYAN, Shader.TileMode.CLAMP
            )
            strokeWidth = 2f
        }
        canvas.drawLine(PAD.toFloat(), y, (WIDTH - PAD).toFloat(), y, paint)
    }

    private fun drawHexagramBadge(canvas: Canvas, hexagramName: String) {
        val cx = WIDTH / 2f
        val cy = 520f

        // Outer glow circle
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(40, 0, 255, 204)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, 120f, glowPaint)

        // Ring
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NEON_CYAN
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawCircle(cx, cy, 100f, ringPaint)

        // Hexagram name
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = FORTUNE_GOLD
            textSize = 80f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(hexagramName, cx, textY, textPaint)

        // Label
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_SECONDARY
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText("HEXAGRAM // 卦", cx, cy + 130f, labelPaint)
    }

    private fun drawEnergyBar(canvas: Canvas, level: Float) {
        val barY = 720f
        val barHeight = 20f
        val barWidth = (WIDTH - PAD * 2).toFloat()

        // Background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = SURFACE_COLOR
        }
        val rect = RectF(PAD.toFloat(), barY, PAD + barWidth, barY + barHeight)
        canvas.drawRoundRect(rect, 10f, 10f, bgPaint)

        // Filled portion
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                PAD.toFloat(), barY, PAD + barWidth * level, barY,
                NEON_GREEN, NEON_CYAN, Shader.TileMode.CLAMP
            )
        }
        val fillRect = RectF(PAD.toFloat(), barY, PAD + barWidth * level, barY + barHeight)
        canvas.drawRoundRect(fillRect, 10f, 10f, fillPaint)

        // Labels
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_SECONDARY
            textSize = 22f
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText("ENERGY_LEVEL", PAD.toFloat(), barY - 10f, labelPaint)
        canvas.drawText("${(level * 100).toInt()}%", PAD + barWidth - 80f, barY - 10f, labelPaint)
    }

    private fun drawFortuneText(canvas: Canvas, text: String) {
        val x = PAD.toFloat()
        var y = 860f

        // Section header
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = NEON_MAGENTA
            textSize = 30f
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText("▸ FORTUNE_OUTPUT // 命运输出", x, y, headerPaint)
        y += 50f

        // Fortune text with word wrap
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_PRIMARY
            textSize = 42f
            typeface = Typeface.DEFAULT
            letterSpacing = 0.05f
        }
        val maxWidth = (WIDTH - PAD * 2).toFloat()
        val lines = breakText(text, textPaint, maxWidth)
        for (line in lines) {
            canvas.drawText(line, x, y, textPaint)
            y += 60f
            if (y > HEIGHT - 300f) break // don't overflow
        }

        // Subtle scan-line noise overlay on text area
        val noisePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(8, 0, 255, 0)
            strokeWidth = 1f
        }
        var ny = 850f
        while (ny < y + 20) {
            canvas.drawLine(x, ny, WIDTH - PAD.toFloat(), ny, noisePaint)
            ny += 3f
        }
    }

    private fun drawBranding(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEXT_SECONDARY
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText("⚡ CYBERDIVINER v1.0 ⚡", WIDTH / 2f, HEIGHT - 100f, paint)
        paint.apply {
            textSize = 20f
            color = Color.argb(80, 136, 136, 170)
        }
        canvas.drawText("赛博算命 · 数字命理 · AI占卜", WIDTH / 2f, HEIGHT - 70f, paint)
    }

    // ── Utility ──────────────────────────────────────────────────────────

    private fun breakText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()
        for (char in words) {
            val test = currentLine.toString() + char
            if (paint.measureText(test) > maxWidth) {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(char.toString())
            } else {
                currentLine.append(char)
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        return lines
    }

    private fun saveTempFile(context: Context, bitmap: Bitmap): File {
        val file = File(context.cacheDir, "poster_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { os ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
        }
        return file
    }
}
