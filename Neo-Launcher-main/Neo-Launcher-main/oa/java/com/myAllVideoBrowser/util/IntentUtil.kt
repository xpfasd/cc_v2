package com.myAllVideoBrowser.util

//import com.allVideoDownloaderXmaster.OpenForTesting

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.myAllVideoBrowser.R
import java.io.File
import javax.inject.Inject

//@OpenForTesting
class IntentUtil @Inject constructor(private val fileUtil: FileUtil) {

    @Deprecated("This old method is deprecated")
    fun openVideoFolder(context: Context?, path: String) {
        context?.let {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val photoURI = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                File("${context.filesDir.path}/${FileUtil.FOLDER_NAME}")
            )

            intent.setDataAndType(photoURI, DocumentsContract.Document.MIME_TYPE_DIR)

            if (intent.resolveActivity(it.packageManager) != null) {
                it.startActivity(intent)
            } else {
                Toast.makeText(
                    it,
                    it.getString(R.string.settings_message_open_folder),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    fun shareVideo(context: Context, uri: Uri, fileName: String? = null) {
        val intent = Intent(Intent.ACTION_SEND)
        val mimeType = resolveMimeType(context, uri, fileName)
        intent.type = mimeType
        val shareUri = toExposedUri(context, uri)
        intent.setDataAndType(shareUri, mimeType)
        intent.clipData = ClipData.newRawUri("", shareUri)
        intent.putExtra(Intent.EXTRA_STREAM, shareUri)

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val resInfoList: List<ResolveInfo> = context.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(
                packageName,
                shareUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        if (intent.resolveActivityInfo(context.packageManager, 0) != null) {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_via)))
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.video_share_message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun openMedia(context: Context, uri: Uri, fileName: String? = null) {
        val mimeType = resolveMimeType(context, uri, fileName)
        val openUri = toExposedUri(context, uri)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(openUri, mimeType)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newRawUri("", openUri)
        }

        val resInfoList: List<ResolveInfo> = context.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        for (resolveInfo in resInfoList) {
            context.grantUriPermission(
                resolveInfo.activityInfo.packageName,
                openUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        if (intent.resolveActivityInfo(context.packageManager, 0) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.video_share_message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun resolveMimeType(context: Context, uri: Uri, fileName: String? = null): String {
        return context.contentResolver.getType(uri)
            ?: resolveMimeTypeFromName(fileName)
            ?: resolveMimeTypeFromName(uri.lastPathSegment)
    }

    private fun toExposedUri(context: Context, uri: Uri): Uri {
        return if (fileUtil.isFileApiSupportedByUri(context, uri)) {
            FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                uri.toFile()
            )
        } else {
            uri
        }
    }
}

internal fun resolveMimeTypeFromNames(fileNames: List<String>): String {
    val mimeTypes = fileNames.map { resolveMimeTypeFromName(it) }.distinct()
    if (mimeTypes.isEmpty()) {
        return "*/*"
    }
    if (mimeTypes.size == 1) {
        val mimeType = mimeTypes.first()
        return when {
            mimeType.startsWith("video/") -> "video/*"
            mimeType.startsWith("audio/") -> "audio/*"
            mimeType.startsWith("image/") -> "image/*"
            else -> mimeType
        }
    }

    val families = mimeTypes.map { it.substringBefore('/') }.distinct()
    return if (families.size == 1) {
        "${families.first()}/*"
    } else {
        "*/*"
    }
}

internal fun resolveMimeTypeFromName(fileName: String?): String {
    val extension = fileName
        ?.substringAfterLast('.', "")
        ?.lowercase()
        ?.takeIf { it.isNotBlank() }
        ?: return "*/*"

    return when {
        MediaFormatSupport.isVideoExtension(extension) -> when (extension) {
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "mov" -> "video/quicktime"
            "m4v" -> "video/x-m4v"
            else -> "video/*"
        }

        MediaFormatSupport.isAudioExtension(extension) -> when (extension) {
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            else -> "audio/*"
        }

        MediaFormatSupport.isImageExtension(extension) -> when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            else -> "image/*"
        }

        else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
    }
}
