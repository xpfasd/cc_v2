package com.myAllVideoBrowser.ui.main.home.browser.detectedVideos

import com.myAllVideoBrowser.data.local.room.entity.ProgressInfo
import com.myAllVideoBrowser.util.downloaders.generic_downloader.models.VideoTaskState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectedDownloadSessionRuleTest {

    @Test
    fun `current session download requires requested marker`() {
        val progressInfo = ProgressInfo(
            videoInfo = com.myAllVideoBrowser.data.local.room.entity.VideoInfo(),
            downloadStatus = VideoTaskState.SUCCESS
        )

        assertFalse(
            isDownloadedInCurrentSession(
                requestedThisSession = false,
                downloadedFileExists = true,
                progressInfo = progressInfo
            )
        )
    }

    @Test
    fun `current session download is done when requested and file exists`() {
        assertTrue(
            isDownloadedInCurrentSession(
                requestedThisSession = true,
                downloadedFileExists = true,
                progressInfo = null
            )
        )
    }
}
