package com.myAllVideoBrowser.ui.main.home.browser

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Test

class BaseWebTabLegacyMenuSourceTest {

    @Test
    fun `base web tab fragment no longer owns popup browser menu`() {
        val source = File(
            "src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BaseWebTabFragment.kt"
        ).readText()

        assertFalse(source.contains("PopupMenu"))
        assertFalse(source.contains("menu_browser"))
        assertFalse(source.contains("buildWebTabMenu("))
        assertFalse(source.contains("showPopupMenu()"))
    }
}
