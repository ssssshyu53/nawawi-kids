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
 * Generates the personalized Arabic completion certificate bitmap onto assets/certificate_template.png
 * (native 1451x1084 artwork) with optically centered Name, Date, and Signature ("منصة أبطال السنة").
 */
object CertificateRenderer {

    const val TEMPLATE_ASPECT_RATIO = 1451f / 1084f
    private const val SIGNATURE_LABEL = "منصة أبطال السنة"

    fun renderCertificateBitmap(
        context: Context,
        rawStudentName: String,
        dateString: String = SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())
    ): Bitmap? {
        val baseBitmap = try {
            context.assets.open("certificate_template.png").use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (_: Exception) {
            null
        } ?: return null

        val mutable = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
        baseBitmap.recycle()

        val canvas = Canvas(mutable)
        val scaleX = mutable.width / 1451f
        val scaleY = mutable.height / 1084f

        // Gently blend out the placeholder dotted lines in the Date & Signature cloud boxes
        // using each cloud's exact sampled ivory-cream fill so Arabic text sits cleanly without dot clash.
        val dateCloudMaskPaint = Paint().apply {
            color = Color.rgb(254, 250, 232)
            style = Paint.Style.FILL
        }
        canvas.drawRect(310f * scaleX, 993f * scaleY, 526f * scaleX, 1005f * scaleY, dateCloudMaskPaint)

        val signatureCloudMaskPaint = Paint().apply {
            color = Color.rgb(253, 248, 230)
            style = Paint.Style.FILL
        }
        canvas.drawRect(844f * scaleX, 993f * scaleY, 1066f * scaleX, 1005f * scaleY, signatureCloudMaskPaint)

        val nameTypeface = try {
            ResourcesCompat.getFont(context, R.font.tajawal_extrabold)
                ?: ResourcesCompat.getFont(context, R.font.amiri_bold)
                ?: Typeface.DEFAULT_BOLD
        } catch (_: Exception) {
            Typeface.DEFAULT_BOLD
        }

        val detailTypeface = try {
            ResourcesCompat.getFont(context, R.font.tajawal_bold)
                ?: nameTypeface
        } catch (_: Exception) {
            nameTypeface
        }

        val cleanName = rawStudentName.trim().take(50).ifBlank { "الطالب المتفوق" }
        val arabicDate = ArabicUtils.toArabicDigits(dateString)

        // 1. Student Name inside the central dashed orange pill (x=308..1068 clear of pencil, y=303..387)
        drawFittedVerticallyCenteredText(
            canvas = canvas,
            text = cleanName,
            typeface = nameTypeface,
            centerX = 695f * scaleX,
            centerY = 345f * scaleY,
            maxWidth = 680f * scaleX,
            maxSizePx = 50f * scaleY,
            minSizePx = 22f * scaleY,
            color = Color.rgb(30, 58, 95)
        )

        // 2. Date inside the bottom-left cloud ("التّاريخ:" is on the right at x=532..611; dots x=313..524)
        drawFittedVerticallyCenteredText(
            canvas = canvas,
            text = arabicDate,
            typeface = detailTypeface,
            centerX = 418f * scaleX,
            centerY = 988f * scaleY,
            maxWidth = 196f * scaleX,
            maxSizePx = 29f * scaleY,
            minSizePx = 16f * scaleY,
            color = Color.rgb(68, 45, 25)
        )

        // 3. Signature inside the bottom-right cloud ("التّوقيع:" + quill on the right at x=1072..1239; dots x=848..1064)
        drawFittedVerticallyCenteredText(
            canvas = canvas,
            text = SIGNATURE_LABEL,
            typeface = detailTypeface,
            centerX = 955f * scaleX,
            centerY = 988f * scaleY,
            maxWidth = 206f * scaleX,
            maxSizePx = 28f * scaleY,
            minSizePx = 15f * scaleY,
            color = Color.rgb(15, 118, 110)
        )

        return mutable
    }

    private fun drawFittedVerticallyCenteredText(
        canvas: Canvas,
        text: String,
        typeface: Typeface,
        centerX: Float,
        centerY: Float,
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
        val fm = paint.fontMetrics
        val baselineY = centerY - (fm.ascent + fm.descent) / 2f
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
