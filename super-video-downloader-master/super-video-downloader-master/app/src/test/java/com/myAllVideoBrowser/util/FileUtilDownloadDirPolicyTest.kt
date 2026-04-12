package com.myAllVideoBrowser.util

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class FileUtilDownloadDirPolicyTest {

    @Test
    fun `android 9 uses app external files dir for downloads when public mode is requested`() {
        val publicDownloads = File("/storage/emulated/0/Download")
        val externalFiles = File("/storage/emulated/0/Android/data/com.test/files")
        val internalFiles = File("/data/user/0/com.test/files")

        val result = resolveDownloadDirectory(
            sdkInt = 28,
            isExternalStorageUse = true,
            isAppDataDirUse = false,
            publicDownloadsRoot = publicDownloads,
            externalFilesRoot = externalFiles,
            internalFilesRoot = internalFiles
        )

        assertEquals(File(externalFiles, FileUtil.FOLDER_NAME), result)
    }

    @Test
    fun `android 10 and above keeps managed public downloads folder`() {
        val publicDownloads = File("/storage/emulated/0/Download")
        val externalFiles = File("/storage/emulated/0/Android/data/com.test/files")
        val internalFiles = File("/data/user/0/com.test/files")

        val result = resolveDownloadDirectory(
            sdkInt = 29,
            isExternalStorageUse = true,
            isAppDataDirUse = false,
            publicDownloadsRoot = publicDownloads,
            externalFilesRoot = externalFiles,
            internalFilesRoot = internalFiles
        )

        assertEquals(File("/storage/emulated/0/Download/SuperX"), result)
    }
}
