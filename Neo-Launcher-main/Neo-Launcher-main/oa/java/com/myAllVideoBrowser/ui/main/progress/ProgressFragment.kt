package com.myAllVideoBrowser.ui.main.progress

import android.content.ClipboardManager
import android.content.Intent
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.data.local.room.entity.ProgressInfo
import com.myAllVideoBrowser.data.local.room.entity.VideFormatEntityList
import com.myAllVideoBrowser.data.local.room.entity.VideoInfo
import com.myAllVideoBrowser.databinding.FragmentProgressBinding
import com.myAllVideoBrowser.ui.component.adapter.ProgressAdapter
import com.myAllVideoBrowser.ui.component.adapter.DownloadTabListener
import com.myAllVideoBrowser.ui.component.adapter.ProgressListener
import com.myAllVideoBrowser.ui.main.base.BaseFragment
import com.myAllVideoBrowser.ui.main.home.MainActivity
import com.myAllVideoBrowser.ui.main.home.MainViewModel
import com.myAllVideoBrowser.ui.main.home.browser.detectedVideos.DetectedVideosTabFragment
import com.myAllVideoBrowser.ui.main.home.browser.detectedVideos.VideoDetectionTabViewModel
import com.myAllVideoBrowser.ui.main.player.VideoPlayerActivity
import com.myAllVideoBrowser.ui.main.player.VideoPlayerFragment
import com.myAllVideoBrowser.util.AppLogger
import com.myAllVideoBrowser.util.CopyrightRestrictedSitePolicy
import com.myAllVideoBrowser.util.FileNameCleaner
import com.myAllVideoBrowser.util.downloaders.generic_downloader.models.VideoTaskState
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction

//@OpenForTesting
class ProgressFragment : BaseFragment() {

