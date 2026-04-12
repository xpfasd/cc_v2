package com.myAllVideoBrowser.ui.main.home.browser.detectedVideos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectedImageSupportTest {

    @Test
    fun `regular detector should not ignore image URLs`() {
        assertFalse(
            DirectMediaSupport.shouldIgnoreRegularUrl("https://cdn.example.com/folder/cover.jpg")
        )
    }

    @Test
    fun `regular detector still ignores static css assets`() {
        assertTrue(
            DirectMediaSupport.shouldIgnoreRegularUrl("https://cdn.example.com/app.css")
        )
    }

    @Test
    fun `infer image extension from content type`() {
        assertEquals(
            "jpg",
            DirectMediaSupport.inferExtension(
                url = "https://cdn.example.com/resource?id=1",
                contentType = "image/jpeg",
                fallbackExt = "bin"
            )
        )
    }

    @Test
    fun `infer image extension from URL when content type is absent`() {
        assertEquals(
            "webp",
            DirectMediaSupport.inferExtension(
                url = "https://cdn.example.com/sample.webp?token=1",
                contentType = null,
                fallbackExt = "bin"
            )
        )
    }
}
