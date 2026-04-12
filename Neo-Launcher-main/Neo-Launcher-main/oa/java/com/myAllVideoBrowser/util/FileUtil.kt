package com.myAllVideoBrowser.util

//import com.allVideoDownloaderXmaster.OpenForTesting
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.myAllVideoBrowser.R
import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.file.Files
import java.text.DecimalFormat
import java.util.Arrays
import javax.inject.Inject

//@OpenForTesting
class FileUtil @Inject constructor() {

    data class DownloadedMediaFile(
        val uri: Uri
    )

    companion object {
        var INITIIALIZED = false

        // For downloads and tmp data
        var IS_EXTERNAL_STORAGE_USE = true

        // For downloads
        var IS_APP_DATA_DIR_USE = false

        const val FOLDER_NAME = "SuperX"
        const val TMP_DATA_FOLDER_NAME = "superx_tmp_data"

        private const val KB = 1024
        private const val MB = 1024 * 1024
        private const val GB = 1024 * 1024 * 1024

        // 10MB
        private const val FREE_SPACE_TRESHOLD = 10 * 1024 * 1024

        fun getFileSizeReadable(length: Double): String {

            val decimalFormat = DecimalFormat("#.##")
            return when {
                length > GB -> decimalFormat.format(length / GB) + " GB"
                length > MB -> decimalFormat.format(length / MB) + " MB"
                length > KB -> decimalFormat.format(length / KB) + " KB"
                else -> decimalFormat.format(length) + " B"
            }
        }

        fun getFreeDiskSpace(path: File): Long {
            if (!path.exists()) {
                throw IllegalArgumentException("Path does not exist")
            }

            val stats = StatFs(path.absolutePath)
            return stats.availableBlocksLong * stats.blockSizeLong
        }

        fun calculateFolderSize(directory: File): Long {
            var length = 0L
            if (directory.isDirectory) {
                for (file in directory.listFiles() ?: emptyArray()) {
                    length += calculateFolderSize(file)
                }
            } else {
                length += directory.length()
            }
            return length
        }

        fun isExternalStorageWritable(): Boolean {
            return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        }
    }

    val folderDir: File
        get() {
            if (!INITIIALIZED) {
                throw Error("File Util Not Initialized")
            }

            val context = ContextUtils.getApplicationContext()
            return resolveDownloadDirectory(
                sdkInt = Build.VERSION.SDK_INT,
                isExternalStorageUse = IS_EXTERNAL_STORAGE_USE,
                isAppDataDirUse = IS_APP_DATA_DIR_USE,
                publicDownloadsRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                externalFilesRoot = context.getExternalFilesDir(null),
                internalFilesRoot = context.filesDir
            )
        }

    val privateSpaceDir: File
        get() {
            val context = ContextUtils.getApplicationContext()
            val file = StorageVisibilityPolicy.privateSpaceDir(context.filesDir)
            if (!file.exists()) {
                file.mkdirs()
            }
            migrateLegacyPrivateSpace(file)
            return file
        }

    val tmpDir: File
        get() {
            if (!INITIIALIZED) {
                throw Error("File Util Not Initialized")
            }

            val context = ContextUtils.getApplicationContext()

            return getTmpDataDir(context, IS_EXTERNAL_STORAGE_USE)
        }

    val listFiles: Map<String, DownloadedMediaFile>
        get() {
            val result = mutableMapOf<String, DownloadedMediaFile>()
            val managedFolder = folderDir
            if (!managedFolder.exists()) {
                managedFolder.mkdirs()
            }
            for (file in collectManagedMediaFiles(managedFolder)) {
                result[file.name] = DownloadedMediaFile(Uri.fromFile(file))
            }

            AppLogger.d(
                "FileUtil.listFiles folderDir=${managedFolder.absolutePath} " +
                    "publicMode=managed_direct " +
                    "total=${result.size} " +
                    "sample=${result.keys.take(10)}"
            )

            return result

        }

    fun isFreeSpaceAvailable(): Boolean {
        return getFreeDiskSpace(folderDir) > FREE_SPACE_TRESHOLD
    }

