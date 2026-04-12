package com.myAllVideoBrowser.ui.main.video

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
//import com.allVideoDownloaderXmaster.OpenForTesting
import com.myAllVideoBrowser.data.local.model.LocalVideo
import com.myAllVideoBrowser.ui.main.base.BaseViewModel
import com.myAllVideoBrowser.util.AppLogger
import com.myAllVideoBrowser.util.ContextUtils
import com.myAllVideoBrowser.util.FileUtil
import com.myAllVideoBrowser.util.MediaFormatSupport
import com.myAllVideoBrowser.util.SingleLiveEvent
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

//@OpenForTesting
class VideoViewModel @Inject constructor(
    private val fileUtil: FileUtil,
) : BaseViewModel() {

    enum class ContentSource {
        DOWNLOADS,
        PRIVATE_SPACE
    }

    enum class MediaFilter {
        ALL,
        VIDEO,
        IMAGE
    }

    enum class VideoSortMode {
        NAME_ASC,
        NAME_DESC,
        LAST_EDIT_DESC,
        LAST_EDIT_ASC,
        SIZE_DESC,
        SIZE_ASC
    }

    companion object {
        const val FILE_EXIST_ERROR_CODE = 1
        const val FILE_INVALID_ERROR_CODE = 2
    }

    var localVideos: ObservableField<MutableList<LocalVideo>> = ObservableField(mutableListOf())
    val sortMode: ObservableField<VideoSortMode> = ObservableField(VideoSortMode.LAST_EDIT_DESC)
    val mediaFilter: ObservableField<MediaFilter> = ObservableField(MediaFilter.ALL)
    val contentSource: ObservableField<ContentSource> = ObservableField(ContentSource.DOWNLOADS)
    val searchQuery: ObservableField<String> = ObservableField("")

    val renameErrorEvent = SingleLiveEvent<Int>()
    val shareEvent = SingleLiveEvent<Uri>()

    override fun start() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(1000)
                refreshVideos()
            }
        }
    }


    override fun stop() {
    }

    fun setSortMode(mode: VideoSortMode) {
        sortMode.set(mode)
        refreshVideos()
    }

    fun setMediaFilter(filter: MediaFilter) {
        mediaFilter.set(filter)
        refreshVideos()
    }

    fun setContentSource(source: ContentSource) {
        contentSource.set(source)
        refreshVideos()
    }

    fun setSearchQuery(query: String) {
        searchQuery.set(query.trim())
        refreshVideos()
    }

    fun refreshVideos() {
        viewModelScope.launch(Dispatchers.IO) {
            val source = contentSource.get() ?: ContentSource.DOWNLOADS
            val filter = mediaFilter.get() ?: MediaFilter.ALL
            val allFiles = getFilesList()
            val filteredFiles = filterVideos(allFiles)
            val searchedFiles = applyVideoNameSearch(filteredFiles, searchQuery.get().orEmpty())
            val newList = sortVideos(searchedFiles)
            AppLogger.d(
                "VideoViewModel.refreshVideos source=$source filter=$filter " +
                    "search=${searchQuery.get().orEmpty()} all=${allFiles.size} " +
                    "filtered=${filteredFiles.size} searched=${searchedFiles.size} sorted=${newList.size} " +
                    "sample=${newList.take(10).map { it.name }}"
            )
            withContext(Dispatchers.Main) {
                localVideos.set(newList.toMutableList())
            }
        }
    }

    private fun getFilesList(): List<LocalVideo> {
        if (contentSource.get() == ContentSource.PRIVATE_SPACE) {
            val privateFiles = getPrivateSpaceFilesList()
            AppLogger.d(
                "VideoViewModel.getFilesList source=PRIVATE_SPACE count=${privateFiles.size} " +
                    "sample=${privateFiles.take(10).map { it.name }}"
            )
            return privateFiles
        }
        val listVideos: MutableList<LocalVideo> = mutableListOf()
        fileUtil.listFiles.forEach { entry ->
            val fileUri = entry.value.uri
            val fileSize = fileUtil.getContentLength(ContextUtils.getApplicationContext(), fileUri)
            val readableSize = FileUtil.getFileSizeReadable(fileSize.toDouble())
            val video = LocalVideo(
                stableLocalVideoId(entry.key, fileUri.toString()),
                fileUri,
                entry.key
            )
            video.size = readableSize
            video.subtitle = buildSubtitle(video, fileSize)
            listVideos.add(video)
        }

        AppLogger.d(
            "VideoViewModel.getFilesList source=DOWNLOADS mapped=${listVideos.size} " +
                "sample=${listVideos.take(10).map { it.name }}"
        )

        return listVideos.toList()
    }

    private fun getPrivateSpaceFilesList(): List<LocalVideo> {
        val files = fileUtil.privateSpaceDir.listFiles()
            ?.filter { it.isFile }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()

        return files.map { file ->
            val fileSize = file.length()
            LocalVideo(
                id = file.absolutePath.hashCode().toLong(),
                uri = Uri.fromFile(file),
                name = file.name
            ).apply {
                size = FileUtil.getFileSizeReadable(fileSize.toDouble())
                subtitle = buildSubtitle(this, fileSize)
            }
        }
    }

    private fun filterVideos(videos: List<LocalVideo>): List<LocalVideo> {
        return when (mediaFilter.get() ?: MediaFilter.ALL) {
            MediaFilter.ALL -> videos
            MediaFilter.VIDEO -> videos.filter { it.isVideoFile() }
            MediaFilter.IMAGE -> videos.filter { it.isImageFile() }
        }
    }

    private fun sortVideos(videos: List<LocalVideo>): List<LocalVideo> {
        return when (sortMode.get() ?: VideoSortMode.LAST_EDIT_DESC) {
            VideoSortMode.NAME_ASC -> videos.sortedBy { it.name.lowercase() }
            VideoSortMode.NAME_DESC -> videos.sortedByDescending { it.name.lowercase() }
            VideoSortMode.LAST_EDIT_DESC -> videos.sortedByDescending { getLastModified(it) }
            VideoSortMode.LAST_EDIT_ASC -> videos.sortedBy { getLastModified(it) }
            VideoSortMode.SIZE_DESC -> videos.sortedByDescending { getFileSize(it) }
            VideoSortMode.SIZE_ASC -> videos.sortedBy { getFileSize(it) }
        }
    }

    private fun getFileSize(video: LocalVideo): Long {
        return try {
            fileUtil.getContentLength(ContextUtils.getApplicationContext(), video.uri)
        } catch (t: Throwable) {
            0L
        }
    }

    private fun getLastModified(video: LocalVideo): Long {
        return try {
            video.uri.toFile().lastModified()
        } catch (t: Throwable) {
            0L
        }
    }

    private fun buildSubtitle(video: LocalVideo, fileSize: Long): String {
        val sizeText = FileUtil.getFileSizeReadable(fileSize.toDouble())
        val lastModified = getLastModified(video)
        if (lastModified <= 0L) {
            return sizeText
        }
        val formatted = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            .format(Date(lastModified))
        return "$sizeText ·$formatted"
    }

    fun deleteVideo(context: Context, video: LocalVideo) {
        localVideos.get()?.find { it.uri.path == video.uri.path }?.let {
            fileUtil.deleteMedia(context, video.uri)
            refreshVideos()
        }
    }

    fun renameVideo(context: Context, uri: Uri, newName: String) {
        if (newName.isNotEmpty()) {
            val exists = fileUtil.isUriExists(context, uri)
            if (exists) {
                val isFileWithNameNotExists =
                    fileUtil.isFileWithNameNotExists(context, uri, newName)
                if (isFileWithNameNotExists) {
                    val newMediaNameUri = fileUtil.renameMedia(context, uri, newName)
                    if (newMediaNameUri != null) {
                        refreshVideos()
                        return
                    }
                }

                renameErrorEvent.value = FILE_EXIST_ERROR_CODE
                return
            }
        }

        renameErrorEvent.value = FILE_INVALID_ERROR_CODE
    }

    fun findVideoByName(downloadFilename: String?): Observable<LocalVideo> {
        return Observable.create { emitter ->
            val videos = getFilesList()
            val found =
                videos.find { it.name.contains(File(downloadFilename.toString()).name) }
            if (found != null) {
                emitter.onNext(found)
                emitter.onComplete()
            }
        }
    }

    private fun LocalVideo.isVideoFile(): Boolean {
        return MediaFormatSupport.isVideoExtension(name.substringAfterLast('.', ""))
    }

    private fun LocalVideo.isImageFile(): Boolean {
        return MediaFormatSupport.isImageExtension(name.substringAfterLast('.', ""))
    }
}

internal fun stableLocalVideoId(name: String, uri: String): Long {
    return "$uri|$name".hashCode().toLong()
}

internal fun applyVideoNameSearch(videos: List<LocalVideo>, query: String): List<LocalVideo> {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) {
        return videos
    }

    return videos.filter { video ->
        video.name.contains(normalizedQuery, ignoreCase = true)
    }
}
