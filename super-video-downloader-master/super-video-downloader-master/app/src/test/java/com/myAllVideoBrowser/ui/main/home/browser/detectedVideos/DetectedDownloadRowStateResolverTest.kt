package com.myAllVideoBrowser.ui.main.home.browser.detectedVideos

import com.myAllVideoBrowser.data.local.room.entity.ProgressInfo
import com.myAllVideoBrowser.data.local.room.entity.VideoInfo
import com.myAllVideoBrowser.util.downloaders.generic_downloader.models.VideoTaskState
import org.junit.Assert.assertEquals
import org.junit.Test

class DetectedDownloadRowStateResolverTest {

    @Test
    fun `success progress without current session download is treated as downloadable`() {
        val videoInfo = VideoInfo()
        val progressInfo = ProgressInfo(
            videoInfo = videoInfo,
            downloadStatus = VideoTaskState.SUCCESS
        )

        val state = DetectedDownloadRowStateResolver.resolve(
            videoInfo = videoInfo,
            progressInfo = progressInfo,
            isDownloaded = false
        )

        assertEquals(DetectedDownloadActionState.DOWNLOAD, state.actionState)
    }

    @Test
    fun `downloaded file keeps done state`() {
        val state = DetectedDownloadRowStateResolver.resolve(
            videoInfo = VideoInfo(),
            progressInfo = null,
            isDownloaded = true
        )

        assertEquals(DetectedDownloadActionState.DONE, state.actionState)
    }
}
