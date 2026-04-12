package com.myAllVideoBrowser.ui.main.home

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class LaunchSplashLayoutTest {

    @Test
    fun `launch splash uses a progress indicator control`() {
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = false }
            .newDocumentBuilder()
            .parse(File("src/main/res/layout/include_launch_splash.xml"))

        val progressIndicator = findElementById(
            document.documentElement,
            "@+id/launch_splash_progress_indicator"
        )

        assertNotNull("launch_splash_progress_indicator should exist", progressIndicator)
        assertEquals(
            "com.google.android.material.progressindicator.LinearProgressIndicator",
            progressIndicator?.tagName
        )
        assertEquals(
            "",
            progressIndicator?.getAttribute("app:indeterminate")
        )
        assertTrue(
            "launch_splash_progress_indicator should opt into determinate mode with android:indeterminate",
            progressIndicator?.getAttribute("android:indeterminate") == "false"
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
