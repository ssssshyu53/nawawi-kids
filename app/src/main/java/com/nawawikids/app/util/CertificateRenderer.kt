package com.nawawikids.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.nawawikids.app.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates the personalized Arabic completion certificate bitmap onto assets/certificate_template.jpg
 * using exact pill-box coordinates from certificate.php and native Android RTL Arabic shaping.
 */
object CertificateRenderer {

    fun renderCertificateBitmap(
        context: Context,
        rawStudentName: String,
        dateString: String = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())
    ): Bitmap? {
        val baseBitmap = try {
            context.assets.open("certificate_template.jpg").use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) {
            null
        } ?: return null

        val mutable = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
        baseBitmap.recycle()

        val canvas = Canvas(mutable)
        val scaleX = mutable.width / 1024f
        val scaleY = mutable.height / 764f

        val whitePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        // Mask the placeholder dots inside the 3 pill boxes cleanly
        canvas.drawRect(276f * scaleX, 392f * scaleY, 691f * scaleX, 428f * scaleY, whitePaint)
        canvas.drawRect(662f * scaleX, 646f * scaleY, 796f * scaleX, 678f * scaleY, whitePaint)
        canvas.drawRect(145f * scaleX, 646f * scaleY, 286f * scaleX, 678f * scaleY, whitePaint)

        val typeface = try {
            Typeface.createFromAsset(context.assets, "fonts/Amiri-Bold.ttf")
        } catch (_: Exception) {
            ResourcesCompat.getFont(context, R.font.amiri) ?: Typeface.DEFAULT_BOLD
        }

        val cleanName = rawStudentName.trim().take(50).ifBlank { "الطالب المتفوق" }

        drawFittedCenteredText(
            canvas = canvas,
            text = cleanName,
            typeface = typeface,
            centerX = 483f * scaleX,
            baselineY = 418f * scaleY,
            maxWidth = 386f * scaleX,
            maxSizePx = 32f * scaleY,
            minSizePx = 16f * scaleY,
            color = Color.rgb(24, 52, 74)
        )

        drawFittedCenteredText(
            canvas = canvas,
            text = dateString,
            typeface = typeface,
            centerX = 729f * scaleX,
            baselineY = 668f * scaleY,
            maxWidth = 122f * scaleX,
            maxSizePx = 20f * scaleY,
            minSizePx = 13f * scaleY,
            color = Color.rgb(82, 54, 34)
        )

        drawFittedCenteredText(
            canvas = canvas,
            text = "الأربعون النووية",
            typeface = typeface,
            centerX = 215f * scaleX,
            baselineY = 668f * scaleY,
            maxWidth = 128f * scaleX,
            maxSizePx = 20f * scaleY,
            minSizePx = 13f * scaleY,
            color = Color.rgb(15, 118, 102)
        )

        return mutable
    }

    private fun drawFittedCenteredText(
        canvas: Canvas,
        text: String,
        typeface: Typeface,
        centerX: Float,
        baselineY: Float,
        maxWidth: Float,
        maxSizePx: Float,
        minSizePx: Float,
        color: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.typeface = typeface
            textAlign = Paint.Align.CENTER
        }
        var size = maxSizePx
        val bounds = Rect()
        while (size >= minSizePx) {
            paint.textSize = size
            paint.getTextBounds(text, 0, text.length, bounds)
            if (bounds.width() <= maxWidth) break
            size -= 1f
        }
        canvas.drawText(text, centerX, baselineY, paint)
    }

    fun saveAndGetShareUri(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val dir = File(context.cacheDir, "certificates").apply { mkdirs() }
            val file = File(dir, "nawawi_kids_certificate.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: Exception) {
            null
        }
    }
}
