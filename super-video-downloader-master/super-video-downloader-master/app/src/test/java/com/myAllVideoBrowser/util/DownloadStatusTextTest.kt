package com.myAllVideoBrowser.util

import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.util.downloaders.generic_downloader.models.VideoTaskState
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadStatusTextTest {

    @Test
    fun `statusLabelRes maps known states to localized string resources`() {
        assertEquals(R.string.download_status_preparing, DownloadStatusText.statusLabelRes(VideoTaskState.PREPARE))
        assertEquals(R.string.download_status_pending, DownloadStatusText.statusLabelRes(VideoTaskState.PENDING))
        assertEquals(R.string.download_status_downloading, DownloadStatusText.statusLabelRes(VideoTaskState.DOWNLOADING))
        assertEquals(R.string.download_status_paused, DownloadStatusText.statusLabelRes(VideoTaskState.PAUSE))
        assertEquals(R.string.download_status_success, DownloadStatusText.statusLabelRes(VideoTaskState.SUCCESS))
        assertEquals(R.string.download_status_failed, DownloadStatusText.statusLabelRes(VideoTaskState.ERROR))
        assertEquals(R.string.download_status_failed, DownloadStatusText.statusLabelRes(VideoTaskState.ENOSPC))
        assertEquals(R.string.download_status_canceled, DownloadStatusText.statusLabelRes(VideoTaskState.CANCELED))
    }

    @Test
    fun `statusLabelRes falls back to unknown status string`() {
        assertEquals(R.string.download_status_unknown, DownloadStatusText.statusLabelRes(Int.MIN_VALUE))
    }
}
