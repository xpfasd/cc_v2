package com.myAllVideoBrowser.ui.main.home.browser

data class BrowserSelectionRequest(
    val url: String,
    val title: String?
)

object BrowserSelectionResultContract {
    const val EXTRA_SELECTED_URL = "extra_selected_url"
    const val EXTRA_SELECTED_TITLE = "extra_selected_title"

    fun toRequest(url: String?, title: String?): BrowserSelectionRequest? {
        val normalizedUrl = url?.trim().orEmpty()
        if (normalizedUrl.isEmpty()) {
            return null
        }

        val normalizedTitle = title?.trim()?.takeIf { it.isNotEmpty() }
        return BrowserSelectionRequest(
            url = normalizedUrl,
            title = normalizedTitle
        )
    }
}
