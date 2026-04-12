package com.myAllVideoBrowser.ui.main.video

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoFragmentSourceSafetyTest {

    @Test
    fun `video fragment removes pending ui sync callbacks when view is destroyed`() {
        val source = File(
            "src/main/java/com/myAllVideoBrowser/ui/main/video/VideoFragment.kt"
        ).readText()

        assertTrue(source.contains("private val syncListUiRunnable = Runnable"))
        assertTrue(source.contains("dataBinding.root.removeCallbacks(syncListUiRunnable)"))
    }

    @Test
    fun `syncListUi guards against detached fragment state`() {
        val source = File(
            "src/main/java/com/myAllVideoBrowser/ui/main/video/VideoFragment.kt"
        ).readText()

        val methodBody = source.substringAfter("private fun syncListUi() {")
            .substringBefore("\n    private fun toggleSelection(")

        assertTrue(methodBody.contains("if (!isAdded || view == null || !this::dataBinding.isInitialized)"))
    }
}
