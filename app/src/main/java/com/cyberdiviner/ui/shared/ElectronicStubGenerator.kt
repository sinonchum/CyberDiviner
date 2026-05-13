package com.cyberdiviner.ui.shared

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Generates strict black-and-white electronic stub (电子存根) PNG images
 * for divination results.
 *
 * Renders a 1080 × 1920 Canvas with:
 *  - Divination type (周易 / 塔罗 / 视界)
 *  - Timestamp (ISO-8601 + localised)
 *  - Soul hash (SHA-256 fragment)
 *  - Brief result summary
 *  - App branding (CYBERDIVINER)
 *
 * Strict B&W aesthetic — monospace font, Canvas API, no colour.
 */
object ElectronicStubGenerator {

    // ── Canvas dimensions (9:16 ratio) ──────────────────────────────────
    private const val WIDTH = 1080
    private const val HEIGHT = 1920
    private const val PAD = 80

    // ── B&W palette ─────────────────────────────────────────────────────
    private const val BLACK = Color.BLACK
    private const val WHITE = Color.WHITE
    private const val GRAY_DARK = 0xFF333333.toInt()
    private const val GRAY_MID = 0xFF666666.toInt()
    private const val GRAY_LIGHT = 0xFF999999.toInt()
    private const val GRAY_FAINTEST = 0xFF1A1A1A.toInt()

    // ── Divination types ────────────────────────────────────────────────
    enum class DivinationType(val label: String, val tag: String) {
        I_CHING("周易", "I_CHING"),
        TAROT("塔罗", "TAROT"),
        VISION("视界", "VISION")
    }

    /**
     * Generate a B&W electronic stub PNG.
     *
     * @param context        Android context for resource loading
     * @param type           Divination method used
     * @param resultSummary  Brief result text
     * @param soulHash       SHA-256 hash (or fragment) of the session
     * @param timestamp      Instant of the reading (defaults to now)
     * @return Bitmap ready for saving / sharing
     */
    fun generateStub(
        context: Context,
        type: DivinationType,
        resultSummary: String,
        soulHash: String,
        timestamp: Instant = Instant.now()
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // White background — inverted from PosterGenerator's dark BG
        canvas.drawColor(BLACK)

        // Decorative scan-lines
        drawScanLines(canvas)

        // Corner brackets
        drawCornerBrackets(canvas)

        // Content blocks
        drawTypeHeader(canvas, type)
        drawDivider(canvas, 320f)
        drawTimestampBlock(canvas, timestamp)
        drawDivider(canvas, 580f)
        drawSoulHash(canvas, soulHash)
        drawDivider(canvas, 740f)
        drawResultSummary(canvas, resultSummary)
        drawBranding(canvas)
        drawStubLabel(canvas)

        return bitmap
    }

    // ── Public: save / share ────────────────────────────────────────────

