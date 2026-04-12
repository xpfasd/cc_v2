package com.myAllVideoBrowser.util

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class FileUtilListFilesSourceTest {

    @Test
    fun `listFiles exposes downloaded media objects instead of pair payloads`() {
        val source = File("src/main/java/com/myAllVideoBrowser/util/FileUtil.kt").readText()

        assertTrue(source.contains("data class DownloadedMediaFile("))
        assertTrue(source.contains("val listFiles: Map<String, DownloadedMediaFile>"))
    }
}
