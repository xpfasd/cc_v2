package com.myAllVideoBrowser.ui.main.home.browser.webTab

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.webkit.WebView
import java.io.ByteArrayOutputStream
import kotlin.math.min

object WebTabPreviewCapture {
    private const val DEFAULT_WIDTH_PX = 360
    private const val DEFAULT_HEIGHT_PX = 240
    private const val JPEG_QUALITY = 72

    fun capture(
        webView: WebView?,
        targetWidthPx: Int = DEFAULT_WIDTH_PX,
        targetHeightPx: Int = DEFAULT_HEIGHT_PX
    ): ByteArray? {
        if (webView == null || targetWidthPx <= 0 || targetHeightPx <= 0) {
            return null
        }

        val sourceWidth = webView.width
        val sourceHeight = webView.height
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            return null
        }

        return runCatching {
            val bitmap = Bitmap.createBitmap(
                targetWidthPx,
                targetHeightPx,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            val scale = min(
                targetWidthPx / sourceWidth.toFloat(),
                targetHeightPx / sourceHeight.toFloat()
            )
            val scaledWidth = sourceWidth * scale
            val scaledHeight = sourceHeight * scale
            val dx = (targetWidthPx - scaledWidth) / 2f
            val dy = (targetHeightPx - scaledHeight) / 2f

            canvas.save()
            canvas.translate(dx, dy)
            canvas.scale(scale, scale)
            webView.draw(canvas)
            canvas.restore()

            ByteArrayOutputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
                output.toByteArray()
            }
        }.getOrNull()
    }
}
