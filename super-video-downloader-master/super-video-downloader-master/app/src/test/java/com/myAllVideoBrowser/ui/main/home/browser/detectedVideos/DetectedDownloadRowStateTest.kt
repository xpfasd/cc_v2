package com.myAllVideoBrowser.ui.main.home.browser.detectedVideos

import com.myAllVideoBrowser.data.local.room.entity.ProgressInfo
import com.myAllVideoBrowser.data.local.room.entity.VideFormatEntityList
import com.myAllVideoBrowser.data.local.room.entity.VideoFormatEntity
import com.myAllVideoBrowser.data.local.room.entity.VideoInfo
import com.myAllVideoBrowser.util.downloaders.generic_downloader.models.VideoTaskState
import org.junit.Assert.assertEquals
import org.junit.Test

class DetectedDownloadRowStateTest {

    @Test
    fun `ready candidate maps to download action`() {
        val state = DetectedDownloadRowStateResolver.resolve(
            videoInfo = createVideoInfo("sample.jpg", "jpg"),
            progressInfo = null,
            isDownloaded = false
        )

        assertEquals(DetectedDownloadActionState.DOWNLOAD, state.actionState)
    }

    @Test
    fun `paused progress maps to start action`() {
        val videoInfo = createVideoInfo("movie.mp4", "mp4")
        val progressInfo = ProgressInfo(
            id = videoInfo.id,
            downloadId = 10L,
            videoInfo = videoInfo,
            progressDownloaded = 512L,
            progressTotal = 1024L,
            downloadStatus = VideoTaskState.PAUSE
        )

        val state = DetectedDownloadRowStateResolver.resolve(
            videoInfo = videoInfo,
            progressInfo = progressInfo,
            isDownloaded = false
        )

        assertEquals(DetectedDownloadActionState.START, state.actionState)
    }

    @Test
    fun `successful progress without current session download maps to download action`() {
        val videoInfo = createVideoInfo("movie.mp4", "mp4")
        val progressInfo = ProgressInfo(
            id = videoInfo.id,
            downloadId = 11L,
            videoInfo = videoInfo,
            progressDownloaded = 1024L,
            progressTotal = 1024L,
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
    fun `failed progress maps to fail action`() {
        val videoInfo = createVideoInfo("movie.mp4", "mp4")
        val progressInfo = ProgressInfo(
            id = videoInfo.id,
            downloadId = 12L,
            videoInfo = videoInfo,
            progressDownloaded = 256L,
            progressTotal = 1024L,
            downloadStatus = VideoTaskState.ERROR
        )

        val state = DetectedDownloadRowStateResolver.resolve(
            videoInfo = videoInfo,
            progressInfo = progressInfo,
            isDownloaded = false
        )

        assertEquals(DetectedDownloadActionState.FAIL, state.actionState)
    }

    private fun createVideoInfo(name: String, ext: String): VideoInfo {
        val title = name.substringBeforeLast('.')
        return VideoInfo(
            id = name,
            title = title,
            ext = ext,
            downloadUrls = emptyList(),
            formats = VideFormatEntityList(
                listOf(
                    VideoFormatEntity(
                        formatId = "0",
                        format = ext,
                        ext = ext,
                        url = "https://example.com/$name"
                    )
                )
            ),
            isRegularDownload = true
        )
    }
}
