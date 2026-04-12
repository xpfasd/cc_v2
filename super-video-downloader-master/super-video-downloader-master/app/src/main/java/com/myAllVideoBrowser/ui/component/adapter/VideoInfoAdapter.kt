package com.myAllVideoBrowser.ui.component.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.card.MaterialCardView
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.data.local.room.entity.VideoFormatEntity
import com.myAllVideoBrowser.data.local.room.entity.VideoInfo
import com.myAllVideoBrowser.databinding.ItemVideoInfoBinding
import com.myAllVideoBrowser.ui.main.home.browser.detectedVideos.DetectedDownloadActionState
import com.myAllVideoBrowser.ui.main.home.browser.detectedVideos.DetectedDownloadRowState
import com.myAllVideoBrowser.ui.main.home.browser.detectedVideos.DirectMediaSupport
import com.myAllVideoBrowser.ui.main.home.browser.detectedVideos.VideoDetectionTabViewModel
import com.myAllVideoBrowser.util.FileNameCleaner
import com.myAllVideoBrowser.util.MediaFormatSupport

interface DetectedDownloadItemListener {
    fun onRowClicked(videoInfo: VideoInfo)
    fun onActionClicked(videoInfo: VideoInfo, actionState: DetectedDownloadActionState)
}

class VideoInfoAdapter(
    initialVideoInfoList: List<VideoInfo>,
    private val model: VideoDetectionTabViewModel,
    private val itemListener: DetectedDownloadItemListener,
    private val rowStateProvider: (VideoInfo) -> DetectedDownloadRowState,
    private val metadataProvider: (VideoInfo) -> String
) : RecyclerView.Adapter<VideoInfoAdapter.VideoInfoViewHolder>() {

    private var videoInfoList: List<VideoInfo> =
        sortDetectedVideosForDisplay(initialVideoInfoList) { info ->
            resolveDisplayedFormatSize(
                info = info,
                selectedFormatName = model.selectedFormats.get()?.get(info.id)
            )
        }

    companion object {
        private fun VideoInfo.isImageCandidate(): Boolean = DirectMediaSupport.isImageExtension(ext)

        private fun VideoInfo.isAudioCandidate(): Boolean {
            return MediaFormatSupport.isAudioExtension(ext)
        }
    }

    inner class VideoInfoViewHolder(
        private val binding: ItemVideoInfoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(info: VideoInfo) {
            ensureDefaults(info)
            val selectedFormat = currentFormat(info)
            val isImage = info.isImageCandidate()
            val isAudio = info.isAudioCandidate()
            val rowState = rowStateProvider(info)
            val displayFileName = buildDisplayFileName(info, selectedFormat)
            val metadataText = metadataProvider(info)

            binding.videoInfo = info
            binding.titleTextView.text = displayFileName
            binding.metadataTextView.text = metadataText
            binding.playOverlay.isVisible = !isImage && !isAudio
            binding.playOverlayIcon.isVisible = binding.playOverlay.isVisible
            binding.divider.isVisible = bindingAdapterPosition != itemCount - 1

            bindThumbnail(info, isImage)
            bindAction(binding.actionButton, rowState)

            binding.root.setOnClickListener {
                itemListener.onRowClicked(info)
            }
            binding.actionButton.setOnClickListener {
                itemListener.onActionClicked(info, rowState.actionState)
            }
            binding.executePendingBindings()
        }

        private fun bindThumbnail(info: VideoInfo, isImage: Boolean) {
            val previewUrl = when {
                info.thumbnail.isNotBlank() -> info.thumbnail
                isImage -> info.firstUrlToString
                else -> ""
            }
            val placeholder = if (isImage) R.drawable.image_24px else R.drawable.ic_video_24dp

            if (previewUrl.isBlank()) {
                binding.mediaPreview.setImageResource(placeholder)
                return
            }

            Glide.with(binding.root)
                .load(previewUrl)
                .placeholder(placeholder)
                .error(placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.mediaPreview)
        }

        private fun bindAction(
            actionButton: MaterialCardView,
            rowState: DetectedDownloadRowState
        ) {
            val context = binding.root.context
            val strokeColor: Int
            val backgroundColor: Int
            val iconTint: Int
            val iconRes: Int

            when (rowState.actionState) {
                DetectedDownloadActionState.DOWNLOAD -> {
                    strokeColor = ContextCompat.getColor(context, R.color.black)
                    backgroundColor = ContextCompat.getColor(context, R.color.white)
                    iconTint = ContextCompat.getColor(context, R.color.black)
                    iconRes = R.drawable.ic_detected_download_16
                }

                DetectedDownloadActionState.PAUSE -> {
                    strokeColor = ContextCompat.getColor(context, R.color.home_divider)
                    backgroundColor = ContextCompat.getColor(context, R.color.white)
                    iconTint = ContextCompat.getColor(context, R.color.detected_status_orange)
                    iconRes = R.drawable.ic_detected_pause_16
                }

                DetectedDownloadActionState.START -> {
                    strokeColor = ContextCompat.getColor(context, R.color.home_divider)
                    backgroundColor = ContextCompat.getColor(context, R.color.white)
                    iconTint = ContextCompat.getColor(context, R.color.detected_status_orange)
                    iconRes = R.drawable.ic_detected_play_16
                }

                DetectedDownloadActionState.DONE -> {
                    strokeColor = ContextCompat.getColor(context, R.color.detected_status_green)
                    backgroundColor = ContextCompat.getColor(context, R.color.detected_status_green)
                    iconTint = ContextCompat.getColor(context, R.color.white)
                    iconRes = R.drawable.ic_detected_done_16
                }

                DetectedDownloadActionState.FAIL -> {
                    strokeColor = ContextCompat.getColor(context, R.color.detected_status_red)
                    backgroundColor = ContextCompat.getColor(context, R.color.detected_status_red)
                    iconTint = ContextCompat.getColor(context, R.color.white)
                    iconRes = R.drawable.ic_detected_error_16
                }
            }

            actionButton.setCardBackgroundColor(backgroundColor)
            actionButton.strokeColor = strokeColor
            actionButton.strokeWidth = if (rowState.actionState == DetectedDownloadActionState.DOWNLOAD) {
                binding.root.resources.displayMetrics.density.toInt().coerceAtLeast(1)
            } else {
                0
            }
            binding.actionIcon.setImageResource(iconRes)
            binding.actionIcon.imageTintList = android.content.res.ColorStateList.valueOf(iconTint)
        }

        private fun currentFormat(info: VideoInfo): VideoFormatEntity? {
            val selected = model.selectedFormats.get()?.get(info.id)
            return info.formats.formats.find { it.format == selected } ?: info.formats.formats.lastOrNull()
        }

        private fun ensureDefaults(info: VideoInfo) {
            val currentTitles = model.formatsTitles.get()?.toMutableMap() ?: mutableMapOf()
            if (currentTitles[info.id].isNullOrBlank()) {
                currentTitles[info.id] = info.title
                model.formatsTitles.set(currentTitles)
            }

            val currentFormats = model.selectedFormats.get()?.toMutableMap() ?: mutableMapOf()
            if (currentFormats[info.id].isNullOrBlank()) {
                currentFormats[info.id] = info.formats.formats.lastOrNull()?.format ?: ""
                model.selectedFormats.set(currentFormats)
            }
        }

        private fun buildDisplayFileName(info: VideoInfo, format: VideoFormatEntity?): String {
            val typedTitle = model.formatsTitles.get()?.get(info.id)?.ifBlank { info.title } ?: info.title
            val cleanTitle = FileNameCleaner.cleanFileName(typedTitle.ifBlank { info.title })
            val ext = format?.ext?.takeIf { it.isNotBlank() } ?: info.ext
            return if (cleanTitle.endsWith(".$ext", ignoreCase = true)) cleanTitle else "$cleanTitle.$ext"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoInfoViewHolder {
        val binding = DataBindingUtil.inflate<ItemVideoInfoBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_video_info,
            parent,
            false
        )
        return VideoInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoInfoViewHolder, position: Int) {
        holder.bind(videoInfoList[position])
    }

    override fun getItemCount(): Int = videoInfoList.size

    fun setData(localVideos: List<VideoInfo>) {
        videoInfoList = sortDetectedVideosForDisplay(localVideos) { info ->
            resolveDisplayedFormatSize(
                info = info,
                selectedFormatName = model.selectedFormats.get()?.get(info.id)
            )
        }
        notifyDataSetChanged()
    }
}

internal fun sortDetectedVideosForDisplay(
    videos: List<VideoInfo>,
    sizeResolver: (VideoInfo) -> Long
): List<VideoInfo> {
    return videos.sortedWith(
        compareByDescending<VideoInfo> { sizeResolver(it) }
            .thenBy { it.title.lowercase() }
            .thenBy { it.id }
    )
}

internal fun resolveDisplayedFormatSize(
    info: VideoInfo,
    selectedFormatName: String?
): Long {
    val selectedFormat = info.formats.formats.find { it.format == selectedFormatName }
    val selectedSize = selectedFormat.readableSortSize()
    if (selectedSize > 0L) {
        return selectedSize
    }

    val fallbackFormat = info.formats.formats.lastOrNull()
    val fallbackSize = fallbackFormat.readableSortSize()
    if (fallbackSize > 0L) {
        return fallbackSize
    }

    return info.formats.formats.maxOfOrNull { it.readableSortSize() } ?: 0L
}

private fun VideoFormatEntity?.readableSortSize(): Long {
    return when {
        this == null -> 0L
        fileSizeApproximate > 0L -> fileSizeApproximate
        fileSize > 0L -> fileSize
        else -> 0L
    }
}
