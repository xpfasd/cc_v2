package com.myAllVideoBrowser.util

import java.io.File
import kotlin.io.path.createTempDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileUtilManagedFolderScanTest {

    @Test
    fun `collectManagedMediaFiles reads supported media from target folder only`() {
        val rootDir = createTempDirectory("managed-download-scan").toFile()
        val nestedDir = File(rootDir, "VideoDownloader").apply { mkdirs() }
        File(rootDir, "root.mp4").writeText("root")
        File(rootDir, "poster.jpg").writeText("poster")
        File(rootDir, "notes.txt").writeText("ignore")
        File(nestedDir, "nested.webm").writeText("nested")

        val found = collectManagedMediaFiles(rootDir)

        assertEquals(2, found.size)
        assertTrue(found.any { it.name == "root.mp4" })
        assertTrue(found.any { it.name == "poster.jpg" })
        assertFalse(found.any { it.name == "nested.webm" })
    }

    @Test
    fun `resolveManagedPublicDownloadDir appends app folder name inside downloads`() {
        val downloadsRoot = File("/storage/emulated/0/Download")

        val result = resolveManagedPublicDownloadDir(downloadsRoot)

        assertEquals(File("/storage/emulated/0/Download/SuperX"), result)
    }

    @Test
    fun `managedDownloadRelativePath points to downloads app folder`() {
        assertEquals("Download/SuperX", managedDownloadRelativePath())
    }
}
