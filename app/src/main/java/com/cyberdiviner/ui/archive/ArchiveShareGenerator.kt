package com.cyberdiviner.ui.archive

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
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.FileProvider
import com.cyberdiviner.R
import java.io.File
import java.io.FileOutputStream

internal object ArchiveShareGenerator {
    private const val WIDTH = 1080
    private const val HEIGHT = 760
    private const val PAD = 86f

    private const val BLACK = Color.BLACK
    private const val WHITE = Color.WHITE
    private const val RED = 0xFFCC3333.toInt()
    private const val GRAY = 0xFFB8B8B8.toInt()
    private const val MUTED = 0xFF6F6F6F.toInt()
    private const val LINE = 0xFF2A2A2A.toInt()

    fun generate(context: Context, entry: ArchiveEntry): Bitmap {
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(BLACK)

        drawFrame(canvas)
        drawHeader(canvas, entry)
        drawTitle(canvas, context, entry)
        drawFooter(canvas, entry)

        return bitmap
    }

    fun saveToGallery(context: Context, bitmap: Bitmap): Boolean {
        val filename = "cyberdiviner_archive_${System.currentTimeMillis()}.png"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CyberDiviner")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
            context.contentResolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            true
        } else {
            @Suppress("DEPRECATION")
            val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val dir = File(root, "CyberDiviner")
            dir.mkdirs()
            val file = File(dir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, android.net.Uri.fromFile(file)))
            true
        }
    }

    fun share(context: Context, bitmap: Bitmap, entry: ArchiveEntry) {
        val dir = File(context.cacheDir, "shared_images")
        dir.mkdirs()
        val file = File(dir, "cyberdiviner_${entry.id}_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "因果命簿 · ${entry.title}\n${entry.interpretation}\n\nCyberDiviner")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享因果卡片"))
    }

    private fun drawFrame(canvas: Canvas) {
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = LINE
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(48f, 48f, WIDTH - 48f, HEIGHT - 48f, line)
        line.color = RED
        line.strokeWidth = 4f
        canvas.drawLine(PAD, 222f, 330f, 222f, line)
        val tick = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF111111.toInt()
            strokeWidth = 1f
        }
        var y = 0f
        while (y < HEIGHT) {
            canvas.drawLine(0f, y, WIDTH.toFloat(), y, tick)
            y += 6f
        }
    }

    private fun drawHeader(canvas: Canvas, entry: ArchiveEntry) {
        val mono = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = MUTED
            textSize = 30f
            letterSpacing = 0.12f
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText("CAUSAL LEDGER", PAD, 150f, mono)
        canvas.drawText(entry.solarDate, PAD, 196f, mono)
    }

    private fun drawTitle(canvas: Canvas, context: Context, entry: ArchiveEntry) {
        val huiwen = ResourcesCompat.getFont(context, R.font.huiwen_mingchao)
            ?: Typeface.create(Typeface.SERIF, Typeface.BOLD)
        val wenkai = ResourcesCompat.getFont(context, R.font.lxgw_wenkai_regular)
            ?: Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = WHITE
            textSize = if (entry.title.length <= 4) 112f else 82f
            typeface = Typeface.create(huiwen, Typeface.BOLD)
        }
        canvas.drawText(entry.title, PAD, 370f, paint)
        val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = GRAY
            textSize = 38f
            typeface = wenkai
        }
        drawWrappedText(canvas, entry.interpretation, PAD, 460f, WIDTH - PAD, sub, 64f, maxLines = 3)
    }

    private fun drawFooter(canvas: Canvas, entry: ArchiveEntry) {
        val mono = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = MUTED
            textSize = 28f
            typeface = Typeface.MONOSPACE
        }
        canvas.drawText(entry.ganzhiDate, PAD, HEIGHT - 190f, mono)
        canvas.drawText(entry.hash, PAD, HEIGHT - 145f, mono)
        mono.textAlign = Paint.Align.RIGHT
        mono.color = WHITE
        canvas.drawText("CYBERDIVINER", WIDTH - PAD, HEIGHT - 145f, mono)
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        left: Float,
        top: Float,
        right: Float,
        paint: Paint,
        lineHeight: Float,
        maxLines: Int
    ) {
        val width = (right - left).toInt()
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (char in text) {
            val next = current.toString() + char
            if (paint.measureText(next) > width && current.isNotEmpty()) {
                lines.add(current.toString())
                current = StringBuilder(char.toString())
            } else {
                current.append(char)
            }
            if (lines.size >= maxLines) break
        }
        if (lines.size < maxLines && current.isNotBlank()) lines.add(current.toString())

        lines.take(maxLines).forEachIndexed { index, line ->
            val output = if (index == maxLines - 1 && lines.size >= maxLines && textRemaining(text, lines)) {
                line.trimEnd().let { if (it.length > 3) it.dropLast(2) + "..." else it }
            } else {
                line
            }
            canvas.drawText(output, left, top + index * lineHeight, paint)
        }
    }

    private fun textRemaining(original: String, lines: List<String>): Boolean {
        return lines.joinToString("").length < original.length
    }
}
