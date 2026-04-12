package com.myAllVideoBrowser.util

import android.Manifest
import android.os.Build

object DownloadStoragePermissionPolicy {

    fun requiresPermissionBeforeDownload(
        isExternalStorageUse: Boolean,
        isAppDataDirUse: Boolean
    ): Boolean {
        return isExternalStorageUse && !isAppDataDirUse
    }

    fun requiredPermissions(sdkInt: Int): Array<String> {
        return if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}
