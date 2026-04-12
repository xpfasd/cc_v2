package com.myAllVideoBrowser.util

import android.Manifest
import android.os.Build
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadStoragePermissionPolicyTest {

    @Test
    fun `public downloads mode requires permission gate before download`() {
        assertTrue(
            DownloadStoragePermissionPolicy.requiresPermissionBeforeDownload(
                isExternalStorageUse = true,
                isAppDataDirUse = false
            )
        )
    }

    @Test
    fun `app private download modes bypass upfront permission gate`() {
        assertFalse(
            DownloadStoragePermissionPolicy.requiresPermissionBeforeDownload(
                isExternalStorageUse = false,
                isAppDataDirUse = true
            )
        )
        assertFalse(
            DownloadStoragePermissionPolicy.requiresPermissionBeforeDownload(
                isExternalStorageUse = true,
                isAppDataDirUse = true
            )
        )
    }

    @Test
    fun `android 13 and above requests shared media read permissions`() {
        assertArrayEquals(
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            ),
            DownloadStoragePermissionPolicy.requiredPermissions(Build.VERSION_CODES.TIRAMISU)
        )
    }

    @Test
    fun `android 12 and below requests external storage read permission`() {
        assertArrayEquals(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            DownloadStoragePermissionPolicy.requiredPermissions(Build.VERSION_CODES.S_V2)
        )
    }
}
