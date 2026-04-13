package com.myAllVideoBrowser.ui.main.home.browser.webTab

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserDetailToolbarLayoutTest {

    @Test
    fun `web tab count badge binds to current tab index for immediate sequence rendering`() {
        val source = File(
            "src/main/res/layout/fragment_web_tab.xml"
        ).readText()

        val tabCountBlock = source.substringAfter("android:id=\"@+id/tv_tab_count\"")
            .substringBefore("/>")

        assertTrue(tabCountBlock.contains("android:text='@{(viewModel.thisTabIndex + 1) + \"\"}'"))
    }
}
