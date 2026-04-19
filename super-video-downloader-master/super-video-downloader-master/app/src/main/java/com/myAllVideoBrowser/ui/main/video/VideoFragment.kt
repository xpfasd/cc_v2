package com.myAllVideoBrowser.ui.main.video

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.ClipData
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.databinding.Observable
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import android.content.pm.PackageManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.cc.ads.topon.TopOnAdSceneManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.data.local.model.LocalVideo
import com.myAllVideoBrowser.databinding.FragmentVideoBinding
import com.myAllVideoBrowser.ui.component.adapter.VideoAdapter
import com.myAllVideoBrowser.ui.component.adapter.VideoListener
import com.myAllVideoBrowser.ui.component.dialog.showDownloadedSearchDialog
import com.myAllVideoBrowser.ui.component.dialog.showRenameVideoDialog
import com.myAllVideoBrowser.ui.main.base.BaseFragment
import com.myAllVideoBrowser.ui.main.base.PopupPositioning
import com.myAllVideoBrowser.ui.main.home.MainActivity
import com.myAllVideoBrowser.ui.main.player.VideoPlayerActivity
import com.myAllVideoBrowser.ui.main.player.VideoPlayerFragment
import com.myAllVideoBrowser.ui.main.progress.WrapContentLinearLayoutManager
import com.myAllVideoBrowser.ui.main.settings.password.PasswordFlowNavigator
import com.myAllVideoBrowser.ui.main.settings.password.SecurityQuestionFragment
import com.myAllVideoBrowser.ui.main.video.VideoViewModel.MediaFilter
import com.myAllVideoBrowser.ui.main.video.VideoViewModel.VideoSortMode
import com.myAllVideoBrowser.ui.main.video.VideoViewModel.Companion.FILE_EXIST_ERROR_CODE
import com.myAllVideoBrowser.util.AppLogger
import com.myAllVideoBrowser.util.AppUtil
import com.myAllVideoBrowser.util.FileUtil
import com.myAllVideoBrowser.util.IntentUtil
import com.myAllVideoBrowser.util.MediaFormatSupport
import com.myAllVideoBrowser.util.resolveMimeTypeFromNames
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.util.ArrayList
import javax.inject.Inject

//@OpenForTesting
class VideoFragment : BaseFragment() {

