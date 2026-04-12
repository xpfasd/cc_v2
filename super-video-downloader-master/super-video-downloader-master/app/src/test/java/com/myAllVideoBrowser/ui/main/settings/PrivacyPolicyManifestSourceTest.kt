package com.myAllVideoBrowser.ui.main.settings

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyPolicyManifestSourceTest {

    @Test
    fun `manifest registers privacy policy activity`() {
        val source = File("src/main/AndroidManifest.xml").readText()

        assertTrue(source.contains("com.myAllVideoBrowser.ui.main.settings.PrivacyPolicyActivity"))
    }
}