    companion object {
        fun newInstance() = ProgressFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mainActivity: MainActivity

    private lateinit var progressViewModel: ProgressViewModel

    private lateinit var parsedVideoSelectionViewModel: VideoDetectionTabViewModel

    private lateinit var mainViewModel: MainViewModel

    private lateinit var dataBinding: FragmentProgressBinding

    private lateinit var progressAdapter: ProgressAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mainViewModel = mainActivity.mainViewModel
        progressViewModel = ViewModelProvider(this, viewModelFactory)[ProgressViewModel::class.java]
        parsedVideoSelectionViewModel =
            ViewModelProvider(this, viewModelFactory)[VideoDetectionTabViewModel::class.java]
        parsedVideoSelectionViewModel.settingsModel = mainActivity.settingsViewModel
        progressAdapter = ProgressAdapter(emptyList(), progressListener)

        dataBinding = FragmentProgressBinding.inflate(inflater, container, false).apply {
            val managerL =
                WrapContentLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.mainViewModel = mainActivity.mainViewModel
            this.viewModel = progressViewModel
            this.rvProgress.layoutManager = managerL
            this.rvProgress.adapter = progressAdapter
            this.rvProgress.setHasFixedSize(true)
        }

        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressViewModel.start()
        bindUi()
        handleDownloadVideoEvent()
        handleUrlParsingEvents()
        renderIdlePasteState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressViewModel.stop()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun handleDownloadVideoEvent() {
        mainViewModel.downloadVideoEvent.observe(viewLifecycleOwner) { videoInfo ->
            val currentOriginal = videoInfo.originalUrl
            mainViewModel.currentOriginal.set(currentOriginal)
            progressViewModel.downloadVideo(videoInfo)
        }
    }

    private fun bindUi() {
        dataBinding.llPrase.setOnClickListener {
            parseClipboardUrl()
        }
        dataBinding.downloadActionCircle.setOnClickListener {
            parseClipboardUrl()
        }
    }

    private fun handleUrlParsingEvents() {
        progressViewModel.parsedVideoEvent.observe(viewLifecycleOwner) { videoInfo ->
            showParsedVideoSheet(videoInfo)
        }
        progressViewModel.copyrightRestrictedEvent.observe(viewLifecycleOwner) {
            Toast.makeText(
                requireContext(),
                R.string.copyright_restricted_download_message,
                Toast.LENGTH_SHORT
            ).show()
        }
        progressViewModel.parseFailedEvent.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), R.string.processing_parse_failed, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun parseClipboardUrl() {
        if (progressViewModel.isParsingUrl.get()) {
            return
        }

        val clipboardManager =
            ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
        val clipboardText =
            clipboardManager?.primaryClip?.getItemAt(0)?.coerceToText(requireContext())
        val pasteState = ProgressPasteUiStateFactory.createFromClipboard(clipboardText)

        dataBinding.tvPasteHint.text = pasteState.hintText

        if (!pasteState.shouldParse) {
            Toast.makeText(requireContext(), R.string.processing_parse_failed, Toast.LENGTH_SHORT)
                .show()
            return
        }
        progressViewModel.parseDownloadUrl(
            pasteState.hintText,
            mainActivity.settingsViewModel.isCheckOnAudio.get()
        )
    }

    private fun renderIdlePasteState() {
        dataBinding.tvPasteHint.text = ProgressPasteUiStateFactory.createIdleState().hintText
    }

    private fun showParsedVideoSheet(videoInfo: VideoInfo) {
        parsedVideoSelectionViewModel.detectedVideosList.set(setOf(videoInfo))
        parsedVideoSelectionViewModel.initialUrl = videoInfo.originalUrl
        parsedVideoSelectionViewModel.selectedFormats.set(
            mutableMapOf(
                videoInfo.id to (videoInfo.formats.formats.lastOrNull()?.format ?: "")
            )
        )
        parsedVideoSelectionViewModel.formatsTitles.set(
            mutableMapOf(videoInfo.id to videoInfo.title)
        )

        try {
            val fragmentContainer =
                requireActivity().findViewById<FragmentContainerView>(R.id.fragment_container_view)
            fragmentContainer?.let {
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                val fragment = DetectedVideosTabFragment.newInstance()
                fragment.detectedVideosTabViewModel = parsedVideoSelectionViewModel
                fragment.candidateFormatListener = parsedDownloadListener
                transaction.add(it.id, fragment, "DOWNLOADS_TAB")
                transaction.addToBackStack("DOWNLOADS_TAB")
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                transaction.commit()
            }
        } catch (e: Throwable) {
            AppLogger.e("Can't open parsed downloads sheet: ${e.message}")
        }
    }

    private val progressListener = object : ProgressListener {
        override fun onMenuClicked(view: View, downloadId: Long, isRegular: Boolean) {
            showPopupMenu(view, downloadId)
        }

        override fun onPrimaryActionClicked(progressInfo: ProgressInfo) {
            when (ProgressItemActionStateFactory.create(progressInfo.downloadStatus).primaryAction) {
                ProgressPrimaryAction.PAUSE -> progressViewModel.pauseDownload(progressInfo.downloadId)
                ProgressPrimaryAction.RESUME -> progressViewModel.resumeDownload(progressInfo.downloadId)
                ProgressPrimaryAction.NONE -> Unit
            }
        }

        override fun onCancelClicked(progressInfo: ProgressInfo) {
            progressViewModel.cancelDownload(progressInfo.downloadId, true)
        }
    }

    private fun showPopupMenu(view: View, downloadId: Long) {
        val myView = fixPopup(dataBinding.anchor, view)

        val menuCandidate =
            progressViewModel.progressInfos.get()?.find { it.downloadId == downloadId }
        val popupView = layoutInflater.inflate(R.layout.popup_progress_actions, null, false)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            elevation = 16f
        }

        val canResume = menuCandidate?.downloadStatus == VideoTaskState.PAUSE
        val canPause = menuCandidate != null && !canResume
        popupView.findViewById<View>(R.id.action_pause).visibility = if (canPause) View.VISIBLE else View.GONE
        popupView.findViewById<View>(R.id.action_resume).visibility = if (canResume) View.VISIBLE else View.GONE
        popupView.findViewById<View>(R.id.action_stop_save).visibility =
            if (menuCandidate?.isLive == true) View.VISIBLE else View.GONE

        popupView.findViewById<View>(R.id.action_cancel).setOnClickListener {
            popupWindow.dismiss()
            progressViewModel.cancelDownload(downloadId, true)
        }
        popupView.findViewById<View>(R.id.action_pause).setOnClickListener {
            popupWindow.dismiss()
            progressViewModel.pauseDownload(downloadId)
        }
        popupView.findViewById<View>(R.id.action_resume).setOnClickListener {
            popupWindow.dismiss()
            progressViewModel.resumeDownload(downloadId)
        }
        popupView.findViewById<View>(R.id.action_stop_save).setOnClickListener {
            popupWindow.dismiss()
            progressViewModel.stopAndSaveDownload(downloadId)
        }

        popupView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        popupWindow.showAsDropDown(myView, -popupView.measuredWidth + 24, 8)
    }

