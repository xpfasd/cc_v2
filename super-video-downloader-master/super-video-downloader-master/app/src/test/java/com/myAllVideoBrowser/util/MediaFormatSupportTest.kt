package com.myAllVideoBrowser.util

import org.junit.Assert.assertTrue
import org.junit.Test

class MediaFormatSupportTest {

    @Test
    fun `shared format support recognizes video audio and image extensions`() {
        assertTrue(MediaFormatSupport.isVideoExtension("webm"))
        assertTrue(MediaFormatSupport.isAudioExtension("m4a"))
        assertTrue(MediaFormatSupport.isImageExtension("png"))
    }

    @Test
    fun `downloaded media extension set includes all supported families`() {
        assertTrue(MediaFormatSupport.downloadedMediaExtensions.contains("mp4"))
        assertTrue(MediaFormatSupport.downloadedMediaExtensions.contains("mp3"))
        assertTrue(MediaFormatSupport.downloadedMediaExtensions.contains("jpg"))
    }
}
