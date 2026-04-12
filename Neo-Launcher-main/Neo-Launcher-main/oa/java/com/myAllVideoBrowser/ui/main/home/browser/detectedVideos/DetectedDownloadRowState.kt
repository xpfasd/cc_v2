package com.myAllVideoBrowser.ui.main.home.browser.detectedVideos

import com.myAllVideoBrowser.data.local.room.entity.ProgressInfo
import com.myAllVideoBrowser.data.local.room.entity.VideoInfo
import com.myAllVideoBrowser.util.downloaders.generic_downloader.models.VideoTaskState

enum class DetectedDownloadActionState {
    DOWNLOAD,
    PAUSE,
    START,
    DONE,
    FAIL
}

data class DetectedDownloadRowState(
    val actionState: DetectedDownloadActionState
)

object DetectedDownloadRowStateResolver {
    fun resolve(
        videoInfo: VideoInfo,
        progressInfo: ProgressInfo?,
        isDownloaded: Boolean
    ): DetectedDownloadRowState {
        if (isDownloaded) {
            return DetectedDownloadRowState(DetectedDownloadActionState.DONE)
        }

        val status = progressInfo?.downloadStatus
        return when (status) {
            VideoTaskState.DOWNLOADING,
            VideoTaskState.PENDING,
            VideoTaskState.PREPARE,
            VideoTaskState.START,
            VideoTaskState.PROXYREADY -> DetectedDownloadRowState(DetectedDownloadActionState.PAUSE)

            VideoTaskState.PAUSE -> DetectedDownloadRowState(DetectedDownloadActionState.START)

            VideoTaskState.ERROR,
            VideoTaskState.ENOSPC -> DetectedDownloadRowState(DetectedDownloadActionState.FAIL)

            else -> {
                if (videoInfo.downloadUrls.isNotEmpty() || videoInfo.formats.formats.isNotEmpty()) {
                    DetectedDownloadRowState(DetectedDownloadActionState.DOWNLOAD)
                } else {
                    DetectedDownloadRowState(DetectedDownloadActionState.FAIL)
                }
            }
        }
    }
}
