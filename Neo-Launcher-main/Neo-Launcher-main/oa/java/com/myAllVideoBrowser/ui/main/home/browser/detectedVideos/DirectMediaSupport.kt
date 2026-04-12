package com.myAllVideoBrowser.ui.main.home.browser.detectedVideos

import com.myAllVideoBrowser.util.FileNameCleaner
import com.myAllVideoBrowser.util.MediaFormatSupport

object DirectMediaSupport {
    private val ignoredRegularAssetRegex = Regex(
        "^(.*\\.(apk|html|xml|ico|css|js|json|woff|woff2|m3u8|mpd|ts|php|ttf|otf|eot|cur|psd|ai|eps|pdf|doc|docx|xls|xlsx|ppt|pptx|csv|md|rtf|vtt|srt|swf|jar|log|txt|m4s))?$",
        RegexOption.IGNORE_CASE
    )

    private val imageMimeExtensions = mapOf(
        "image/jpeg" to "jpg",
        "image/jpg" to "jpg",
        "image/png" to "png",
        "image/gif" to "gif",
        "image/webp" to "webp",
        "image/bmp" to "bmp"
    )

    fun shouldIgnoreRegularUrl(url: String): Boolean {
        val cleanUrl = url.substringBefore('?').trim()
        return ignoredRegularAssetRegex.containsMatchIn(cleanUrl)
    }

    fun inferExtension(url: String, contentType: String?, fallbackExt: String): String {
        val normalizedContentType = contentType?.substringBefore(';')?.trim()?.lowercase()
        val mappedFromContentType = normalizedContentType?.let { imageMimeExtensions[it] }
        if (!mappedFromContentType.isNullOrBlank()) {
            return mappedFromContentType
        }

        val urlExtension = url.substringBefore('?')
            .substringAfterLast('/', "")
            .substringAfterLast('.', "")
            .lowercase()
            .trim()

        if (urlExtension.isNotBlank()) {
            return urlExtension
        }

        return fallbackExt.lowercase()
    }

    fun deriveTitle(url: String, fallbackTitle: String): String {
        val fileName = url.substringBefore('?').substringAfterLast('/', "")
        val withoutExtension = fileName.substringBeforeLast('.', fileName)
        val candidate = withoutExtension.ifBlank { fallbackTitle.ifBlank { "downloaded_media" } }
        return FileNameCleaner.cleanFileName(candidate)
    }

    fun isImageExtension(ext: String): Boolean {
        return MediaFormatSupport.isImageExtension(ext)
    }
}