    /**
     * Save the stub to the device gallery via MediaStore.
     * Returns true on success.
     */
    fun saveStub(context: Context, bitmap: Bitmap): Boolean {
        val filename = "cyberdiviner_stub_${System.currentTimeMillis()}.png"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CyberDiviner/Stubs")
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
            val subdir = File(dir, "CyberDiviner/Stubs")
            subdir.mkdirs()
            val file = File(subdir, filename)
            FileOutputStream(file).use { os ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            }
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, android.net.Uri.fromFile(file))
            context.sendBroadcast(intent)
            true
        }
    }

    /**
     * Share the stub via the system share sheet.
     */
    fun shareStub(context: Context, bitmap: Bitmap) {
        val file = saveTempFile(context, bitmap)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "🔮 电子存根 — CyberDiviner")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享电子存根"))
    }

    // ── Drawing helpers ─────────────────────────────────────────────────

    /**
     * Horizontal scan-line texture over the full canvas.
     */
    private fun drawScanLines(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GRAY_FAINTEST
            strokeWidth = 1f
        }
        var y = 0f
        while (y < HEIGHT) {
            canvas.drawLine(0f, y, WIDTH.toFloat(), y, paint)
            y += 4f
        }
    }

    /**
     * Corner bracket marks — identical layout to PosterGenerator but B&W.
     */
    private fun drawCornerBrackets(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = WHITE
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val len = 50f
        val inset = 36f
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

    /**
     * Top section: "ELECTRONIC STUB // 电子存根" label + divination type badge.
     */
    private fun drawTypeHeader(canvas: Canvas, type: DivinationType) {
        // Document label
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GRAY_MID
            textSize = 26f
            typeface = Typeface.MONOSPACE
            letterSpacing = 0.15f
        }
        canvas.drawText("ELECTRONIC_STUB // 电子存根", PAD.toFloat(), 100f, labelPaint)

        // Divination type — large monospace
        val typePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = WHITE
            textSize = 96f
            typeface = Typeface.MONOSPACE
            isFakeBoldText = true
        }
        canvas.drawText(type.label, PAD.toFloat(), 240f, typePaint)

        // Type tag in monospace
        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GRAY_MID
            textSize = 28f
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText("[ ${type.tag} ]", PAD.toFloat(), 285f, tagPaint)
    }

    /**
     * Thin horizontal divider line.
     */
    private fun drawDivider(canvas: Canvas, y: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GRAY_DARK
            strokeWidth = 1f
        }
        canvas.drawLine(PAD.toFloat(), y, (WIDTH - PAD).toFloat(), y, paint)
    }

    /**
     * Timestamp block — ISO-8601 + localised date/time.
     */
    private fun drawTimestampBlock(canvas: Canvas, timestamp: Instant) {
        val zone = ZoneId.systemDefault()
        val local = timestamp.atZone(zone)

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GRAY_MID
            textSize = 24f
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText("TIMESTAMP // 时间戳", PAD.toFloat(), 380f, headerPaint)

        // ISO-8601
        val isoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = WHITE
            textSize = 32f
            typeface = Typeface.MONOSPACE
        }
        val iso = timestamp.toString()
        canvas.drawText(iso, PAD.toFloat(), 425f, isoPaint)

        // Localised
        val localPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GRAY_LIGHT
            textSize = 28f
            typeface = Typeface.MONOSPACE
        }
        val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm:ss", Locale.CHINESE)
        val localised = local.format(formatter)
        canvas.drawText(localised, PAD.toFloat(), 470f, localPaint)

        // Unix epoch
        val epochPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GRAY_MID
            textSize = 22f
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText("epoch: ${timestamp.epochSecond}", PAD.toFloat(), 510f, epochPaint)
    }

    /**
     * Soul hash display — monospace, truncated for visual layout.
     */
    private fun drawSoulHash(canvas: Canvas, soulHash: String) {
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GRAY_MID
            textSize = 24f
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText("SOUL_HASH // 魂灵哈希", PAD.toFloat(), 640f, headerPaint)

        val hashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = WHITE
            textSize = 30f
            typeface = Typeface.MONOSPACE
            letterSpacing = 0.05f
        }

        // Show hash in two rows for readability (max 32 chars per row)
        val displayHash = if (soulHash.length > 32) {
            soulHash.uppercase(Locale.ROOT)
        } else {
            soulHash.uppercase(Locale.ROOT).padEnd(64, '0')
        }

        val row1 = displayHash.substring(0, 32)
        val row2 = displayHash.substring(32, minOf(64, displayHash.length))

        canvas.drawText(row1, PAD.toFloat(), 690f, hashPaint)
        canvas.drawText(row2, PAD.toFloat(), 730f, hashPaint)
    }

    /**
     * Result summary — main content area with word-wrapping.
     */
    private fun drawResultSummary(canvas: Canvas, text: String) {
        val x = PAD.toFloat()
        var y = 840f

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GRAY_MID
            textSize = 24f
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText("RESULT // 占卜结果", x, y, headerPaint)
        y += 50f

        // Result text — white on black, monospace for B&W aesthetic
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = WHITE
            textSize = 38f
            typeface = Typeface.MONOSPACE
            letterSpacing = 0.03f
        }
        val maxWidth = (WIDTH - PAD * 2).toFloat()
        val lines = breakText(text, textPaint, maxWidth)
        for (line in lines) {
            canvas.drawText(line, x, y, textPaint)
            y += 55f
            if (y > HEIGHT - 350f) break
        }

        // Scan-line noise over text area (B&W)
        val noisePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GRAY_FAINTEST
            strokeWidth = 1f
        }
        var ny = 830f
        while (ny < y + 20) {
            canvas.drawLine(x, ny, (WIDTH - PAD).toFloat(), ny, noisePaint)
            ny += 3f
        }
    }

    /**
     * Bottom branding bar.
     */
    private fun drawBranding(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = WHITE
            textSize = 26f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.MONOSPACE
            isFakeBoldText = true
        }
        canvas.drawText("⚡ CYBERDIVINER ⚡", WIDTH / 2f, HEIGHT - 110f, paint)

        paint.apply {
            textSize = 20f
            color = GRAY_MID
            isFakeBoldText = false
        }
        canvas.drawText("赛博算命 · 电子存根 · v5.0", WIDTH / 2f, HEIGHT - 80f, paint)
    }

    /**
     * "STUB" watermark label in the bottom-left area.
     */
    private fun drawStubLabel(canvas: Canvas) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GRAY_DARK
            textSize = 18f
            typeface = Typeface.MONOSPACE
            letterSpacing = 0.3f
        }
        canvas.drawText("STUB_ID: ${System.currentTimeMillis()}", PAD.toFloat(), HEIGHT - 40f, paint)

        val rightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GRAY_DARK
            textSize = 18f
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.RIGHT
            letterSpacing = 0.3f
        }
        canvas.drawText("VALIDATED", (WIDTH - PAD).toFloat(), HEIGHT - 40f, rightPaint)
    }

    // ── Utility ──────────────────────────────────────────────────────────

    /**
     * Character-level text wrapping (CJK-safe).
     */
    private fun breakText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()
        for (char in text) {
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
        val file = File(context.cacheDir, "stub_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { os ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
        }
        return file
    }
}
