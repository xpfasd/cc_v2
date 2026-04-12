package com.myAllVideoBrowser.ui.main.home.browser.webTab

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserDetailToolbarIndexSourceTest {

    @Test
    fun `web tab toolbar header uses current tab index provider for immediate tab count label`() {
        val source = File(
            "src/main/java/com/myAllVideoBrowser/ui/main/home/browser/webTab/WebTabFragment.kt"
        ).readText()

        val methodBody = source.substringAfter("private fun updateToolbarHeader() {")
            .substringBefore("\n    private fun publishTabPreviewSnapshot() {")

        assertTrue(methodBody.contains("currentTabIndexProvider.getCurrentTabIndex().get()"))
    }

    @Test
    fun `current tab selection callback refreshes toolbar header immediately`() {
        val source = File(
            "src/main/java/com/myAllVideoBrowser/ui/main/home/browser/webTab/WebTabFragment.kt"
        ).readText()

        val callbackBody = source.substringAfter("private val currentTabSelectionCallback = object : Observable.OnPropertyChangedCallback() {")
            .substringBefore("\n    private val backPressedCallback = object : OnBackPressedCallback(true) {")

        assertTrue(callbackBody.contains("updateToolbarHeader()"))
    }
}
