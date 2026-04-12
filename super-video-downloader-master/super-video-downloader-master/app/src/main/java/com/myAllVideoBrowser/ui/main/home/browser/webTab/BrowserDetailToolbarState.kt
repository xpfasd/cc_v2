package com.myAllVideoBrowser.ui.main.home.browser.webTab

data class BrowserDetailToolbarState(
    val address: String,
    val tabCountLabel: String
)

object BrowserDetailToolbarStateFactory {
    fun create(address: String?, tabCount: Int): BrowserDetailToolbarState {
        return BrowserDetailToolbarState(
            address = address?.trim().orEmpty(),
            tabCountLabel = (tabCount.coerceAtLeast(0) + 1).toString()
        )
    }
}
