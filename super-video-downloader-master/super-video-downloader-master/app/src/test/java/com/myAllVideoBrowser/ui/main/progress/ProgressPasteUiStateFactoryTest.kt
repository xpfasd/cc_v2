package com.myAllVideoBrowser.ui.main.progress

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressPasteUiStateFactoryTest {

    @Test
    fun `idle state keeps paste hint blank`() {
        val state = ProgressPasteUiStateFactory.createIdleState()

        assertEquals("", state.hintText)
        assertFalse(state.shouldParse)
    }

    @Test
    fun `clipboard click copies trimmed text and enables parse for http links`() {
        val state = ProgressPasteUiStateFactory.createFromClipboard(" https://example.com/video ")

        assertEquals("https://example.com/video", state.hintText)
        assertTrue(state.shouldParse)
    }

    @Test
    fun `clipboard click still copies text when parse is not allowed`() {
        val state = ProgressPasteUiStateFactory.createFromClipboard("not a link")

        assertEquals("not a link", state.hintText)
        assertFalse(state.shouldParse)
    }
}
