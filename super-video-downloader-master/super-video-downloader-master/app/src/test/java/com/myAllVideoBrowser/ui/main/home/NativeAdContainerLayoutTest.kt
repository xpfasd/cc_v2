package com.myAllVideoBrowser.ui.main.home

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.w3c.dom.Element

class NativeAdContainerLayoutTest {

    @Test
    fun `browser home native ad container uses compact fixed height`() {
        val document = parseLayout("src/main/res/layout/fragment_browser_home.xml")

        val nativeContainer = findElementById(document.documentElement, "@+id/homeTopNativeAdContainer")
        assertNotNull("homeTopNativeAdContainer should exist", nativeContainer)
        assertEquals("100dp", nativeContainer!!.getAttribute("android:layout_height"))
    }

    @Test
    fun `language settings native ad container uses compact fixed height`() {
        val document = parseLayout("src/main/res/layout/activity_language_settings.xml")

        val nativeContainer = findElementById(document.documentElement, "@+id/languageNativeAdContainer")
        assertNotNull("languageNativeAdContainer should exist", nativeContainer)
        assertEquals("100dp", nativeContainer!!.getAttribute("android:layout_height"))
    }

    private fun parseLayout(path: String) =
        DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = false }
            .newDocumentBuilder()
            .parse(File(path))

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
