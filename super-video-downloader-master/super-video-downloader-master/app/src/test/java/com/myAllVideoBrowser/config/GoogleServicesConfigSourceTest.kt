package com.myAllVideoBrowser.config

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleServicesConfigSourceTest {

    @Test
    fun `build script prepares test and production google services files`() {
        val source = File("build.gradle.kts").readText()

        assertTrue(source.contains("google-services-production.json"))
        assertTrue(source.contains("google-services-test.json"))
        assertTrue(source.contains("google-services.json"))
        assertTrue(source.contains("prepareGoogleServicesJson"))
        assertTrue(source.contains("tasks.named(\"preBuild\")"))
    }

    @Test
    fun `gitignore excludes local google services variants`() {
        val source = File(".gitignore").readText()

        assertTrue(source.contains("google-services.json"))
        assertTrue(source.contains("google-services-production.json"))
        assertTrue(source.contains("google-services-test.json"))
    }
}
