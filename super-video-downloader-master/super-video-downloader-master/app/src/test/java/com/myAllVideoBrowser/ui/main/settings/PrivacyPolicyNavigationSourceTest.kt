package com.myAllVideoBrowser.ui.main.settings

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyPolicyNavigationSourceTest {

    @Test
    fun `settings fragment launches privacy policy activity`() {
        val source = File(
            "src/main/java/com/myAllVideoBrowser/ui/main/settings/SettingsFragment.kt"
        ).readText()

        assertTrue(
            "privacy policy click should launch the dedicated activity instead of showing unavailable toast",
            source.contains("PrivacyPolicyActivity.createIntent(requireContext())")
        )
    }
}
