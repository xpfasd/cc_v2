package com.myAllVideoBrowser.ui.main.home.browser.webTab

import org.junit.Assert.assertEquals
import org.junit.Test

class WebTabBackgroundControllerTest {

    @Test
    fun `deferred initial load starts when tab becomes active`() {
        val controller = WebTabBackgroundController()
        val runtime = FakeRuntime(currentUrl = "https://deferred.example", isLoading = false)

        controller.deferLoad("https://deferred.example")
        controller.sync(runtime, shouldBeActive = false)
        controller.sync(runtime, shouldBeActive = true)

        assertEquals(
            listOf("resumeTimers", "onResume", "loadUrl:https://deferred.example"),
            runtime.operations
        )
    }

    @Test
    fun `backgrounding a loading tab stops it and reloads current url when active again`() {
        val controller = WebTabBackgroundController()
        val runtime = FakeRuntime(currentUrl = "https://loading.example", isLoading = true)

        controller.sync(runtime, shouldBeActive = true)
        runtime.operations.clear()

        controller.sync(runtime, shouldBeActive = false)
        runtime.isLoading = false
        controller.sync(runtime, shouldBeActive = true)

        assertEquals(
            listOf(
                "stopLoading",
                "pauseMediaPlayback",
                "pauseTimers",
                "onPause",
                "resumeTimers",
                "onResume",
                "loadUrl:https://loading.example"
            ),
            runtime.operations
        )
    }

    @Test
    fun `backgrounding an idle tab pauses it without forcing reload on return`() {
        val controller = WebTabBackgroundController()
        val runtime = FakeRuntime(currentUrl = "https://ready.example", isLoading = false)

        controller.sync(runtime, shouldBeActive = true)
        runtime.operations.clear()

        controller.sync(runtime, shouldBeActive = false)
        controller.sync(runtime, shouldBeActive = true)

        assertEquals(
            listOf(
                "stopLoading",
                "pauseMediaPlayback",
                "pauseTimers",
                "onPause",
                "resumeTimers",
                "onResume"
            ),
            runtime.operations
        )
    }

    @Test
    fun `sync ignores repeated state`() {
        val controller = WebTabBackgroundController()
        val runtime = FakeRuntime(currentUrl = "https://same.example", isLoading = false)

        controller.sync(runtime, shouldBeActive = false)
        controller.sync(runtime, shouldBeActive = false)
        controller.sync(runtime, shouldBeActive = true)
        controller.sync(runtime, shouldBeActive = true)

        assertEquals(
            listOf("resumeTimers", "onResume"),
            runtime.operations
        )
    }

    private class FakeRuntime(
        override var currentUrl: String?,
        override var isLoading: Boolean
    ) : WebTabBackgroundRuntime {
        val operations = mutableListOf<String>()

        override fun stopLoading() {
            operations += "stopLoading"
        }

        override fun loadUrl(url: String) {
            operations += "loadUrl:$url"
        }

        override fun pauseMediaPlayback() {
            operations += "pauseMediaPlayback"
        }

        override fun pauseTimers() {
            operations += "pauseTimers"
        }

        override fun resumeTimers() {
            operations += "resumeTimers"
        }

        override fun onPause() {
            operations += "onPause"
        }

        override fun onResume() {
            operations += "onResume"
        }
    }
}
