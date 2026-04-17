package com.myAllVideoBrowser.config

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildScriptIntegrationModeSourceTest {

    @Test
    fun `build script switches downloaderlib integration back to android library`() {
        val source = File("build.gradle.kts").readText()

        assertTrue(source.contains("id(\"com.android.application\") apply false"))
        assertTrue(source.contains("id(\"com.android.library\") apply false"))
        assertTrue(source.contains("project.name == \"downloaderlib\""))
        assertTrue(source.contains("apply(plugin = \"com.android.library\")"))
        assertTrue(source.contains("apply(plugin = \"com.android.application\")"))
    }
}