    fun isFileWithNameNotExists(context: Context, uri: Uri, newName: String): Boolean {
        return if (isFileApiSupportedByUri(context, uri)) {
            !File(uri.toFile().parentFile, newName).exists()
        } else {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                !isDownloadedVideoContentExistsByName(context.contentResolver, uri, newName)
            } else {
                throw Exception("File api support ERROR")
            }
        }
    }

    @Synchronized
    fun moveMedia(context: Context, from: Uri, to: Uri): Boolean {
        val targetFile = to.toFileOrNull()
        if (targetFile != null && isAppPrivateTarget(context, targetFile)) {
            AppLogger.d("Moving media into app private storage: $from -> $targetFile")
            return moveMediaToPrivateStorage(context, from, targetFile)
        }

        if (isFileApiSupportedByUri(context, to)) {
            AppLogger.d("IS_FILE_API: TRUE -- from $from to $to")
            val newFile = to.toFile()
            var success: Boolean

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Files.move(from.toFile().toPath(), newFile.toPath())
                success = true
            } else {
                success = renameWithLock(from.toFile(), newFile)
            }

            if (success && shouldTriggerMediaScan(context, newFile)) {
                AppLogger.d("File move success, triggering media scan for: ${newFile.absolutePath}")
                scanFile(context, newFile)
            }
            return success
        } else {
            AppLogger.d("IS_FILE_API: FALSE -- from $from to $to")
            return if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                val result = moveFileToDownloadsFolder(
                    context.contentResolver, from.toFile(), to.toFile().name
                )
                if (result) {
                    scanFile(context, to.toFile())
                }
                result
            } else {
                throw Exception("File API support ERROR!!!")
            }
        }
    }

    fun renameMedia(context: Context, from: Uri, newName: String): Pair<String, Uri>? {
        try {
            val originExtension = from.toFile().extension
            val cleanedFileName = FileNameCleaner.cleanFileName(newName) + ".$originExtension"
            val isNewFileNotExists = isFileWithNameNotExists(context, from, newName)

            if (cleanedFileName.isEmpty()) {
                throw Error("Empty file name")
            }

            if (!isUriExists(context, from)) {
                throw FileNotFoundException("File not found: $from")
            }

            if (!isNewFileNotExists) {
                throw Exception("File already exists")
            }
            if (isFileApiSupportedByUri(context, from)) {
                val fromFile = from.toFile()
                val toFile = File(fromFile.parentFile, cleanedFileName)
                if (toFile.exists()) {
                    throw Exception("File already exists: $toFile")
                }
                fromFile.renameTo(toFile)

                return Pair(toFile.name, Uri.fromFile(toFile))
            } else {
                val newUri = renameVideoContentFromDownloads(context, from, cleanedFileName)

                return Pair(cleanedFileName, newUri ?: from)
            }
        } catch (e: Throwable) {
            Toast.makeText(context, R.string.common_error_label, Toast.LENGTH_SHORT).show()
        }

        return null
    }

    fun deleteMedia(context: Context, uri: Uri) {
        try {
            if (!isUriExists(context, uri)) {
                throw FileNotFoundException("File not found: $uri")
            }
            if (isFileApiSupportedByUri(context, uri)) {
                uri.toFile().delete()
            } else {
                deleteDownloadedVideoContent(context, uri)
            }
        } catch (e: Throwable) {
            e.printStackTrace()

            Toast.makeText(context, R.string.common_error_label, Toast.LENGTH_SHORT).show()
        }
    }

    fun isUriExists(context: Context, uri: Uri): Boolean {
        if (isFileApiSupportedByUri(context, uri)) {
            return uri.toFile().exists()
        }

        try {
            context.contentResolver.openInputStream(uri)?.close()
        } catch (e: FileNotFoundException) {
            return false
        } catch (e: Exception) {
            // Handle other exceptions as needed
        }

        // If there were no exceptions, the URI exists
        return true
    }

    fun getContentLength(context: Context, uri: Uri): Long {
        return if (isFileApiSupportedByUri(context, uri)) {
            uri.toFile().length()
        } else {
            getContentSize(context, uri)
        }
    }

    fun isFileApiSupportedByUri(context: Context, uri: Uri): Boolean {
        val isExternalTo = isExternalUri(uri)

        val privateDir = getPrivateDownloadsDir(context, isExternalTo)
        val isAppDir = uri.toString().startsWith(Uri.fromFile(privateDir).toString())

        return !(Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && !isAppDir)
    }

    // WITHOUT LOCK EXISTS PROBABILITY OF CORRUPTED FILE AFTER renameTo()
    private fun renameWithLock(sourceFile: File, targetFile: File): Boolean {
        try {
            // 1. Acquire a lock on the source file
            val randomAccessFile = RandomAccessFile(sourceFile, "rw")
            val fileChannel: FileChannel = randomAccessFile.channel
            val fileLock: FileLock = fileChannel.lock()

            try {
                // 2. Perform the renameTo() operation while holding the lock
                val success = sourceFile.renameTo(targetFile)
                return success

            } finally {
                // 3. Release the lock in the finally block
                fileLock.release()
                randomAccessFile.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AppLogger.d(e.message.toString())
            return false
        }
    }

    private fun getContentSize(context: Context, uri: Uri): Long {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && !cursor.isNull(sizeIndex)) {
                cursor.getLong(sizeIndex)
            } else {
                -1 // Return -1 if size is unknown or an error occurred
            }
        } ?: -1 // Return -1 if the query failed
    }

    private fun renameVideoContentFromDownloads(context: Context, uri: Uri, newName: String): Uri? {
        // Check if the URI is a document URI
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // Rename the document using the DocumentsContract API
            return DocumentsContract.renameDocument(context.contentResolver, uri, newName)
        } else {
            // Rename the file using the ContentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, newName)
                put(MediaStore.Downloads.RELATIVE_PATH, managedDownloadRelativePath())
            }

            context.contentResolver.update(uri, values, null, null)

            return Uri.parse(uri.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isDownloadedVideoContentExistsByName(
        contentResolver: ContentResolver, contentOrig: Uri, fileName: String
    ): Boolean {
        val isExternal = isExternalUri(contentOrig)
        val contentUri = if (isExternal) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Downloads.INTERNAL_CONTENT_URI
        }
        // Query the Downloads collection for files with the given name
        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)

        val cursor = contentResolver.query(
            contentUri, projection, selection, selectionArgs, null
        )

        // Check if the cursor is not null and has at least one row
        val exists = (cursor?.count ?: 0) > 0

        // Close the cursor
        cursor?.close()

        return exists
    }

    private fun isAppPrivateTarget(context: Context, targetFile: File): Boolean {
        return !StorageVisibilityPolicy.shouldTriggerMediaScan(
            targetFile,
            context.filesDir,
            context.getExternalFilesDir(null)
        )
    }

    private fun shouldTriggerMediaScan(context: Context, file: File): Boolean {
        return StorageVisibilityPolicy.shouldTriggerMediaScan(
            file,
            context.filesDir,
            context.getExternalFilesDir(null)
        )
    }

    private fun migrateLegacyPrivateSpace(targetDir: File) {
        val legacyDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            ".private"
        )
        val legacyFiles = legacyDir.listFiles()?.filter { it.isFile }.orEmpty()
        if (legacyFiles.isEmpty()) {
            return
        }

        legacyFiles.forEach { legacyFile ->
            val targetFile = File(targetDir, legacyFile.name)
            if (targetFile.exists()) {
                legacyFile.delete()
                return@forEach
            }
            runCatching {
                legacyFile.copyTo(targetFile, overwrite = false)
                legacyFile.delete()
            }.onFailure {
                AppLogger.e("Failed to migrate legacy private file ${legacyFile.absolutePath}: ${it.message}")
            }
        }

        if (legacyDir.exists() && legacyDir.listFiles().isNullOrEmpty()) {
            legacyDir.delete()
        }
    }

    private fun moveMediaToPrivateStorage(context: Context, from: Uri, targetFile: File): Boolean {
        return try {
            targetFile.parentFile?.mkdirs()
            if (targetFile.exists()) {
                return false
            }

            val copied = openInputStream(context, from)?.use { inputStream ->
                targetFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                true
            } ?: false

            if (!copied) {
                return false
            }

            if (deleteSourceAfterMove(context, from)) {
                true
            } else {
                targetFile.delete()
                false
            }
        } catch (e: Throwable) {
            AppLogger.e("Error moving media into private storage ${e.message}")
            targetFile.delete()
            false
        }
    }

    private fun openInputStream(context: Context, from: Uri) =
        if (isFileApiSupportedByUri(context, from)) {
            from.toFile().inputStream()
        } else {
            context.contentResolver.openInputStream(from)
        }

    private fun deleteSourceAfterMove(context: Context, from: Uri): Boolean {
        return if (isFileApiSupportedByUri(context, from)) {
            from.toFile().delete()
        } else {
            deleteDownloadedVideoContent(context, from)
            !isUriExists(context, from)
        }
    }

    private fun getTmpDataDir(context: Context, isExternal: Boolean): File {
        val path = if (isExternal) {
            "${context.getExternalFilesDir(null)}/$TMP_DATA_FOLDER_NAME"
        } else {
            "${context.filesDir.absolutePath}/$TMP_DATA_FOLDER_NAME"
        }

        val file = File(path)
        if (!file.exists()) {
            file.mkdirs()
        }

        return file
    }

    private fun getPrivateDownloadsDir(context: Context, isExternal: Boolean): File {
        val path = if (isExternal) {
            "${context.getExternalFilesDir(null)}/$FOLDER_NAME"
        } else {
            "${context.filesDir.absolutePath}/$FOLDER_NAME"
        }

        return File(path)
    }

    private fun deleteDownloadedVideoContent(context: Context, uri: Uri) {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            DocumentsContract.deleteDocument(context.contentResolver, uri)
        } else {
            context.contentResolver.delete(uri, null, null)
        }
    }

    private fun isExternalUri(uri: Uri): Boolean {
        val context = ContextUtils.getApplicationContext()

        val ext1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            null
        }
        val ext2 = Uri.fromFile(context.getExternalFilesDir(null))
        val ext3 =
            Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))

        val result = uri.toString().contains(ext1.toString()) || uri.toString()
            .contains(ext2.toString()) || uri.toString().contains(ext3.toString())

        return result
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun moveFileToDownloadsFolder(
        contentResolver: ContentResolver, sourceFile: File, fileName: String
    ): Boolean {
        AppLogger.d(
            "moveFileToDownloadsFoldermoveFileToDownloadsFolder $sourceFile $fileName"
        )
        val isAudio = sourceFile.extension == "mp3"

        // Check if there is enough free space in the Downloads folder
        val downloadsDirectory = folderDir
        val isFolderExternal = isExternalUri(folderDir.toUri())
        val availableSpace = downloadsDirectory.freeSpace

        if (availableSpace < sourceFile.length()) {
            // Handle the case where there is not enough free space
            throw Error("Not available space $availableSpace, file size: ${sourceFile.length()}")
        }

        // Create a ContentValues object to specify the file details
        var name = fileName
        var counter = 1
        while (isDownloadExists(contentResolver, name)) {
            name = "$name($counter)"
            counter++
        }

        val cleaned = FileNameCleaner.cleanFileName(name)
        val values = ContentValues().apply {
            val mimeType = if (isAudio) "audio/mpeg" else "video/mp4"
            put(MediaStore.MediaColumns.DISPLAY_NAME, cleaned)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, managedDownloadRelativePath())
        }

        // Insert the file into the Downloads collection
        val collectionUri = if (isFolderExternal) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Downloads.INTERNAL_CONTENT_URI
        }
        var fileUri = contentResolver.insert(collectionUri, values)
        if (fileUri == null) {
            values.put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                cleaned.replace("mp4", "").replace("mp3", "") + "_e"
            )
            fileUri = contentResolver.insert(collectionUri, values)
        }

        // Copy the file to the Downloads folder
        val isMoved = fileUri?.let { uri ->
            try {
                // 1. Acquire a lock on the source file
                val randomAccessFile = RandomAccessFile(sourceFile, "rw")
                val fileChannel: FileChannel = randomAccessFile.channel
                val fileLock: FileLock = fileChannel.lock()

                try {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        val copied = sourceFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        if (copied > 0) {
                            AppLogger.d("Source removing... $sourceFile")
                            // Delete the source file
                            sourceFile.delete()
                            true
                        } else {
                            AppLogger.d("Source move error $sourceFile")
                            false
                        }
                    }
                } finally {
                    // 3. Release the lock in the finally block
                    fileLock.release()
                    randomAccessFile.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AppLogger.d("Source move error $sourceFile $e")
                false
            }
        }
        return isMoved ?: false
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isDownloadExists(contentResolver: ContentResolver, displayName: String): Boolean {
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(displayName)

        val uri = if (isExternalUri(folderDir.toUri())) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Downloads.INTERNAL_CONTENT_URI
        }
        val cursor = contentResolver.query(
            uri, projection, selection, selectionArgs, null
        )

        val exists = cursor?.moveToFirst() ?: false
        cursor?.close()

        return exists
    }

    private fun scanFile(context: Context, file: File) {
        try {
            val fileUri = Uri.fromFile(file)
            val mediaScanIntent = Intent(ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                data = fileUri
            }
            context.applicationContext.sendBroadcast(mediaScanIntent)
        } catch (error: Exception) {
            error.printStackTrace()
        }
        try {
            val mimeType =
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)

            MediaScannerConnection.scanFile(
                context.applicationContext,
                arrayOf(file.absolutePath),
                arrayOf(mimeType),
                null
            )
        } catch (e: Exception) {
            AppLogger.e("Error triggering media scan ${e.message}")
        }
    }
}

