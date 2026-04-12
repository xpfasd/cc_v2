package com.myAllVideoBrowser.ui.main.home.browser.webTab

internal interface WebTabBackgroundRuntime {
    val currentUrl: String?
    val isLoading: Boolean

    fun stopLoading()
    fun loadUrl(url: String)
    fun pauseMediaPlayback()
    fun pauseTimers()
    fun resumeTimers()
    fun onPause()
    fun onResume()
}

internal class WebTabBackgroundController {
    private var isActive = false
    private var pendingLoadUrl: String? = null

    fun deferLoad(url: String?) {
        pendingLoadUrl = url?.takeIf { it.startsWith("http") }
    }

    fun sync(runtime: WebTabBackgroundRuntime, shouldBeActive: Boolean) {
        if (isActive == shouldBeActive) {
            return
        }

        isActive = shouldBeActive
        if (shouldBeActive) {
            runtime.resumeTimers()
            runtime.onResume()
            pendingLoadUrl?.let(runtime::loadUrl)
            pendingLoadUrl = null
            return
        }

        if (runtime.isLoading) {
            pendingLoadUrl = runtime.currentUrl?.takeIf { it.startsWith("http") }
        }
        runtime.stopLoading()
        runtime.pauseMediaPlayback()
        runtime.pauseTimers()
        runtime.onPause()
    }
}
