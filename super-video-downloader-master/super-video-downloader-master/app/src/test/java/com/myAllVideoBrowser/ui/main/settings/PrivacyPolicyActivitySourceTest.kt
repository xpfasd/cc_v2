package com.myAllVideoBrowser.ui.main.settings

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyPolicyActivitySourceTest {

    @Test
    fun `privacy policy activity loads the fixed policy url in webview`() {
        val source = File(
            "src/main/java/com/myAllVideoBrowser/ui/main/settings/PrivacyPolicyActivity.kt"
        ).readText()

        assertTrue(source.contains("https://sites.google.com/view/vdd-privacy-policy"))
        assertTrue(source.contains("privacyPolicyWebView.loadUrl(PRIVACY_POLICY_URL)"))
        assertTrue(source.contains("privacyPolicyWebView.webViewClient = WebViewClient()"))
    }
}
