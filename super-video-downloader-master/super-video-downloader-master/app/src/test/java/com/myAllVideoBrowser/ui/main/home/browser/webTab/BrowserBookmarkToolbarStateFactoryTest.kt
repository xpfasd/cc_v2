package com.myAllVideoBrowser.ui.main.home.browser.webTab

import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.data.local.room.entity.PageInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserBookmarkToolbarStateFactoryTest {

    @Test
    fun `create returns marked icon when current page is bookmarked`() {
        val state = BrowserBookmarkToolbarStateFactory.create(
            currentUrl = "https://example.com/page",
            bookmarks = listOf(
                PageInfo(link = "https://example.com/page", name = "Example")
            )
        )

        assertTrue(state.isBookmarked)
        assertEquals(R.drawable.bookmarks_mark_24px, state.iconResId)
    }

    @Test
    fun `create returns default icon when current page is not bookmarked`() {
        val state = BrowserBookmarkToolbarStateFactory.create(
            currentUrl = "https://example.com/other",
            bookmarks = listOf(
                PageInfo(link = "https://example.com/page", name = "Example")
            )
        )

        assertFalse(state.isBookmarked)
        assertEquals(R.drawable.bookmarks_24px, state.iconResId)
    }
}
