package com.myAllVideoBrowser.ui.main.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class VideoListIdentityTest {

    @Test
    fun `stableLocalVideoId returns same value for same video identity`() {
        val first = stableLocalVideoId("clip.mp4", "content://downloads/1")
        val second = stableLocalVideoId("clip.mp4", "content://downloads/1")

        assertEquals(first, second)
    }

    @Test
    fun `stableLocalVideoId differs for different uris even when file names match`() {
        val first = stableLocalVideoId("clip.mp4", "content://downloads/1")
        val second = stableLocalVideoId("clip.mp4", "content://downloads/2")

        assertNotEquals(first, second)
    }

    @Test
    fun `stableLocalVideoId differs for different names even when uri base matches`() {
        val first = stableLocalVideoId("clip-a.mp4", "file:///storage/emulated/0/Download")
        val second = stableLocalVideoId("clip-b.mp4", "file:///storage/emulated/0/Download")

        assertNotEquals(first, second)
    }
}
