package com.myAllVideoBrowser.ui.main.settings

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.w3c.dom.Element

class PrivacyPolicyActivityLayoutTest {

    @Test
    fun `privacy policy layout includes a webview beneath the app styled header`() {
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = false }
            .newDocumentBuilder()
            .parse(File("src/main/res/layout/activity_privacy_policy.xml"))

        val title = findElementById(document.documentElement, "@+id/privacyPolicyTitle")
        assertNotNull("privacyPolicyTitle should exist", title)
        assertEquals("@string/settings_home_privacy_policy", title!!.getAttribute("android:text"))

        val webView = findElementById(document.documentElement, "@+id/privacyPolicyWebView")
        assertNotNull("privacyPolicyWebView should exist", webView)
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
