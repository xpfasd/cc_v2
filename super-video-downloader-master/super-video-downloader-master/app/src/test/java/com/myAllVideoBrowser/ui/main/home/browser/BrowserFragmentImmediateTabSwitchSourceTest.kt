package com.myAllVideoBrowser.ui.main.home.browser

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserFragmentImmediateTabSwitchSourceTest {

    @Test
    fun `apply browser tab session switches ViewPager immediately after current tab changes`() {
        val source = File(
            "src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserFragment.kt"
        ).readText()

        val methodBody = source.substringAfter("private fun applyBrowserTabSession(session: BrowserTabSession) {")
            .substringBefore("\n    private fun persistBrowserTabSession() {")

        assertTrue(methodBody.contains("browserViewModel.currentTab.set(safeSession.currentTabIndex)"))
        assertTrue(methodBody.contains("dataBinding.viewPager.currentItem = safeSession.currentTabIndex"))
        assertTrue(
            methodBody.indexOf("browserViewModel.currentTab.set(safeSession.currentTabIndex)") <
                methodBody.indexOf("dataBinding.viewPager.currentItem = safeSession.currentTabIndex")
        )
    }
}
