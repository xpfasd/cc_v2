package com.myAllVideoBrowser.ui.main.home.browser.webTab

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Test

class WebTabLegacyMenuSourceTest {

    @Test
    fun `web tab no longer wires legacy popup menu from address bar`() {
        val source = File(
            "src/main/java/com/myAllVideoBrowser/ui/main/home/browser/webTab/WebTabFragment.kt"
        ).readText()

        assertFalse(source.contains("buildWebTabMenu(this.addressContainer, false)"))
        assertFalse(source.contains("this.addressContainer.setOnLongClickListener"))
        assertFalse(source.contains("showPopupMenu()"))
    }
}
