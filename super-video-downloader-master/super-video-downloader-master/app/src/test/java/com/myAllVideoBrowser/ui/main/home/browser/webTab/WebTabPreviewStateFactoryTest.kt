package com.myAllVideoBrowser.ui.main.home.browser.webTab

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebTabPreviewStateFactoryTest {

    @Test
    fun `create shows image when preview bytes exist for regular tab`() {
        val state = WebTabPreviewStateFactory.create(
            previewBytes = byteArrayOf(1, 2, 3),
            isHomeTab = false
        )

        assertTrue(state.showPreviewImage)
        assertFalse(state.showPlaceholder)
    }

    @Test
    fun `create falls back to placeholder when preview is missing`() {
        val state = WebTabPreviewStateFactory.create(
            previewBytes = null,
            isHomeTab = false
        )

        assertFalse(state.showPreviewImage)
        assertTrue(state.showPlaceholder)
    }

    @Test
    fun `create keeps home tab blank even if preview bytes exist`() {
        val state = WebTabPreviewStateFactory.create(
            previewBytes = byteArrayOf(4, 5, 6),
            isHomeTab = true
        )

        assertFalse(state.showPreviewImage)
        assertFalse(state.showPlaceholder)
    }
}
