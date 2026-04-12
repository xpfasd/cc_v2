package com.myAllVideoBrowser.ui.main.home.browser.webTab

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class WebTabDownloadPermissionSourceTest {

    @Test
    fun `web tab download action gates downloads behind storage permission check`() {
        val source = File(
            "src/main/java/com/myAllVideoBrowser/ui/main/home/browser/webTab/WebTabFragment.kt"
        ).readText()

        assertTrue(source.contains("ActivityResultContracts.RequestMultiplePermissions()"))
        assertTrue(source.contains("DownloadStoragePermissionPolicy.requiresPermissionBeforeDownload"))
        assertTrue(source.contains("storagePermissionLauncher.launch(requiredDownloadPermissions())"))
        assertTrue(source.contains("startDownloadOrRequestPermissions(info)"))
    }
}