private fun Uri.toFileOrNull(): File? {
    return runCatching { toFile() }.getOrNull()
}

internal fun isDownloadedMediaFile(file: File): Boolean {
    return file.isFile && MediaFormatSupport.normalizeExtension(file.extension) in MediaFormatSupport.downloadedMediaExtensions
}

internal fun resolveManagedPublicDownloadDir(downloadsRoot: File): File {
    return File(downloadsRoot, FileUtil.FOLDER_NAME)
}

internal fun resolveDownloadDirectory(
    sdkInt: Int,
    isExternalStorageUse: Boolean,
    isAppDataDirUse: Boolean,
    publicDownloadsRoot: File,
    externalFilesRoot: File?,
    internalFilesRoot: File
): File {
    return when {
        isExternalStorageUse && !isAppDataDirUse && sdkInt >= Build.VERSION_CODES.Q -> {
            resolveManagedPublicDownloadDir(publicDownloadsRoot)
        }

        isExternalStorageUse -> {
            File(externalFilesRoot, FileUtil.FOLDER_NAME)
        }

        else -> {
            File(internalFilesRoot, FileUtil.FOLDER_NAME)
        }
    }
}

internal fun managedDownloadRelativePath(): String {
    return "${Environment.DIRECTORY_DOWNLOADS}/${FileUtil.FOLDER_NAME}"
}

