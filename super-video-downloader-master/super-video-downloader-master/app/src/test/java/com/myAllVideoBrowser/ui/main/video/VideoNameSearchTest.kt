package com.myAllVideoBrowser.ui.main.video

import android.net.Uri
import com.myAllVideoBrowser.data.local.model.LocalVideo
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoNameSearchTest {

    @Test
    fun `applyVideoNameSearch filters by file name ignoring case`() {
        val videos = listOf(
            localVideo(1L, "Holiday.mp4"),
            localVideo(2L, "family-photo.png"),
            localVideo(3L, "notes.txt")
        )

        val result = applyVideoNameSearch(videos, "PHOTO")

        assertEquals(listOf("family-photo.png"), result.map { it.name })
    }

    @Test
    fun `applyVideoNameSearch returns full list when query is blank`() {
        val videos = listOf(
            localVideo(1L, "a.mp4"),
            localVideo(2L, "b.png")
        )

        val result = applyVideoNameSearch(videos, "   ")

        assertEquals(videos.map { it.name }, result.map { it.name })
    }

    private fun localVideo(id: Long, name: String): LocalVideo {
        return LocalVideo(id = id, uri = Uri.parse("content://test/$id"), name = name)
    }
}
