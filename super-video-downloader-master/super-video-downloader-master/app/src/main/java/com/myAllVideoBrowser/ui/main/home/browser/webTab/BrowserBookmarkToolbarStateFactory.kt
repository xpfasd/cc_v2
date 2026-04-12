package com.myAllVideoBrowser.ui.main.home.browser.webTab

import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.data.local.room.entity.PageInfo

data class BrowserBookmarkToolbarState(
    val isBookmarked: Boolean,
    val iconResId: Int
)

object BrowserBookmarkToolbarStateFactory {
    fun create(
        currentUrl: String?,
        bookmarks: List<PageInfo>?
    ): BrowserBookmarkToolbarState {
        val normalizedUrl = currentUrl.orEmpty().trim()
        val isBookmarked = normalizedUrl.isNotEmpty() &&
            bookmarks.orEmpty().any { it.link == normalizedUrl }

        return BrowserBookmarkToolbarState(
            isBookmarked = isBookmarked,
            iconResId = if (isBookmarked) {
                R.drawable.bookmarks_mark_24px
            } else {
                R.drawable.bookmarks_24px
            }
        )
    }
}
