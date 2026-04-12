package com.myAllVideoBrowser.ui.main.home.browser.webTab

import org.junit.Assert.assertEquals
import org.junit.Test

class BrowserDetailToolbarStateFactoryTest {

    @Test
    fun `create keeps current address and formats current tab number`() {
        val state = BrowserDetailToolbarStateFactory.create(
            address = "https://translate.google.com",
            tabCount = 1
        )

        assertEquals("https://translate.google.com", state.address)
        assertEquals("2", state.tabCountLabel)
    }

    @Test
    fun `create clamps empty address and minimum tab count`() {
        val state = BrowserDetailToolbarStateFactory.create(
            address = "  ",
            tabCount = 0
        )

        assertEquals("", state.address)
        assertEquals("1", state.tabCountLabel)
    }
}
