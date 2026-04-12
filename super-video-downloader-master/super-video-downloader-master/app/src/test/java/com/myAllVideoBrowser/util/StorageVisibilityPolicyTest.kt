package com.myAllVideoBrowser.util

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageVisibilityPolicyTest {

    @Test
    fun `private space dir stays under app internal files directory`() {
        val filesDir = File("/data/user/0/com.myAllVideoBrowser/files")

        val privateDir = StorageVisibilityPolicy.privateSpaceDir(filesDir)

        assertEquals(
            File(filesDir, ".private_space").path,
            privateDir.path
        )
    }

    @Test
    fun `media scan is disabled for app private directories`() {
        val filesDir = File("/data/user/0/com.myAllVideoBrowser/files")
        val externalFilesDir = File("/storage/emulated/0/Android/data/com.myAllVideoBrowser/files")

        assertFalse(
            StorageVisibilityPolicy.shouldTriggerMediaScan(
                File(filesDir, ".private_space/secret.mp4"),
                filesDir,
                externalFilesDir
            )
        )
        assertFalse(
            StorageVisibilityPolicy.shouldTriggerMediaScan(
                File(externalFilesDir, "SuperX/hidden.jpg"),
                filesDir,
                externalFilesDir
            )
        )
    }

    @Test
    fun `media scan stays enabled for public downloads`() {
        val filesDir = File("/data/user/0/com.myAllVideoBrowser/files")
        val externalFilesDir = File("/storage/emulated/0/Android/data/com.myAllVideoBrowser/files")
        val publicDownload = File("/storage/emulated/0/Download/video.mp4")

        assertTrue(
            StorageVisibilityPolicy.shouldTriggerMediaScan(
                publicDownload,
                filesDir,
                externalFilesDir
            )
        )
    }
}
