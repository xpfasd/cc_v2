package com.myAllVideoBrowser.ui.main.progress

import androidx.annotation.VisibleForTesting
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
//import com.allVideoDownloaderXmaster.OpenForTesting
import com.myAllVideoBrowser.data.local.room.entity.ProgressInfo
import com.myAllVideoBrowser.data.local.room.entity.VideoInfo
import com.myAllVideoBrowser.data.repository.ProgressRepository
import com.myAllVideoBrowser.data.repository.VideoRepository
import com.myAllVideoBrowser.ui.main.base.BaseViewModel
import com.myAllVideoBrowser.util.ContextUtils
import com.myAllVideoBrowser.util.CopyrightRestrictedSitePolicy
import com.myAllVideoBrowser.util.FileUtil
import com.myAllVideoBrowser.util.SingleLiveEvent
import com.myAllVideoBrowser.util.downloaders.generic_downloader.models.VideoTaskState
import com.myAllVideoBrowser.util.downloaders.custom_downloader.CustomRegularDownloader
import com.myAllVideoBrowser.util.downloaders.super_x_downloader.SuperXDownloader
import com.myAllVideoBrowser.util.downloaders.youtubedl_downloader.YoutubeDlDownloader
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

//@OpenForTesting
class ProgressViewModel @Inject constructor(
    private val fileUtil: FileUtil,
    private val progressRepository: ProgressRepository,
    private val videoRepository: VideoRepository,
) : BaseViewModel() {
    @VisibleForTesting
    internal val compositeDisposable: CompositeDisposable = CompositeDisposable()

    var progressInfos: ObservableField<List<ProgressInfo>> = ObservableField(emptyList())
    val isParsingUrl = ObservableBoolean(false)
    val parsedVideoEvent = SingleLiveEvent<VideoInfo>()
    val parseFailedEvent = SingleLiveEvent<Unit?>()
    val copyrightRestrictedEvent = SingleLiveEvent<Unit?>()
    private val executor = Executors.newFixedThreadPool(3).asCoroutineDispatcher()
    private val executor2 = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

    override fun start() {
        downloadProgressStartListen()
    }

    override fun stop() {
        compositeDisposable.clear()
    }

    // wtf??? fix what?
    // TODO: strange, should fix
    fun stopAndSaveDownload(id: Long) {
        val inf = progressInfos.get()?.find { it.downloadId == id }

        if (inf?.videoInfo?.isRegularDownload == false) {
            inf.let {
                if (inf.videoInfo.isDetectedBySuperX) {
                    SuperXDownloader.stopAndSaveDownload(
                        ContextUtils.getApplicationContext(), it
                    )
                } else {
                    YoutubeDlDownloader.stopAndSaveDownload(
                        ContextUtils.getApplicationContext(), it
                    )
                }
            }
        } else {
            inf?.let {
                CustomRegularDownloader.stopAndSaveDownload(
                    ContextUtils.getApplicationContext(), it
                )
            }
        }
    }

    fun cancelDownload(id: Long, removeFile: Boolean) {
        val inf = progressInfos.get()?.find { it.downloadId == id }
        inf?.let { progressInfo ->
            deleteProgressInfo(progressInfo) { info ->
                if (info.videoInfo.isRegularDownload) {
                    CustomRegularDownloader.cancelDownload(
                        ContextUtils.getApplicationContext(),
                        inf,
                        removeFile
                    )
                } else {
                    info.let {
                        if (inf.videoInfo.isDetectedBySuperX) {
                            SuperXDownloader.cancelDownload(
                                ContextUtils.getApplicationContext(), it, removeFile
                            )
                        } else {
                            YoutubeDlDownloader.cancelDownload(
                                ContextUtils.getApplicationContext(), it, removeFile
                            )
                        }
                    }
                }
                val newList = progressInfos.get()?.filter { it.id != info.id }
                progressInfos.set(newList?.sortedBy { it.id })
            }
        }
    }

    fun pauseDownload(id: Long) {
        val inf = progressInfos.get()?.find { it.downloadId == id }

        if (inf?.videoInfo?.isRegularDownload == true) {
            CustomRegularDownloader.pauseDownload(ContextUtils.getApplicationContext(), inf)
        } else {
            val updated = inf?.copy(downloadStatus = VideoTaskState.PAUSE)
            if (updated != null) {
                saveProgressInfo(updated) { info ->
                    if (inf.videoInfo.isDetectedBySuperX) {
                        SuperXDownloader.pauseDownload(ContextUtils.getApplicationContext(), info)
                    } else {
                        YoutubeDlDownloader.pauseDownload(
                            ContextUtils.getApplicationContext(),
                            info
                        )
                    }
                }
            }
        }
    }

    fun resumeDownload(id: Long) {
        val inf = progressInfos.get()?.find { it.downloadId == id }

        if (inf?.videoInfo?.isRegularDownload == true) {
            CustomRegularDownloader.resumeDownload(ContextUtils.getApplicationContext(), inf)
        } else {
            inf?.let {
                val updated = inf.copy(downloadStatus = VideoTaskState.PREPARE)

                saveProgressInfo(updated) { info ->
                    if (inf.videoInfo.isDetectedBySuperX) {
                        SuperXDownloader.resumeDownload(
                            ContextUtils.getApplicationContext(),
                            info
                        )
                    } else {
                        YoutubeDlDownloader.resumeDownload(
                            ContextUtils.getApplicationContext(),
                            info
                        )
                    }
                }
            }
        }
    }

    fun downloadVideo(videoInfo: VideoInfo?) {
        val context = ContextUtils.getApplicationContext()

        videoInfo?.let {
            if (isRestrictedVideoInfo(it)) {
                copyrightRestrictedEvent.call()
                return
            }

            if (!fileUtil.folderDir.exists() && !fileUtil.folderDir.mkdirs()) {
                return
            }

            val downloadId = videoInfo.id.hashCode().toLong()
            val progressInfo = ProgressInfo(
                id = videoInfo.id,
                downloadId = downloadId,
                videoInfo = videoInfo,
                isM3u8 = videoInfo.isM3u8
            )

            saveProgressInfo(progressInfo) { info ->
                if (info.videoInfo.isRegularDownload) {
                    CustomRegularDownloader.startDownload(context, info.videoInfo)
                } else {
                    if (info.videoInfo.isDetectedBySuperX) {
                        SuperXDownloader.startDownload(context, info.videoInfo)
                    } else {
                        YoutubeDlDownloader.startDownload(context, info.videoInfo)
                    }
                }
            }
        }
    }

    fun parseDownloadUrl(url: String, isAudioCheck: Boolean) {
        val normalizedUrl = url.trim()
        if (normalizedUrl.isBlank() || isParsingUrl.get()) {
            if (normalizedUrl.isBlank()) {
                parseFailedEvent.call()
            }
            return
        }

        if (CopyrightRestrictedSitePolicy.isDownloadRestrictedUrl(normalizedUrl)) {
            copyrightRestrictedEvent.call()
            return
        }

        isParsingUrl.set(true)
        viewModelScope.launch(executor2) {
            val parsedInfo = try {
                val request = Request.Builder().url(normalizedUrl).build()
                val lowerCaseUrl = normalizedUrl.lowercase()
                when {
                    lowerCaseUrl.contains(".m3u8") -> videoRepository.getVideoInfoBySuperXDetector(
                        request,
                        isM3u8 = true,
                        isAudioCheck = isAudioCheck
                    ) ?: videoRepository.getVideoInfo(
                        request,
                        isM3u8OrMpd = true,
                        isAudioCheck = isAudioCheck
                    )

                    lowerCaseUrl.contains(".mpd") -> videoRepository.getVideoInfoBySuperXDetector(
                        request,
                        isMpd = true,
                        isAudioCheck = isAudioCheck
                    ) ?: videoRepository.getVideoInfo(
                        request,
                        isM3u8OrMpd = true,
                        isAudioCheck = isAudioCheck
                    )

                    else -> videoRepository.getVideoInfo(
                        request,
                        isAudioCheck = isAudioCheck
                    )
                }
            } catch (_: Throwable) {
                null
            }

            withContext(Dispatchers.Main) {
                isParsingUrl.set(false)
                if (parsedInfo != null && parsedInfo.id.isNotBlank() && parsedInfo.formats.formats.isNotEmpty()) {
                    parsedVideoEvent.value = parsedInfo
                } else {
                    parseFailedEvent.call()
                }
            }
        }
    }

    private fun saveProgressInfo(
        progressInfo: ProgressInfo,
        onSuccess: (ProgressInfo) -> Unit = {}
    ) {
        viewModelScope.launch(executor2) {
            progressRepository.saveProgressInfo(progressInfo)
            onSuccess(progressInfo)
        }
    }

    private fun deleteProgressInfo(
        progressInfo: ProgressInfo,
        onSuccess: (ProgressInfo) -> Unit = {}
    ) {
        viewModelScope.launch(executor2) {
            progressRepository.deleteProgressInfo(progressInfo)
            onSuccess(progressInfo)
        }
    }

    @VisibleForTesting
    internal fun downloadProgressStartListen() {
        viewModelScope.launch(executor) {
            progressObservable().doOnError {
                it.printStackTrace()
            }.blockingForEach { progressInfoList ->
                progressInfos.set(progressInfoList.sortedBy { it.id })
            }
        }
    }

    private fun progressObservable(): Observable<List<ProgressInfo>> {
        val youtubeDlDownloads = Observable.interval(1000, TimeUnit.MILLISECONDS).flatMap {
            progressRepository.getProgressInfos().take(1).flatMap {
                val filtered = it.filter { info -> info.downloadStatus != VideoTaskState.SUCCESS }
                Observable.just(filtered).toFlowable(BackpressureStrategy.LATEST).take(1)
            }.toObservable().doOnError { error ->
                error.printStackTrace()
            }
        }

        return youtubeDlDownloads
    }

    private fun isRestrictedVideoInfo(videoInfo: VideoInfo): Boolean {
        return CopyrightRestrictedSitePolicy.isDownloadRestrictedUrl(videoInfo.originalUrl) ||
            CopyrightRestrictedSitePolicy.isDownloadRestrictedUrl(videoInfo.firstUrlToString)
    }
}
