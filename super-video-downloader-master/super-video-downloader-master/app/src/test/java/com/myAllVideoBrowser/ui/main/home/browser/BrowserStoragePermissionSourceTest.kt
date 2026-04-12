package com.myAllVideoBrowser.ui.main.home.browser

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Test

class BrowserStoragePermissionSourceTest {

    @Test
    fun `browser fragment no longer requests write external storage permission`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserFragment.kt").readText()

        assertFalse(source.contains("Manifest.permission.WRITE_EXTERNAL_STORAGE"))
    }

    @Test
    fun `manifest no longer declares write external storage permission`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()

        assertFalse(manifest.contains("android.permission.WRITE_EXTERNAL_STORAGE"))
    }
}