internal fun collectManagedMediaFiles(rootDir: File): List<File> {
    if (!rootDir.exists()) {
        return emptyList()
    }
    return rootDir.listFiles()
        ?.asSequence()
        ?.filter(::isDownloadedMediaFile)
        ?.toList()
        ?: emptyList()
}

object FileNameCleaner {
    private const val MAX_FILE_NAME_LENGTH = 100
    private val illegalChars = intArrayOf(
        34,
        60,
        62,
        124,
        0,
        1,
        2,
        3,
        4,
        5,
        6,
        7,
        8,
        9,
        10,
        11,
        12,
        13,
        14,
        15,
        16,
        17,
        18,
        19,
        20,
        21,
        22,
        23,
        24,
        25,
        26,
        27,
        28,
        29,
        30,
        31,
        58,
        42,
        63,
        92,
        47
    )

    init {
        Arrays.sort(illegalChars)
    }

    fun cleanFileName(badFileName: String): String {
        val cleanName = StringBuilder()
        for (element in badFileName) {
            val c = element.code
            if (Arrays.binarySearch(illegalChars, c) < 0) {
                cleanName.append(c.toChar())
            }
        }
        var finalName = cleanName.toString()
            .replace(".mp3", "")
            .replace(".mp4", "")
            .replace("/", "").replace("\\", "")
            .replace(":", "")
            .replace("*", "")
            .replace("?", "")
            .replace("\"", "")
            .replace("`", "")
            .replace("\'", "")
            .replace("<", "")
            .replace(">", "")
            .replace(".", "_")
            .replace("|", "")
            .replace(Regex("\\s*-\\s*"), "-")
            .replace(" ", "_").trim()
        if (finalName.isEmpty()) {
            finalName = "Untitled"
        }

        if (finalName.length > MAX_FILE_NAME_LENGTH) {
            return finalName.substring(0, MAX_FILE_NAME_LENGTH)
        }

        return finalName
    }
}
