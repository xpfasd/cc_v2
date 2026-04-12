package com.myAllVideoBrowser.util

object MediaFormatSupport {
    val videoExtensions = setOf("mp4", "mkv", "webm", "mov", "m4v")
    val audioExtensions = setOf("mp3", "m4a", "aac", "wav", "ogg")
    val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp")

    val downloadedMediaExtensions = videoExtensions + audioExtensions + imageExtensions

    fun normalizeExtension(extension: String): String = extension.lowercase()

    fun isVideoExtension(extension: String): Boolean = normalizeExtension(extension) in videoExtensions

    fun isAudioExtension(extension: String): Boolean = normalizeExtension(extension) in audioExtensions

    fun isImageExtension(extension: String): Boolean = normalizeExtension(extension) in imageExtensions
}
