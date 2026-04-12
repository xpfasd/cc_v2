package com.myAllVideoBrowser.util

import android.content.Context
import androidx.annotation.StringRes
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.util.downloaders.generic_downloader.models.VideoTaskState

object DownloadStatusText {
    @StringRes
    fun statusLabelRes(status: Int): Int = when (status) {
        VideoTaskState.PREPARE -> R.string.download_status_preparing
        VideoTaskState.PENDING -> R.string.download_status_pending
        VideoTaskState.DOWNLOADING -> R.string.download_status_downloading
        VideoTaskState.PAUSE -> R.string.download_status_paused
        VideoTaskState.SUCCESS -> R.string.download_status_success
        VideoTaskState.ENOSPC, VideoTaskState.ERROR -> R.string.download_status_failed
        VideoTaskState.CANCELED -> R.string.download_status_canceled
        else -> R.string.download_status_unknown
    }

    fun formatProgressSummary(
        context: Context,
        progressDownloaded: Long,
        progressTotal: Long,
        status: Int
    ): String {
        val downloadedText = FileUtil.getFileSizeReadable(progressDownloaded.toDouble())
        val totalText = FileUtil.getFileSizeReadable(progressTotal.toDouble())
        val statusText = context.getString(statusLabelRes(status))
        return "$downloadedText / $totalText - $statusText"
    }
}
