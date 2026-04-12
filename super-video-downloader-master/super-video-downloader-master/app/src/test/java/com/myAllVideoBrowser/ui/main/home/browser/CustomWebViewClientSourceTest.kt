package com.myAllVideoBrowser.ui.main.home.browser

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomWebViewClientSourceTest {

    @Test
    fun `doUpdateVisitedHistory switches back to main thread before updating observable state`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/browser/CustomWebViewClient.kt").readText()

        val methodBody = source.substringAfter("override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {")
            .substringBefore("\n    override fun onReceivedHttpAuthRequest(")

        assertTrue(
            "doUpdateVisitedHistory should move UI-affecting updates back to the main thread",
            methodBody.contains("withContext(Dispatchers.Main.immediate)")
        )
        assertTrue(
            "video detection page start should happen from the main thread block",
            methodBody.contains("videoDetectionModel.onStartPage(")
        )
        assertTrue(
            "tab view-model history update should happen from the main thread block",
            methodBody.contains("tabViewModel.onUpdateVisitedHistory(")
        )
    }
}
