package com.myAllVideoBrowser.ui.main.home

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.w3c.dom.Element

class LaunchSplashProgressLayoutTest {

    @Test
    fun `launch splash progress indicator disables stop dot and rounded endcaps`() {
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = false }
            .newDocumentBuilder()
            .parse(File("src/main/res/layout/include_launch_splash.xml"))

        val indicator = findElementById(document.documentElement, "@+id/launch_splash_progress_indicator")
        assertNotNull("launch_splash_progress_indicator should exist", indicator)
        assertEquals("0dp", indicator!!.getAttribute("app:trackStopIndicatorSize"))
        assertEquals("0dp", indicator.getAttribute("app:trackCornerRadius"))
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
