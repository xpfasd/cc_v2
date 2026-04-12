package com.myAllVideoBrowser.util

import java.io.File

object StorageVisibilityPolicy {
    const val PRIVATE_SPACE_FOLDER_NAME = ".private_space"

    fun privateSpaceDir(filesDir: File): File = File(filesDir, PRIVATE_SPACE_FOLDER_NAME)

    fun shouldTriggerMediaScan(
        targetFile: File,
        filesDir: File,
        externalFilesDir: File?
    ): Boolean {
        return !isUnder(targetFile, filesDir) && !isUnder(targetFile, externalFilesDir)
    }

    private fun isUnder(targetFile: File, root: File?): Boolean {
        if (root == null) {
            return false
        }
        val rootPath = root.canonicalFile.toPath()
        val targetPath = targetFile.canonicalFile.toPath()
        return targetPath.startsWith(rootPath)
    }
}