    private fun onParsedVideoPreview(videoInfo: VideoInfo, format: String, isForce: Boolean) {
        val currentFormat = videoInfo.formats.formats.filter {
            it.format?.contains(format) ?: false
        }

        val isImagePreview = videoInfo.ext.lowercase() in setOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
        if (isImagePreview) {
            val previewUrl = currentFormat.firstOrNull()?.url ?: videoInfo.firstUrlToString
            if (previewUrl.isNotBlank()) {
                startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(previewUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            }
            return
        }

        startActivity(
            Intent(requireContext(), VideoPlayerActivity::class.java).apply {
                putExtra(VideoPlayerFragment.VIDEO_NAME, videoInfo.title)
                if (currentFormat.isNotEmpty()) {
                    val headers = currentFormat.first().httpHeaders?.let {
                        JSONObject(currentFormat.first().httpHeaders ?: emptyMap<String, String>()).toString()
                    } ?: "{}"
                    putExtra(VideoPlayerFragment.VIDEO_URL, currentFormat.first().url)
                    putExtra(VideoPlayerFragment.VIDEO_HEADERS, if (isForce) "{}" else headers)
                }
            }
        )
    }

    private fun onParsedVideoDownload(videoInfo: VideoInfo, videoTitle: String, format: String) {
        if (CopyrightRestrictedSitePolicy.isDownloadRestrictedUrl(videoInfo.originalUrl) ||
            CopyrightRestrictedSitePolicy.isDownloadRestrictedUrl(videoInfo.firstUrlToString)
        ) {
            Toast.makeText(
                requireContext(),
                R.string.copyright_restricted_download_message,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val info = videoInfo.copy(
            id = UUID.randomUUID().toString(),
            title = FileNameCleaner.cleanFileName(videoTitle),
            formats = VideFormatEntityList(
                videoInfo.formats.formats.filter { it.format?.contains(format) ?: false }
            )
        )
        mainActivity.mainViewModel.downloadVideoEvent.value = info
        Toast.makeText(requireContext(), R.string.download_started, Toast.LENGTH_SHORT).show()
    }

    private val parsedDownloadListener = object : DownloadTabListener {
        override fun onCancel() {
            mainActivity.supportFragmentManager.popBackStack()
        }

        override fun onPreviewVideo(videoInfo: VideoInfo, format: String, isForce: Boolean) {
            onParsedVideoPreview(videoInfo, format, isForce)
        }

        override fun onDownloadVideo(videoInfo: VideoInfo, format: String, videoTitle: String) {
            onParsedVideoDownload(videoInfo, videoTitle, format)
        }

        override fun onSelectFormat(videoInfo: VideoInfo, format: String) {
            val formats =
                parsedVideoSelectionViewModel.selectedFormats.get()?.toMutableMap() ?: mutableMapOf()
            formats[videoInfo.id] = format
            parsedVideoSelectionViewModel.selectedFormats.set(formats)
        }

        override fun onFormatUrlShare(videoInfo: VideoInfo, format: String): Boolean {
            val foundFormat = videoInfo.formats.formats.find { thisFormat ->
                thisFormat.format?.contains(format) == true
            } ?: return false

            ShareCompat.IntentBuilder(mainActivity).setType("text/plain")
                .setChooserTitle("Share Link")
                .setText(foundFormat.url)
                .startChooser()
            return true
        }
    }
}

class WrapContentLinearLayoutManager : LinearLayoutManager {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(
        context, orientation, reverseLayout
    ) {
    }

    constructor(
        context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            AppLogger.e("meet a IOOBE in RecyclerView")
        }
    }
}
