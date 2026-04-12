package com.myAllVideoBrowser.util.proxy_utils

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ProxyServiceManifestSourceTest {

    @Test
    fun `manifest keeps proxy service disabled`() {
        val source = File("src/main/AndroidManifest.xml").readText()

        assertTrue(source.contains("android:name=\"com.myAllVideoBrowser.util.proxy_utils.ProxyService\""))
        assertTrue(source.contains("android:enabled=\"false\""))
    }
}
