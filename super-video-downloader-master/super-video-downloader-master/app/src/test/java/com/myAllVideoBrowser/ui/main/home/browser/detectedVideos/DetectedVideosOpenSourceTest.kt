package com.myAllVideoBrowser.ui.main.home.browser.detectedVideos

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectedVideosOpenSourceTest {

    @Test
    fun `openDownloaded delegates to safe media opener instead of exposing file uri`() {
        val source = File(
            "src/main/java/com/myAllVideoBrowser/ui/main/home/browser/detectedVideos/DetectedVideosTabFragment.kt"
        ).readText()

        val methodBody = source.substringAfter("private fun openDownloaded(videoInfo: VideoInfo) {")
            .substringBefore("\n    private val itemListener = object")

        assertTrue(
            "Detected videos open flow should delegate to IntentUtil for safe content uri handling",
            methodBody.contains("intentUtil.openMedia(")
        )
    }
}
