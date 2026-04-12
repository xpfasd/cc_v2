package com.myAllVideoBrowser.ui.main.home.browser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class BrowserSelectionResultContractTest {

    @Test
    fun `selection result trims values and keeps title`() {
        val request = BrowserSelectionResultContract.toRequest(
            " https://target.example ",
            "  Target title  "
        )

        assertNotNull(request)
        assertEquals("https://target.example", request?.url)
        assertEquals("Target title", request?.title)
    }

    @Test
    fun `selection result ignores blank url`() {
        val request = BrowserSelectionResultContract.toRequest("   ", "Anything")

        assertNull(request)
    }
}
