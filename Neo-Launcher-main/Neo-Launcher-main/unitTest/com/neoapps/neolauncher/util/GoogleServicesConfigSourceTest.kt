package com.neoapps.neolauncher.util

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleServicesConfigSourceTest {

    @Test
    fun buildScript_preparesProductionAndTestGoogleServicesFiles() {
        val source = File("build.gradle.kts").readText()

        assertTrue(source.contains("google-services-production.json"))
        assertTrue(source.contains("google-services-test.json"))
        assertTrue(source.contains("prepareGoogleServicesJson"))
        assertTrue(source.contains("TOPON_TEST_MODE"))
    }

    @Test
    fun gitignore_excludesLocalGoogleServicesVariants() {
        val source = File(".gitignore").readText()

        assertTrue(source.contains("google-services-production.json"))
        assertTrue(source.contains("google-services-test.json"))
    }
}
