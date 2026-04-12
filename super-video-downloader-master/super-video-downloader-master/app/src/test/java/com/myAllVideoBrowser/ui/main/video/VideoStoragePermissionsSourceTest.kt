package com.myAllVideoBrowser.ui.main.video

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoStoragePermissionsSourceTest {

    @Test
    fun `video fragment requests media permissions before loading downloads`() {
        val source = File(
            "src/main/java/com/myAllVideoBrowser/ui/main/video/VideoFragment.kt"
        ).readText()

        assertTrue(source.contains("Manifest.permission.READ_MEDIA_IMAGES"))
        assertTrue(source.contains("Manifest.permission.READ_MEDIA_VIDEO"))
        assertTrue(source.contains("Manifest.permission.READ_MEDIA_AUDIO"))
        assertTrue(source.contains("storagePermissionLauncher.launch(requiredStoragePermissions())"))
    }

    @Test
    fun `manifest declares shared media read permissions`() {
        val manifest = File("src/main/AndroidManifest.xml").readText()

        assertTrue(manifest.contains("android.permission.READ_MEDIA_IMAGES"))
        assertTrue(manifest.contains("android.permission.READ_MEDIA_VIDEO"))
        assertTrue(manifest.contains("android.permission.READ_MEDIA_AUDIO"))
    }
}
