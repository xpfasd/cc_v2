package com.myAllVideoBrowser.ui.component.adapter

import com.myAllVideoBrowser.data.local.room.entity.VideFormatEntityList
import com.myAllVideoBrowser.data.local.room.entity.VideoFormatEntity
import com.myAllVideoBrowser.data.local.room.entity.VideoInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoInfoAdapterSortTest {

    @Test
    fun `sortDetectedVideosForDisplay sorts initial list by displayed size descending`() {
        val small = videoInfo(
            id = "small",
            title = "small",
            formats = listOf(format(name = "single", ext = "png", approximateSize = 253))
        )
        val large = videoInfo(
            id = "large",
            title = "large",
            formats = listOf(format(name = "single", ext = "mp4", approximateSize = 18_030_000))
        )
        val medium = videoInfo(
            id = "medium",
            title = "medium",
            formats = listOf(format(name = "single", ext = "png", approximateSize = 14_700))
        )

        val sorted = sortDetectedVideosForDisplay(listOf(small, medium, large)) {
            resolveDisplayedFormatSize(it, selectedFormatName = null)
        }

        assertEquals(listOf("large", "medium", "small"), sorted.map { it.id })
    }

    @Test
    fun `resolveDisplayedFormatSize prefers selected format before fallback`() {
        val info = videoInfo(
            id = "video",
            title = "video",
            formats = listOf(
                format(name = "small", ext = "mp4", approximateSize = 10_000),
                format(name = "large", ext = "mp4", approximateSize = 99_000)
            )
        )

        val selectedSize = resolveDisplayedFormatSize(info, selectedFormatName = "small")
        val fallbackSize = resolveDisplayedFormatSize(info, selectedFormatName = null)

        assertEquals(10_000L, selectedSize)
        assertEquals(99_000L, fallbackSize)
    }

    private fun videoInfo(
        id: String,
        title: String,
        formats: List<VideoFormatEntity>
    ): VideoInfo {
        return VideoInfo(
            id = id,
            title = title,
            ext = formats.lastOrNull()?.ext.orEmpty(),
            formats = VideFormatEntityList(formats)
        )
    }

    private fun format(
        name: String,
        ext: String,
        approximateSize: Long
    ): VideoFormatEntity {
        return VideoFormatEntity(
            format = name,
            ext = ext,
            fileSizeApproximate = approximateSize
        )
    }
}
