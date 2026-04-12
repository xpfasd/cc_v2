package com.myAllVideoBrowser.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MediaMimeTypeResolverTest {

    @Test
    fun `resolveMimeTypeFromName returns video mime for webm`() {
        assertEquals("video/webm", resolveMimeTypeFromName("clip.webm"))
    }

    @Test
    fun `resolveMimeTypeFromName returns image mime for png`() {
        assertEquals("image/png", resolveMimeTypeFromName("poster.png"))
    }

    @Test
    fun `resolveMimeTypeFromName returns audio mime for m4a`() {
        assertEquals("audio/mp4", resolveMimeTypeFromName("track.m4a"))
    }

    @Test
    fun `resolveMimeTypeFromName falls back to generic media bucket`() {
        assertEquals("*/*", resolveMimeTypeFromName("archive.bin"))
    }

    @Test
    fun `resolveMimeTypeFromNames prefers a shared media family for mixed videos`() {
        assertEquals("video/*", resolveMimeTypeFromNames(listOf("clip.mp4", "clip.webm")))
    }

    @Test
    fun `resolveMimeTypeFromNames falls back to generic type for mixed media families`() {
        assertEquals("*/*", resolveMimeTypeFromNames(listOf("clip.mp4", "poster.png")))
    }
}
