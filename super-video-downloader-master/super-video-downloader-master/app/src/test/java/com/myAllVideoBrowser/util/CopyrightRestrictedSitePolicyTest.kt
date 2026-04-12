package com.myAllVideoBrowser.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CopyrightRestrictedSitePolicyTest {

    @Test
    fun `youtube urls are blocked`() {
        assertTrue(CopyrightRestrictedSitePolicy.isDownloadRestrictedUrl("https://www.youtube.com/watch?v=abc"))
        assertTrue(CopyrightRestrictedSitePolicy.isDownloadRestrictedUrl("https://m.youtube.com/watch?v=abc"))
        assertTrue(CopyrightRestrictedSitePolicy.isDownloadRestrictedUrl("https://youtu.be/abc"))
    }

    @Test
    fun `non youtube urls stay allowed`() {
        assertFalse(CopyrightRestrictedSitePolicy.isDownloadRestrictedUrl("https://vimeo.com/123"))
        assertFalse(CopyrightRestrictedSitePolicy.isDownloadRestrictedUrl("https://www.tiktok.com/@demo/video/1"))
        assertFalse(CopyrightRestrictedSitePolicy.isDownloadRestrictedUrl(""))
    }
}
