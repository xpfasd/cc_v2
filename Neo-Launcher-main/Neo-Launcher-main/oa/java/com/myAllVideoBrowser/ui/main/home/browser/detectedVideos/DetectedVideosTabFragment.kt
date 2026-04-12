package com.myAllVideoBrowser.ui.main.home.browser.detectedVideos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.data.local.room.entity.ProgressInfo
import com.myAllVideoBrowser.data.local.room.entity.VideoFormatEntity
import com.myAllVideoBrowser.data.local.room.entity.VideoInfo
import com.myAllVideoBrowser.data.repository.ProgressRepository
import com.myAllVideoBrowser.databinding.FragmentDetectedVideosTabBinding
import com.myAllVideoBrowser.ui.component.adapter.DetectedDownloadItemListener
import com.myAllVideoBrowser.ui.component.adapter.DownloadTabListener
import com.myAllVideoBrowser.ui.component.adapter.VideoInfoAdapter
import com.myAllVideoBrowser.ui.main.base.BaseFragment
import com.myAllVideoBrowser.ui.main.home.MainActivity
import com.myAllVideoBrowser.util.DownloadStatusText
import com.myAllVideoBrowser.util.FileNameCleaner
import com.myAllVideoBrowser.util.FileUtil
import com.myAllVideoBrowser.util.IntentUtil
import com.myAllVideoBrowser.util.MediaFormatSupport
import com.myAllVideoBrowser.util.downloaders.custom_downloader.CustomRegularDownloader
import com.myAllVideoBrowser.util.downloaders.generic_downloader.models.VideoTaskState
import com.myAllVideoBrowser.util.downloaders.super_x_downloader.SuperXDownloader
import com.myAllVideoBrowser.util.downloaders.youtubedl_downloader.YoutubeDlDownloader
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetectedVideosTabFragment : BaseFragment() {
    var detectedVideosTabViewModel: VideoDetectionTabViewModel? = null
    var candidateFormatListener: DownloadTabListener? = null

    @Inject
    lateinit var mainActivity: MainActivity

    @Inject
    lateinit var fileUtil: FileUtil

    @Inject
    lateinit var intentUtil: IntentUtil

    @Inject
    lateinit var progressRepository: ProgressRepository

    private lateinit var binding: FragmentDetectedVideosTabBinding
    private lateinit var adapter: VideoInfoAdapter
    private var progressInfos: List<ProgressInfo> = emptyList()
    private var progressDisposable: Disposable? = null
    private var downloadedFilesSnapshot: Map<String, FileUtil.DownloadedMediaFile> = emptyMap()

    private val detectedVideosCallback = object : OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            adapter.setData(detectedVideosTabViewModel?.detectedVideosList?.get()?.toList() ?: emptyList())
        }
    }

    companion object {
        fun newInstance() = DetectedVideosTabFragment()
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        val dialogModel = detectedVideosTabViewModel
        val listener = candidateFormatListener
        if (dialogModel == null || listener == null) {
            Toast.makeText(context, R.string.detected_videos_unexpected_error, Toast.LENGTH_SHORT)
                .show()
            parentFragmentManager.popBackStack()
            binding = FragmentDetectedVideosTabBinding.inflate(inflater, container, false)
            return binding.root
        }

        adapter = VideoInfoAdapter(
            dialogModel?.detectedVideosList?.get()?.toList() ?: emptyList(),
            dialogModel!!,
            itemListener,
            ::resolveRowState,
            ::resolveMetadata
        )

        binding = FragmentDetectedVideosTabBinding.inflate(inflater, container, false).apply {
            videoInfoList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            videoInfoList.adapter = adapter
            scrimDismissArea.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        detectedVideosTabViewModel?.detectedVideosList?.addOnPropertyChangedCallback(detectedVideosCallback)
        refreshDownloadedFilesSnapshot()
        subscribeProgress()
    }

    override fun onDestroyView() {
        detectedVideosTabViewModel?.detectedVideosList?.removeOnPropertyChangedCallback(detectedVideosCallback)
        progressDisposable?.dispose()
        super.onDestroyView()
    }

    private fun subscribeProgress() {
        progressDisposable?.dispose()
        progressDisposable = progressRepository.getProgressInfos()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ infos ->
                progressInfos = infos
                refreshDownloadedFilesSnapshot()
                adapter.notifyDataSetChanged()
            }, {
                it.printStackTrace()
            })
    }

    private fun refreshDownloadedFilesSnapshot() {
        lifecycleScope.launch(Dispatchers.IO) {
            val snapshot = fileUtil.listFiles
            launch(Dispatchers.Main) {
                downloadedFilesSnapshot = snapshot
                if (isAdded) {
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun resolveRowState(videoInfo: VideoInfo): DetectedDownloadRowState {
        val progressInfo = findProgressInfo(videoInfo)
        return DetectedDownloadRowStateResolver.resolve(
            videoInfo = videoInfo,
            progressInfo = progressInfo,
            isDownloaded = isDownloadedInCurrentSession(videoInfo, progressInfo)
        )
    }

    private fun resolveMetadata(videoInfo: VideoInfo): String {
        val progressInfo = findProgressInfo(videoInfo)
        if (progressInfo != null) {
            return DownloadStatusText.formatProgressSummary(
                requireContext(),
                progressInfo.progressDownloaded,
                progressInfo.progressTotal,
                progressInfo.downloadStatus
            )
        }

        val downloadedUri = findDownloadedUri(videoInfo)
        if (downloadedUri != null) {
            val fileSize = fileUtil.getContentLength(requireContext(), downloadedUri)
            val sizeText = FileUtil.getFileSizeReadable(fileSize.toDouble())
            val timestamp = getDownloadedTimestamp(videoInfo)
            if (timestamp > 0L) {
                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                    .format(Date(timestamp))
                return "$sizeText | $formattedDate"
            }
            return sizeText
        }

        val format = selectedFormat(videoInfo)
        val bytes = when {
            (format?.fileSizeApproximate ?: 0L) > 0L -> format?.fileSizeApproximate ?: 0L
            (format?.fileSize ?: 0L) > 0L -> format?.fileSize ?: 0L
            else -> 0L
        }
        return if (bytes > 0L) {
            "${FileUtil.getFileSizeReadable(bytes.toDouble())} | ${mediaLabel(videoInfo)}"
        } else {
            getString(R.string.detected_download_ready)
        }
    }

    private fun mediaLabel(videoInfo: VideoInfo): String {
        return when {
            DirectMediaSupport.isImageExtension(videoInfo.ext) -> getString(R.string.detected_media_type_image)
            MediaFormatSupport.isAudioExtension(videoInfo.ext) -> getString(R.string.detected_media_type_audio)
            else -> getString(R.string.detected_media_type_video)
        }
    }

    private fun selectedFormat(videoInfo: VideoInfo): VideoFormatEntity? {
        val selected = detectedVideosTabViewModel?.selectedFormats?.get()?.get(videoInfo.id)
        return videoInfo.formats.formats.find { it.format == selected } ?: videoInfo.formats.formats.lastOrNull()
    }

    private fun currentTitle(videoInfo: VideoInfo): String {
        return detectedVideosTabViewModel?.formatsTitles?.get()?.get(videoInfo.id)?.ifBlank { videoInfo.title }
            ?: videoInfo.title
    }

    private fun targetFileName(videoInfo: VideoInfo): String {
        val cleanTitle = FileNameCleaner.cleanFileName(currentTitle(videoInfo))
        val ext = selectedFormat(videoInfo)?.ext?.takeIf { it.isNotBlank() } ?: videoInfo.ext
        return if (cleanTitle.endsWith(".$ext", ignoreCase = true)) cleanTitle else "$cleanTitle.$ext"
    }

    private fun findProgressInfo(videoInfo: VideoInfo): ProgressInfo? {
        val fileName = targetFileName(videoInfo)
        val selectedUrl = selectedFormat(videoInfo)?.url
        return progressInfos.firstOrNull { progressInfo ->
            progressInfo.videoInfo.name.equals(fileName, ignoreCase = true) ||
                (selectedUrl != null && progressInfo.videoInfo.firstUrlToString == selectedUrl)
        }
    }

    private fun findDownloadedUri(videoInfo: VideoInfo): Uri? {
        return findDownloadedUriFromSnapshot(downloadedFilesSnapshot, targetFileName(videoInfo))
    }

    private fun getDownloadedTimestamp(videoInfo: VideoInfo): Long {
        return findDownloadedUri(videoInfo)?.path?.let { path ->
            kotlin.runCatching { java.io.File(path).lastModified() }.getOrDefault(0L)
        } ?: 0L
    }

    private fun startDownload(videoInfo: VideoInfo) {
        val format = selectedFormat(videoInfo)?.format ?: ""
        detectedVideosTabViewModel?.markSessionDownloadRequested(targetFileName(videoInfo))
        candidateFormatListener?.onDownloadVideo(videoInfo, format, currentTitle(videoInfo))
        adapter.notifyDataSetChanged()
    }

    private fun isDownloadedInCurrentSession(
        videoInfo: VideoInfo,
        progressInfo: ProgressInfo?
    ): Boolean {
        val targetName = targetFileName(videoInfo)
        val requestedThisSession =
            detectedVideosTabViewModel?.wasRequestedThisSession(targetName) == true
        return isDownloadedInCurrentSession(
            requestedThisSession = requestedThisSession,
            downloadedFileExists = findDownloadedUri(videoInfo) != null,
            progressInfo = progressInfo
        )
    }

    private fun pauseDownload(progressInfo: ProgressInfo) {
        if (progressInfo.videoInfo.isRegularDownload) {
            CustomRegularDownloader.pauseDownload(requireContext().applicationContext, progressInfo)
            return
        }

        val updated = progressInfo.copy(downloadStatus = VideoTaskState.PAUSE)
        lifecycleScope.launch(Dispatchers.IO) {
            progressRepository.saveProgressInfo(updated)
        }
        if (progressInfo.videoInfo.isDetectedBySuperX) {
            SuperXDownloader.pauseDownload(requireContext().applicationContext, updated)
        } else {
            YoutubeDlDownloader.pauseDownload(requireContext().applicationContext, updated)
        }
    }

    private fun resumeDownload(progressInfo: ProgressInfo) {
        if (progressInfo.videoInfo.isRegularDownload) {
            CustomRegularDownloader.resumeDownload(requireContext().applicationContext, progressInfo)
            return
        }

        val updated = progressInfo.copy(downloadStatus = VideoTaskState.PREPARE)
        lifecycleScope.launch(Dispatchers.IO) {
            progressRepository.saveProgressInfo(updated)
        }
        if (progressInfo.videoInfo.isDetectedBySuperX) {
            SuperXDownloader.resumeDownload(requireContext().applicationContext, updated)
        } else {
            YoutubeDlDownloader.resumeDownload(requireContext().applicationContext, updated)
        }
    }

    private fun cancelDownload(progressInfo: ProgressInfo, removeFile: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            progressRepository.deleteProgressInfo(progressInfo)
        }
        progressInfos = progressInfos.filterNot { it.id == progressInfo.id }
        if (progressInfo.videoInfo.isRegularDownload) {
            CustomRegularDownloader.cancelDownload(
                requireContext().applicationContext,
                progressInfo,
                removeFile
            )
        } else if (progressInfo.videoInfo.isDetectedBySuperX) {
            SuperXDownloader.cancelDownload(requireContext().applicationContext, progressInfo, removeFile)
        } else {
            YoutubeDlDownloader.cancelDownload(requireContext().applicationContext, progressInfo, removeFile)
        }
    }

    private fun openDownloaded(videoInfo: VideoInfo) {
        val downloadedUri = findDownloadedUri(videoInfo) ?: return
        intentUtil.openMedia(requireContext(), downloadedUri, targetFileName(videoInfo))
    }

    private val itemListener = object : DetectedDownloadItemListener {
        override fun onRowClicked(videoInfo: VideoInfo) {
            if (isDownloadedInCurrentSession(videoInfo, findProgressInfo(videoInfo))) {
                openDownloaded(videoInfo)
                return
            }
            val format = selectedFormat(videoInfo)?.format ?: ""
            candidateFormatListener?.onPreviewVideo(videoInfo, format, false)
        }

        override fun onActionClicked(videoInfo: VideoInfo, actionState: DetectedDownloadActionState) {
            when (actionState) {
                DetectedDownloadActionState.DOWNLOAD -> startDownload(videoInfo)
                DetectedDownloadActionState.PAUSE -> findProgressInfo(videoInfo)?.let(::pauseDownload)
                DetectedDownloadActionState.START -> {
                    val progressInfo = findProgressInfo(videoInfo)
                    if (progressInfo != null) {
                        resumeDownload(progressInfo)
                    } else {
                        startDownload(videoInfo)
                    }
                }

                DetectedDownloadActionState.DONE -> openDownloaded(videoInfo)
                DetectedDownloadActionState.FAIL -> {
                    findProgressInfo(videoInfo)?.let {
                        cancelDownload(it, true)
                    }
                    startDownload(videoInfo)
                }
            }
        }
    }
}

internal fun findDownloadedUriFromSnapshot(
    snapshot: Map<String, FileUtil.DownloadedMediaFile>,
    targetName: String
): Uri? {
    return snapshot.entries.firstOrNull { entry ->
        entry.key.equals(targetName, ignoreCase = true)
    }?.value?.uri
}

internal fun isDownloadedInCurrentSession(
    requestedThisSession: Boolean,
    downloadedFileExists: Boolean,
    progressInfo: ProgressInfo?
): Boolean {
    if (!requestedThisSession) {
        return false
    }

    return downloadedFileExists || progressInfo?.downloadStatus == VideoTaskState.SUCCESS
}
