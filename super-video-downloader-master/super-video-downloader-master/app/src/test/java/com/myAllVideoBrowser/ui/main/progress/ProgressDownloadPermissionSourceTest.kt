package com.myAllVideoBrowser.ui.main.progress

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressDownloadPermissionSourceTest {

    @Test
    fun `progress fragment gates downloads behind storage permission check`() {
        val source = File(
            "src/main/java/com/myAllVideoBrowser/ui/main/progress/ProgressFragment.kt"
        ).readText()

        assertTrue(source.contains("ActivityResultContracts.RequestMultiplePermissions()"))
        assertTrue(source.contains("DownloadStoragePermissionPolicy.requiresPermissionBeforeDownload"))
        assertTrue(source.contains("storagePermissionLauncher.launch(requiredDownloadPermissions())"))
        assertTrue(source.contains("startDownloadOrRequestPermissions(info)"))
    }
}
