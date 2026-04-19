package com.myAllVideoBrowser.ui.main.home

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.w3c.dom.Element

class GuideNativeLayoutTest {

    @Test
    fun `onboarding native container includes pager and close button`() {
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = false }
            .newDocumentBuilder()
            .parse(File("src/main/res/layout/include_launch_onboarding.xml"))

        val pager = findElementById(document.documentElement, "@+id/onboarding_native_ad_pager")
        val closeButton = findElementById(document.documentElement, "@+id/onboarding_native_ad_close")

        assertNotNull("onboarding_native_ad_pager should exist", pager)
        assertEquals("androidx.viewpager2.widget.ViewPager2", pager?.tagName)
        assertNotNull("onboarding_native_ad_close should exist", closeButton)
        assertEquals("androidx.appcompat.widget.AppCompatImageView", closeButton?.tagName)
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
