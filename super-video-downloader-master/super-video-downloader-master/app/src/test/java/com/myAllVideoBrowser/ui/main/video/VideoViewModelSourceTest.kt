package com.myAllVideoBrowser.ui.main.video

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoViewModelSourceTest {

    @Test
    fun `refreshVideos performs file scanning off the main thread`() {
        val source = File(
            "src/main/java/com/myAllVideoBrowser/ui/main/video/VideoViewModel.kt"
        ).readText()

        val methodBody = source.substringAfter("fun refreshVideos() {")
            .substringBefore("\n    private fun getFilesList(): List<LocalVideo> {")

        assertTrue(
            "refreshVideos should dispatch work to a background coroutine",
            methodBody.contains("viewModelScope.launch(Dispatchers.IO)")
        )
        assertTrue(
            "refreshVideos should post the loaded list back on the main thread",
            methodBody.contains("withContext(Dispatchers.Main)")
        )
    }
}
