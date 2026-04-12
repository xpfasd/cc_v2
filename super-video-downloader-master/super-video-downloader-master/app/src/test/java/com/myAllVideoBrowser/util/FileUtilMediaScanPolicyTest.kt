package com.myAllVideoBrowser.util

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileUtilMediaScanPolicyTest {

    @Test
    fun `downloaded media scan accepts supported video formats`() {
        assertTrue(isDownloadedMediaFile(File("movie.mp4")))
        assertTrue(isDownloadedMediaFile(File("clip.webm")))
        assertTrue(isDownloadedMediaFile(File("sample.MKV")))
    }

    @Test
    fun `downloaded media scan accepts supported image and audio formats`() {
        assertTrue(isDownloadedMediaFile(File("image.jpg")))
        assertTrue(isDownloadedMediaFile(File("cover.WEBP")))
        assertTrue(isDownloadedMediaFile(File("track.m4a")))
    }

    @Test
    fun `downloaded media scan rejects unsupported files`() {
        assertFalse(isDownloadedMediaFile(File("notes.txt")))
        assertFalse(isDownloadedMediaFile(File("archive.zip")))
    }
}
