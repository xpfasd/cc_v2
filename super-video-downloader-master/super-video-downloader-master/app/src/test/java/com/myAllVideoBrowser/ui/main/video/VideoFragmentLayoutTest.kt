package com.myAllVideoBrowser.ui.main.video

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.w3c.dom.Element

class VideoFragmentLayoutTest {

    @Test
    fun `video fragment list and empty state are not driven by databinding items expression`() {
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = false }
            .newDocumentBuilder()
            .parse(File("src/main/res/layout/fragment_video.xml"))

        val recyclerView = findElementById(document.documentElement, "@+id/rv_video")
        assertNotNull("rv_video should exist in fragment_video.xml", recyclerView)
        assertFalse(
            "rv_video items should be driven explicitly from VideoFragment syncListUi()",
            recyclerView!!.hasAttribute("app:items")
        )

        val emptyLayout = findElementById(document.documentElement, "@+id/layout_empty")
        assertNotNull("layout_empty should exist in fragment_video.xml", emptyLayout)
        assertEquals(
            "layout_empty visibility should be controlled explicitly by VideoFragment syncListUi()",
            "gone",
            emptyLayout!!.getAttribute("android:visibility")
        )
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
