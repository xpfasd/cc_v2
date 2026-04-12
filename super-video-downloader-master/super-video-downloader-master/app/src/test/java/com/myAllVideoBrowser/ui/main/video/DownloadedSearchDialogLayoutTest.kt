package com.myAllVideoBrowser.ui.main.video

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.w3c.dom.Element

class DownloadedSearchDialogLayoutTest {

    @Test
    fun `downloaded search dialog uses app search pill styling and custom actions`() {
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = false }
            .newDocumentBuilder()
            .parse(File("src/main/res/layout/dialog_downloaded_search.xml"))

        val searchBox = findElementById(document.documentElement, "@+id/search_box")
        assertNotNull("search_box should exist", searchBox)
        assertEquals("@drawable/bg_home_search_pill", searchBox!!.getAttribute("android:background"))

        val searchButton = findElementById(document.documentElement, "@+id/btn_search")
        assertNotNull("btn_search should exist", searchButton)
        assertEquals("@color/home_accent", searchButton!!.getAttribute("app:backgroundTint"))

        val cancelButton = findElementById(document.documentElement, "@+id/btn_cancel")
        assertNotNull("btn_cancel should exist", cancelButton)
        assertEquals("@color/home_divider", cancelButton!!.getAttribute("app:strokeColor"))
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
