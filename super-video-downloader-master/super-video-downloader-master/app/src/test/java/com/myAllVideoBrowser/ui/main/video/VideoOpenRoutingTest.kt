package com.myAllVideoBrowser.ui.main.video

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoOpenRoutingTest {

    @Test
    fun `internal player handles supported video formats`() {
        assertTrue(shouldOpenWithInternalPlayer("movie.mp4"))
        assertTrue(shouldOpenWithInternalPlayer("clip.webm"))
        assertTrue(shouldOpenWithInternalPlayer("sample.MKV"))
    }

    @Test
    fun `images and audio open with system viewer`() {
        assertFalse(shouldOpenWithInternalPlayer("photo.jpg"))
        assertFalse(shouldOpenWithInternalPlayer("cover.PNG"))
        assertFalse(shouldOpenWithInternalPlayer("track.m4a"))
        assertFalse(shouldOpenWithInternalPlayer("voice.mp3"))
    }
}
