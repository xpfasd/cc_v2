package com.myAllVideoBrowser.ui.main.home.browser.webTab

import android.util.Patterns
import com.myAllVideoBrowser.ui.main.home.browser.BrowserViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class WebTabFactory {
    companion object {
        private const val DEFAULT_NEW_TAB_URL = "https://www.google.com/"

        fun createWebTabFromInput(input: String): WebTab {
            if (input.isNotEmpty()) {
                return if (input.startsWith("http://") || input.startsWith("https://")) {
                    WebTab(url = input, title = null, iconBytes = null, headers = emptyMap())
                } else if (Patterns.WEB_URL.matcher(input).matches()) {
                    WebTab(url = "https://$input", title = null, iconBytes = null, headers = emptyMap())
                } else {
                    val encodedQuery = URLEncoder.encode(input, StandardCharsets.UTF_8.toString())
                    WebTab(
                        url = String.format(BrowserViewModel.SEARCH_URL, encodedQuery),
                        title = null,
                        iconBytes = null,
                        headers = emptyMap()
                    )
                }
            }

            return WebTab.HOME_TAB
        }

        fun createDefaultNewTab(): WebTab {
            return WebTab(
                url = DEFAULT_NEW_TAB_URL,
                title = "Google",
                headers = emptyMap()
            )
        }
    }
}
