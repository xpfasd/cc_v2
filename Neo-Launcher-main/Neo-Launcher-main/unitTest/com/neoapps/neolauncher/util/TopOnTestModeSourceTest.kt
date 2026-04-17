package com.neoapps.neolauncher.util

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class TopOnTestModeSourceTest {

    @Test
    fun neoBuildScript_supportsTopOnTestModeOverrides() {
        val source = File("build.gradle.kts").readText()

        assertTrue(source.contains("TOPON_TEST_MODE"))
        assertTrue(source.contains("TEST_TOPON_APP_ID"))
        assertTrue(source.contains("TEST_TOPON_APP_KEY"))
        assertTrue(source.contains("TEST_TOPON_SPLASH_PLACEMENT_ID"))
        assertTrue(source.contains("TEST_TOPON_APP_PACKAGE_NAME"))
        assertTrue(source.contains("activeProperty("))
    }

    @Test
    fun neoManifest_declaresTopOnTestModeMetadata() {
        val manifest = File("AndroidManifest-common.xml").readText()

        assertTrue(manifest.contains("com.cc.ads.TOPON_TEST_MODE"))
        assertTrue(manifest.contains("com.cc.ads.TOPON_APP_PACKAGE_NAME"))
    }
}