    companion object {
        private const val ARG_IS_PRIVATE_SPACE = "arg_is_private_space"

        fun newInstance() = VideoFragment()

        fun newPrivateSpaceInstance() = VideoFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_IS_PRIVATE_SPACE, true)
            }
        }
    }

    private var disposable: Disposable? = null
    private val selectedVideoIds = linkedSetOf<Long>()
    private var selectionMode = false
    private var currentSortMode = VideoSortMode.LAST_EDIT_DESC
    private var isPrivateSpaceMode = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var intentUtil: IntentUtil

    @Inject
    lateinit var fileUtil: FileUtil

    @Inject
    lateinit var appUtil: AppUtil

    private lateinit var dataBinding: FragmentVideoBinding
    private lateinit var videoViewModel: VideoViewModel
    private lateinit var videoAdapter: VideoAdapter
    private val syncListUiRunnable = Runnable {
        if (isAdded && view != null && this::dataBinding.isInitialized) {
            syncListUi()
        }
    }
    private var hasInitializedContent = false
    private var cancelStoragePermissionPromptReadyCallback: Runnable? = null

    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted = result.values.all { it }
            AppLogger.d("VideoFragment.storagePermissionLauncher granted=$granted result=$result")
            if (granted) {
                initializeVideoContent()
            } else {
                syncListUi()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isPrivateSpaceMode = arguments?.getBoolean(ARG_IS_PRIVATE_SPACE) == true
    }

    private val videosObserver = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            if (!this@VideoFragment::dataBinding.isInitialized) {
                return
            }
            dataBinding.root.removeCallbacks(syncListUiRunnable)
            dataBinding.root.post(syncListUiRunnable)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        videoViewModel = ViewModelProvider(this, viewModelFactory)[VideoViewModel::class.java]
        videoAdapter = VideoAdapter(emptyList(), videoListener, fileUtil)

        dataBinding = FragmentVideoBinding.inflate(inflater, container, false).apply {
            val managerL =
                WrapContentLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            viewModel = videoViewModel
            rvVideo.layoutManager = managerL
            rvVideo.adapter = videoAdapter
        }

        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoViewModel.localVideos.addOnPropertyChangedCallback(videosObserver)
        dataBinding.lifecycleOwner = viewLifecycleOwner
        videoViewModel.shareEvent.observe(viewLifecycleOwner) { uri ->
            val matchedVideo = videoViewModel.localVideos.get().orEmpty()
                .firstOrNull { it.uri == uri }
            intentUtil.shareVideo(requireContext(), uri, matchedVideo?.name)
        }
        currentSortMode = videoViewModel.sortMode.get() ?: VideoSortMode.LAST_EDIT_DESC
        bindUi()
        handleUIEvents()
        handleIfStartedFromNotification()
        ensureVideoContentReady()
        syncListUi()
    }

    override fun onDestroyView() {
        videoViewModel.localVideos.removeOnPropertyChangedCallback(videosObserver)
        if (this::dataBinding.isInitialized) {
            dataBinding.root.removeCallbacks(syncListUiRunnable)
        }
        cancelStoragePermissionPromptReadyCallback?.run()
        cancelStoragePermissionPromptReadyCallback = null
        disposable?.dispose()
        disposable = null
        super.onDestroyView()
    }

    private fun bindUi() {
        dataBinding.ivHeaderLeft.setOnClickListener {
            if (selectionMode) {
                exitSelectionMode()
            } else if (isPrivateSpaceMode) {
                requireActivity().finish()
            } else {
                openPrivateSpaceEntry()
            }
        }
        dataBinding.ivHeaderRight.setOnClickListener {
            if (!selectionMode) {
                if (isPrivateSpaceMode) {
                    showPrivateSpaceOptionsPopup(it)
                } else {
                    showSearchDialog()
                }
            } else {
                selectAllOrClear()
            }
        }
        dataBinding.ivSelectAll.setOnClickListener { selectAllOrClear() }
        dataBinding.ivSort.setOnClickListener { showSortDialog() }
        dataBinding.ivMultiSelect.setOnClickListener { handleMultiSelectClick() }
        dataBinding.btnShareSelected.setOnClickListener { shareSelectedVideos() }
        dataBinding.btnDeleteSelected.setOnClickListener { deleteSelectedVideos() }
        dataBinding.tabAll.setOnClickListener { selectFilter(MediaFilter.ALL) }
        dataBinding.tabVideos.setOnClickListener { selectFilter(MediaFilter.VIDEO) }
        dataBinding.tabImages.setOnClickListener { selectFilter(MediaFilter.IMAGE) }
    }

    private fun ensureVideoContentReady() {
        if (isPrivateSpaceMode || hasDownloadLocationPermissions()) {
            initializeVideoContent()
            return
        }

        val mainActivity = activity as? MainActivity
        if (mainActivity == null || mainActivity.isStoragePermissionPromptReady()) {
            requestStoragePermissions()
            return
        }

        if (cancelStoragePermissionPromptReadyCallback != null) {
            return
        }

        AppLogger.d("VideoFragment.ensureVideoContentReady waiting for launch flow delay before requesting permissions")
        cancelStoragePermissionPromptReadyCallback =
            mainActivity.runWhenStoragePermissionPromptReady {
                cancelStoragePermissionPromptReadyCallback = null
                if (!isAdded || view == null) {
                    return@runWhenStoragePermissionPromptReady
                }
                if (hasDownloadLocationPermissions()) {
                    initializeVideoContent()
                } else {
                    requestStoragePermissions()
                }
            }
    }

    private fun requestStoragePermissions() {
        if (!isAdded) {
            return
        }
        AppLogger.d(
            "VideoFragment.ensureVideoContentReady requesting permissions ${requiredStoragePermissions().toList()}"
        )
        storagePermissionLauncher.launch(requiredStoragePermissions())
    }

    private fun initializeVideoContent() {
        cancelStoragePermissionPromptReadyCallback?.run()
        cancelStoragePermissionPromptReadyCallback = null
        if (hasInitializedContent) {
            videoViewModel.refreshVideos()
        } else {
            hasInitializedContent = true
            videoViewModel.setContentSource(
                if (isPrivateSpaceMode) VideoViewModel.ContentSource.PRIVATE_SPACE
                else VideoViewModel.ContentSource.DOWNLOADS
            )
            videoViewModel.start()
        }
    }

    private fun hasDownloadLocationPermissions(): Boolean {
        return requiredStoragePermissions().all { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) ==
                PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requiredStoragePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun handleUIEvents() {
        videoViewModel.renameErrorEvent.observe(viewLifecycleOwner) { errorCode ->
            val errorMessage =
                if (errorCode == FILE_EXIST_ERROR_CODE) R.string.video_rename_exist else R.string.video_rename_invalid
            activity?.runOnUiThread {
                Toast.makeText(context, context?.getString(errorMessage), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun handleIfStartedFromNotification() {
        val hostMainActivity = activity as? MainActivity ?: return
        hostMainActivity.mainViewModel.openDownloadedVideoEvent.observe(viewLifecycleOwner) { downloadFilename ->
            disposable?.dispose()
            disposable = null
            disposable = videoViewModel.findVideoByName(downloadFilename)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .subscribe { video ->
                    activity?.runOnUiThread { startVideo(video) }
                }
        }
    }

    private val videoListener = object : VideoListener {
        override fun onItemClicked(localVideo: LocalVideo) {
            if (selectionMode) {
                toggleSelection(localVideo)
            } else {
                startVideo(localVideo)
            }
        }

        override fun onItemLongClicked(localVideo: LocalVideo) {
            if (selectionMode) {
                toggleSelection(localVideo)
            }
        }

        override fun onMenuClicked(view: View, localVideo: LocalVideo) {
            showMoreActionsPopup(view, localVideo)
        }
    }

    private fun syncListUi() {
        if (!isAdded || view == null || !this::dataBinding.isInitialized) {
            return
        }
        val videos = videoViewModel.localVideos.get().orEmpty()
        val validIds = videos.map { it.id }.toSet()
        selectedVideoIds.retainAll(validIds)
        if (selectionMode && videos.isEmpty()) {
            selectionMode = false
        }

        videoAdapter.setData(videos)
        dataBinding.layoutEmpty.isVisible = videos.isEmpty()
        AppLogger.d(
            "VideoFragment.syncListUi private=$isPrivateSpaceMode selection=$selectionMode " +
                "filter=${videoViewModel.mediaFilter.get()} search=${videoViewModel.searchQuery.get().orEmpty()} " +
                "videos=${videos.size} " +
                "adapter=${videoAdapter.itemCount} emptyVisible=${dataBinding.layoutEmpty.isVisible} " +
                "sample=${videos.take(10).map { it.name }}"
        )

        dataBinding.tvFileCount.text = if (isPrivateSpaceMode) {
            getString(R.string.private_space_file_count, videos.size)
        } else {
            resources.getQuantityString(
                R.plurals.video_file_count,
                videos.size,
                videos.size
            )
        }
        dataBinding.tvHeaderTitle.text =
            if (selectionMode) getString(R.string.video_selected_count, selectedVideoIds.size)
            else if (isPrivateSpaceMode) getString(R.string.title_private_space)
            else getString(R.string.title_video)
        dataBinding.tvHeaderSubtitle.isVisible = isPrivateSpaceMode && !selectionMode
        dataBinding.ivHeaderLeft.setImageResource(
            if (selectionMode) R.drawable.close_24px
            else if (isPrivateSpaceMode) R.drawable.homeback
            else R.drawable.shield_24px
        )
        dataBinding.ivHeaderRight.isVisible = !selectionMode
        dataBinding.ivHeaderRight.setImageResource(
            if (isPrivateSpaceMode) R.drawable.more_vert_24px
            else R.drawable.search_24px
        )
        dataBinding.ivSelectAll.isVisible = selectionMode
        dataBinding.ivSelectAll.setImageResource(
            if (isAllSelected()) R.drawable.downloaded_ic_selection_checked
            else R.drawable.downloaded_ic_selection_unchecked
        )
//        dataBinding.ivMultiSelect.setImageResource(
//            if (selectionMode) R.drawable.downloaded_ic_selection_checked
//            else R.drawable.downloaded_ic_selection_unchecked
//        )
        dataBinding.layoutMultiSelectActions.isVisible = selectionMode
        dataBinding.btnShareSelected.isVisible = selectionMode && !isPrivateSpaceMode
        dataBinding.btnDeleteSelected.text = getString(
            if (isPrivateSpaceMode) R.string.private_space_delete_action
            else R.string.video_action_delete
        )
        dataBinding.btnDeleteSelected.setTextColor(
            getColorCompat(if (isPrivateSpaceMode) android.R.color.black else android.R.color.white)
        )
        dataBinding.btnDeleteSelected.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                getColorCompat(
                    if (isPrivateSpaceMode) R.color.home_accent else android.R.color.holo_red_light
                )
            )
//        dataBinding.ivEmptyIcon.setImageResource(
//            if (isPrivateSpaceMode) R.drawable.password_lock_24px else R.drawable.ic_empty
//        )
//        dataBinding.tvEmptyText.text = getString(
//            if (isPrivateSpaceMode) R.string.private_space_empty_message
//            else R.string.video_empty_message
//        )
        videoAdapter.setSelectionState(selectionMode, selectedVideoIds)
        renderFilterTabs()
    }

    private fun toggleSelection(localVideo: LocalVideo) {
        applySelectionState(
            VideoSelectionModeController.toggleItemSelection(
                selectedIds = selectedVideoIds,
                toggledId = localVideo.id
            )
        )
    }

    private fun handleMultiSelectClick() {
        applySelectionState(
            VideoSelectionModeController.onMultiSelectClick(
                selectionMode = selectionMode,
                selectedIds = selectedVideoIds,
                availableIds = videoViewModel.localVideos.get().orEmpty().map { it.id }
            )
        )
    }

    private fun enterPrivateBatchDeleteMode() {
        selectionMode = true
        selectedVideoIds.clear()
        syncListUi()
    }

    private fun exitSelectionMode() {
        selectionMode = false
        selectedVideoIds.clear()
        syncListUi()
    }

    private fun selectAllOrClear() {
        applySelectionState(
            VideoSelectionModeController.toggleAllSelection(
                selectedIds = selectedVideoIds,
                availableIds = videoViewModel.localVideos.get().orEmpty().map { it.id }
            )
        )
    }

    private fun applySelectionState(state: VideoSelectionState) {
        selectionMode = state.selectionMode
        selectedVideoIds.clear()
        selectedVideoIds.addAll(state.selectedIds)
        syncListUi()
    }

    private fun isAllSelected(): Boolean {
        val videos = videoViewModel.localVideos.get().orEmpty()
        return videos.isNotEmpty() && selectedVideoIds.size == videos.size
    }

    private fun showMoreActionsPopup(view: View, video: LocalVideo) {
        val myView = fixPopup(dataBinding.anchor, view)
        val popupView = layoutInflater.inflate(R.layout.popup_downloaded_more_actions, null, false)
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

        popupView.findViewById<View>(R.id.action_rename).setOnClickListener {
            popupWindow.dismiss()
            showRenameVideoDialog(view.context, appUtil, video.name) { v ->
                with(v as EditText) {
                    val newName = text.toString().trim()
                    val extension = File(video.name).extension
                    val targetName = buildString {
                        append(File(newName).nameWithoutExtension)
                        if (extension.isNotBlank()) {
                            append(".")
                            append(extension)
                        }
                    }
                    videoViewModel.renameVideo(
                        v.context, video.uri, targetName
                    )
                }
            }
        }
        popupView.findViewById<View>(R.id.action_share).setOnClickListener {
            popupWindow.dismiss()
            videoViewModel.shareEvent.value = video.uri
        }
        val privateActionText = popupView.findViewById<android.widget.TextView>(R.id.action_private_text)
        privateActionText.text = getString(
            if (isPrivateSpaceMode) R.string.video_menu_remove_private_title
            else R.string.video_menu_move_private_title
        )
        popupView.findViewById<View>(R.id.action_private).setOnClickListener {
            popupWindow.dismiss()
            if (isPrivateSpaceMode) {
                removeFromPrivateSpace(video)
            } else {
                moveToPrivateFolder(video)
            }
        }
        popupView.findViewById<View>(R.id.action_delete).setOnClickListener {
            popupWindow.dismiss()
            TopOnAdSceneManager.showGeneralInterstitial(requireActivity()) {
                context?.let { videoViewModel.deleteVideo(it, video) }
            }
        }

        popupView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        popupWindow.showAsDropDown(
            myView,
            PopupPositioning.calculateEndAlignedXOffset(view.width, popupView.measuredWidth),
            popupVerticalOffsetPx()
        )
    }

    private fun showPrivateSpaceOptionsPopup(anchorView: View) {
        val myView = fixPopup(dataBinding.anchor, anchorView)
        val popupView = layoutInflater.inflate(R.layout.popup_private_space_actions, null, false)
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

        popupView.findViewById<View>(R.id.action_change_password).setOnClickListener {
            popupWindow.dismiss()
            PasswordFlowNavigator.startChangePin(requireActivity().supportFragmentManager)
        }
        popupView.findViewById<View>(R.id.action_change_security).setOnClickListener {
            popupWindow.dismiss()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container_view,
                    SecurityQuestionFragment.newEditOnlyInstance()
                )
                .addToBackStack("private_space_security_question")
                .commit()
        }
        popupView.findViewById<View>(R.id.action_delete_batch).setOnClickListener {
            popupWindow.dismiss()
            enterPrivateBatchDeleteMode()
        }

        popupView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        popupWindow.showAsDropDown(
            myView,
            PopupPositioning.calculateEndAlignedXOffset(anchorView.width, popupView.measuredWidth),
            popupVerticalOffsetPx()
        )
    }

    private fun popupVerticalOffsetPx(): Int {
        return (8 * resources.displayMetrics.density).toInt()
    }

    private fun renderFilterTabs() {
        val selected = videoViewModel.mediaFilter.get() ?: MediaFilter.ALL
        renderFilterTab(
            dataBinding.tabAll,
            selected == MediaFilter.ALL
        )
        renderFilterTab(
            dataBinding.tabVideos,
            selected == MediaFilter.VIDEO
        )
        renderFilterTab(
            dataBinding.tabImages,
            selected == MediaFilter.IMAGE
        )
    }

    private fun renderFilterTab(cardView: com.google.android.material.card.MaterialCardView, selected: Boolean) {
        val textView = cardView.getChildAt(0) as? android.widget.TextView ?: return
        textView.setTextColor(
            if (selected) getColorCompat(R.color.downloaded_tab_selected)
            else getColorCompat(R.color.home_hint)
        )
        textView.textSize = if (selected) 17f else 15f
        textView.setTypeface(textView.typeface, if (selected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
    }

    private fun selectFilter(filter: MediaFilter) {
        if (videoViewModel.mediaFilter.get() == filter) {
            return
        }
        exitSelectionMode()
        videoViewModel.setMediaFilter(filter)
        syncListUi()
    }

    private fun getColorCompat(colorRes: Int): Int {
        return androidx.core.content.ContextCompat.getColor(requireContext(), colorRes)
    }

    private fun showSortDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_downloaded_sort, null, false)
        dialog.setContentView(dialogView)

        var selectedSortMode = currentSortMode
        val checkViews = mapOf(
            VideoSortMode.NAME_ASC to dialogView.findViewById<ImageView>(R.id.sort_name_asc_check),
            VideoSortMode.NAME_DESC to dialogView.findViewById<ImageView>(R.id.sort_name_desc_check),
            VideoSortMode.LAST_EDIT_DESC to dialogView.findViewById<ImageView>(R.id.sort_last_edit_desc_check),
            VideoSortMode.LAST_EDIT_ASC to dialogView.findViewById<ImageView>(R.id.sort_last_edit_asc_check),
            VideoSortMode.SIZE_DESC to dialogView.findViewById<ImageView>(R.id.sort_size_desc_check),
            VideoSortMode.SIZE_ASC to dialogView.findViewById<ImageView>(R.id.sort_size_asc_check),
        )

        fun renderSortChoice() {
            checkViews.forEach { (mode, imageView) ->
                imageView.setImageResource(
                    if (mode == selectedSortMode) R.drawable.downloaded_ic_selection_checked
                    else R.drawable.downloaded_ic_selection_unchecked
                )
            }
        }

        dialogView.findViewById<View>(R.id.sort_name_asc).setOnClickListener {
            selectedSortMode = VideoSortMode.NAME_ASC
            renderSortChoice()
        }
        dialogView.findViewById<View>(R.id.sort_name_desc).setOnClickListener {
            selectedSortMode = VideoSortMode.NAME_DESC
            renderSortChoice()
        }
        dialogView.findViewById<View>(R.id.sort_last_edit_desc).setOnClickListener {
            selectedSortMode = VideoSortMode.LAST_EDIT_DESC
            renderSortChoice()
        }
        dialogView.findViewById<View>(R.id.sort_last_edit_asc).setOnClickListener {
            selectedSortMode = VideoSortMode.LAST_EDIT_ASC
            renderSortChoice()
        }
        dialogView.findViewById<View>(R.id.sort_size_desc).setOnClickListener {
            selectedSortMode = VideoSortMode.SIZE_DESC
            renderSortChoice()
        }
        dialogView.findViewById<View>(R.id.sort_size_asc).setOnClickListener {
            selectedSortMode = VideoSortMode.SIZE_ASC
            renderSortChoice()
        }

        dialogView.findViewById<View>(R.id.btn_sort_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<View>(R.id.btn_sort_confirm).setOnClickListener {
            currentSortMode = selectedSortMode
            videoViewModel.setSortMode(selectedSortMode)
            dialog.dismiss()
        }

        renderSortChoice()
        dialog.show()
    }

    private fun showSearchDialog() {
        showDownloadedSearchDialog(
            context = requireContext(),
            appUtil = appUtil,
            initialQuery = videoViewModel.searchQuery.get().orEmpty()
        ) { query ->
            videoViewModel.setSearchQuery(query)
        }
    }

    private fun shareSelectedVideos() {
        val videos = videoViewModel.localVideos.get().orEmpty()
            .filter { selectedVideoIds.contains(it.id) }
        if (videos.isEmpty()) return

        if (videos.size == 1) {
            videoViewModel.shareEvent.value = videos.first().uri
            return
        }

        val shareUris = videos.map { videoUriForShare(it) }
        val shareMimeType = resolveMimeTypeFromNames(videos.map { it.name })
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = shareMimeType
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(shareUris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(requireContext().contentResolver, "", shareUris.first()).also { clip ->
                shareUris.drop(1).forEach { clip.addItem(ClipData.Item(it)) }
            }
        }
        startActivity(Intent.createChooser(intent, getString(R.string.video_action_share)))
    }

    private fun deleteSelectedVideos() {
        val videos = videoViewModel.localVideos.get().orEmpty()
            .filter { selectedVideoIds.contains(it.id) }
        if (videos.isEmpty()) return

        TopOnAdSceneManager.showGeneralInterstitial(requireActivity()) {
            videos.forEach { video ->
                context?.let { fileUtil.deleteMedia(it, video.uri) }
            }
            videoViewModel.refreshVideos()
            exitSelectionMode()
        }
    }

    private fun moveToPrivateFolder(video: LocalVideo) {
        moveVideoTo(video, fileUtil.privateSpaceDir, R.string.media_move_success)
    }

    private fun removeFromPrivateSpace(video: LocalVideo) {
        moveVideoTo(
            video,
            fileUtil.folderDir,
            R.string.private_space_remove_success
        )
    }

    private fun moveVideoTo(video: LocalVideo, targetDir: File, successMessageRes: Int) {
        try {
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            val target = resolveMoveTarget(targetDir, video.name)
            val isSuccess = fileUtil.moveMedia(requireContext(), video.uri, target.toUri())
            if (isSuccess) {
                Toast.makeText(requireContext(), getString(successMessageRes), Toast.LENGTH_SHORT)
                    .show()
                videoViewModel.refreshVideos()
                return
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        Toast.makeText(
            requireContext(),
            getString(R.string.media_move_error),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun openPrivateSpaceEntry() {
        startActivity(PrivateSpaceActivity.createIntent(requireContext()))
    }

    @OptIn(UnstableApi::class)
    private fun startVideo(localVideo: LocalVideo) {
        if (!shouldOpenWithInternalPlayer(localVideo.name)) {
            startVideoWith(localVideo)
            return
        }
        val playIntent = Intent(
                requireContext(), VideoPlayerActivity::class.java
            ).apply {
                putExtra(VideoPlayerFragment.VIDEO_NAME, localVideo.name)
                putExtra(VideoPlayerFragment.VIDEO_URL, localVideo.uri.toString())
            }
        TopOnAdSceneManager.showGeneralInterstitial(requireActivity()) {
            startActivity(playIntent)
        }
    }

    private fun startVideoWith(localVideo: LocalVideo) {
        val mimeType = intentUtil.resolveMimeType(requireContext(), localVideo.uri, localVideo.name)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val fileSupported = fileUtil.isFileApiSupportedByUri(requireContext(), localVideo.uri)
            if (fileSupported) {
                val videoUri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().applicationContext.packageName + ".provider",
                    localVideo.uri.toFile()
                )
                setDataAndType(videoUri, mimeType)
            } else {
                setDataAndType(localVideo.uri, mimeType)
            }
        }

        context?.startActivity(intent)
    }

    private fun videoUriForShare(localVideo: LocalVideo): Uri {
        val fileSupported = fileUtil.isFileApiSupportedByUri(requireContext(), localVideo.uri)
        return if (fileSupported) {
            FileProvider.getUriForFile(
                requireContext(),
                requireContext().applicationContext.packageName + ".provider",
                localVideo.uri.toFile()
            )
        } else {
            localVideo.uri
        }
    }
}

internal fun shouldOpenWithInternalPlayer(fileName: String): Boolean {
    return MediaFormatSupport.isVideoExtension(fileName.substringAfterLast('.', ""))
}

internal fun resolveMoveTarget(targetDir: File, originalName: String): File {
    val desired = File(targetDir, originalName)
    if (!desired.exists()) {
        return desired
    }

    val baseName = File(originalName).nameWithoutExtension
    val extension = File(originalName).extension
    var counter = 1
    while (true) {
        val candidateName = buildString {
            append(baseName)
            append("_cp")
            append(counter)
            if (extension.isNotBlank()) {
                append(".")
                append(extension)
            }
        }
        val candidate = File(targetDir, candidateName)
        if (!candidate.exists()) {
            return candidate
        }
        counter++
    }
}
