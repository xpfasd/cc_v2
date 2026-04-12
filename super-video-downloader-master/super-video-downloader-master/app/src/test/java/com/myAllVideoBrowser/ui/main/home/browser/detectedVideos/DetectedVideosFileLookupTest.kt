package com.myAllVideoBrowser.ui.main.home.browser.detectedVideos

import android.net.Uri
import com.myAllVideoBrowser.util.FileUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DetectedVideosFileLookupTest {

    @Test
    fun `findDownloadedUriFromSnapshot matches file names ignoring case`() {
        val snapshot = mapOf(
            "TikTok-Make_Your_Day.mp4" to FileUtil.DownloadedMediaFile(Uri.parse("content://downloads/1"))
        )

        val result = findDownloadedUriFromSnapshot(snapshot, "tiktok-make_your_day.mp4")

        assertEquals(Uri.parse("content://downloads/1"), result)
    }

    @Test
    fun `findDownloadedUriFromSnapshot returns null when file is absent`() {
        val snapshot = mapOf(
            "image.jpg" to FileUtil.DownloadedMediaFile(Uri.parse("content://downloads/2"))
        )

        assertNull(findDownloadedUriFromSnapshot(snapshot, "video.mp4"))
    }
}
