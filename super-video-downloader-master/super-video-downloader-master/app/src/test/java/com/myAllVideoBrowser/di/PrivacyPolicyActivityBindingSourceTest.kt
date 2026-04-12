package com.myAllVideoBrowser.di

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyPolicyActivityBindingSourceTest {

    @Test
    fun `activity binding module contributes injector for privacy policy activity`() {
        val source = File(
            "src/main/java/com/myAllVideoBrowser/di/module/ActivityBindingModule.kt"
        ).readText()

        assertTrue(source.contains("PrivacyPolicyActivity"))
        assertTrue(source.contains("bindPrivacyPolicyActivity()"))
    }
}
