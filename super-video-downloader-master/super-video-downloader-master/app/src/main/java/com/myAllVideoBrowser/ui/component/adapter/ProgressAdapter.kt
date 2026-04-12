package com.myAllVideoBrowser.ui.component.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.data.local.room.entity.ProgressInfo
import com.myAllVideoBrowser.databinding.ItemProgressBinding
import com.myAllVideoBrowser.ui.main.progress.ProgressItemActionStateFactory
import com.myAllVideoBrowser.ui.main.progress.ProgressPrimaryAction
import com.myAllVideoBrowser.util.FileUtil

class ProgressAdapter(
    private var progressInfos: List<ProgressInfo>,
    private var videoListener: ProgressListener
) : RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val binding = DataBindingUtil.inflate<ItemProgressBinding>(
            LayoutInflater.from(parent.context), R.layout.item_progress, parent, false
        )

        return ProgressViewHolder(binding)
    }

    override fun getItemCount() = progressInfos.size

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) =
        holder.bind(progressInfos[position], videoListener)

    class ProgressViewHolder(val binding: ItemProgressBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProgressInfo, progressListener: ProgressListener) {
            val actionState = ProgressItemActionStateFactory.create(item.downloadStatus)
            val isImage = item.videoInfo.ext.lowercase() in IMAGE_EXTENSIONS
            val thumbnailRes = if (isImage) R.drawable.image_24px else R.drawable.ic_video_24dp
            val thumbnailTint = if (isImage) {
                R.color.processing_thumbnail_image_tint
            } else {
                R.color.processing_thumbnail_video_tint
            }
            val primaryIconRes = when (actionState.primaryAction) {
                ProgressPrimaryAction.RESUME -> R.drawable.process_start
                ProgressPrimaryAction.PAUSE -> R.drawable.process_stop
                ProgressPrimaryAction.NONE -> 0
            }
            val progressText = buildString {
                append(FileUtil.getFileSizeReadable(item.progressDownloaded.toDouble()))
                append(" / ")
                append(FileUtil.getFileSizeReadable(item.progressTotal.toDouble()))
            }

            with(binding) {
                this.progressInfo = item
                ivThumbnail.setImageResource(thumbnailRes)
                ivThumbnail.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(itemView.context, thumbnailTint)
                )
                tvProgress.text = progressText

                primaryActionButton.visibility =
                    if (actionState.showPrimaryAction) View.VISIBLE else View.GONE
                if (actionState.showPrimaryAction) {
                    ivPrimaryAction.setImageResource(primaryIconRes)
                    ivPrimaryAction.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.processing_primary_action_tint
                        )
                    )
                    primaryActionButton.setOnClickListener {
                        progressListener.onPrimaryActionClicked(item)
                    }
                } else {
                    primaryActionButton.setOnClickListener(null)
                }

                cancelActionButton.setOnClickListener {
                    progressListener.onCancelClicked(item)
                }

                root.setOnLongClickListener {
                    progressListener.onMenuClicked(
                        cardProgress,
                        item.downloadId,
                        item.videoInfo.isRegularDownload
                    )
                    true
                }

                executePendingBindings()
            }
        }
    }

    fun setData(progressInfos: List<ProgressInfo>) {
        this.progressInfos = progressInfos
        notifyDataSetChanged()
    }

    private companion object {
        val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
    }
}

interface ProgressListener {
    fun onMenuClicked(view: View, downloadId: Long, isRegular: Boolean)
    fun onPrimaryActionClicked(progressInfo: ProgressInfo)
    fun onCancelClicked(progressInfo: ProgressInfo)
}
