package com.myAllVideoBrowser.ui.main.home.browser.detectedVideos

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.w3c.dom.Element

class DetectedVideosLayoutTest {

    @Test
    fun `detected downloads sheet keeps bottom space above main navigation`() {
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = false }
            .newDocumentBuilder()
            .parse(File("src/main/res/layout/fragment_detected_videos_tab.xml"))

        val sheetContainer = findElementById(document.documentElement, "@+id/sheet_container")

        assertNotNull(sheetContainer)
        assertEquals("@dimen/home_tab_bar_height", sheetContainer?.getAttribute("android:layout_marginBottom"))
    }

    private fun findElementById(root: Element, idValue: String): Element? {
        if (root.getAttribute("android:id") == idValue) {
            return root
        }
        val children = root.childNodes
        for (index in 0 until children.length) {
            val child = children.item(index)
            if (child is Element) {
                val match = findElementById(child, idValue)
                if (match != null) {
                    return match
                }
            }
        }
        return null
    }
}
