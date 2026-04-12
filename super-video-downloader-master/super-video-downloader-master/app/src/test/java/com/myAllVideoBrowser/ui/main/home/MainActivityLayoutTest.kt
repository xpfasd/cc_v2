package com.myAllVideoBrowser.ui.main.home

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class MainActivityLayoutTest {

    @Test
    fun `fragment container view stays outside view pager`() {
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = false }
            .newDocumentBuilder()
            .parse(File("src/main/res/layout/activity_main.xml"))

        val fragmentContainerView = findElementById(document.documentElement, "@+id/fragment_container_view")
        assertNotNull("fragment_container_view should exist in activity_main.xml", fragmentContainerView)

        val parent = fragmentContainerView?.parentNode as? Element
        assertEquals("FrameLayout", parent?.tagName)

        val viewPager = findFirstElementByTag(document.documentElement, "androidx.viewpager2.widget.ViewPager2")
        assertNotNull("view_pager should exist in activity_main.xml", viewPager)
        assertTrue(
            "ViewPager2 must not declare direct child views in XML",
            viewPager?.childNodes
                ?.let { children ->
                    (0 until children.length).none { index -> children.item(index) is Element }
                } == true
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

    private fun findFirstElementByTag(root: Element, tagName: String): Element? {
        if (root.tagName == tagName) {
            return root
        }
        val children = root.childNodes
        for (index in 0 until children.length) {
            val child = children.item(index)
            if (child is Element) {
                val match = findFirstElementByTag(child, tagName)
                if (match != null) {
                    return match
                }
            }
        }
        return null
    }
}
