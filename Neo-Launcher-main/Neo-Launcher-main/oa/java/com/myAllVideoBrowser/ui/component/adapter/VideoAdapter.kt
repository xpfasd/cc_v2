package com.myAllVideoBrowser.ui.component.adapter

import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.color.MaterialColors
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.data.local.model.LocalVideo
import com.myAllVideoBrowser.databinding.ItemVideoBinding
import com.myAllVideoBrowser.util.FileUtil

class VideoAdapter(
    private var localVideos: List<LocalVideo>,
    private val videoListener: VideoListener,
    private val fileUtil: FileUtil
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private var selectionMode = false
    private var selectedVideoIds: Set<Long> = emptySet()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = DataBindingUtil.inflate<ItemVideoBinding>(
            LayoutInflater.from(parent.context), R.layout.item_video, parent, false
        )

        return VideoViewHolder(binding, fileUtil)
    }

    override fun getItemCount() = localVideos.size

    override fun getItemId(position: Int): Long = localVideos.getOrNull(position)?.id ?: RecyclerView.NO_ID

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) =
        holder.bind(localVideos[position], videoListener, selectionMode, selectedVideoIds.contains(localVideos[position].id))

    class VideoViewHolder(var binding: ItemVideoBinding, var fileUtil: FileUtil) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            localVideo: LocalVideo,
            videoListener: VideoListener,
            selectionMode: Boolean,
            isSelected: Boolean
        ) {
            val size = getScreenResolution(itemView.context)
            val color =
                MaterialColors.getColor(
                    itemView.context,
                    com.google.android.material.R.attr.colorSurfaceVariant,
                    Color.YELLOW
                )

            with(binding) {
                this.localVideo = localVideo
                this.ivThumbnail.setBackgroundColor(color)
                this.ivSelect.isVisible = selectionMode
                this.ivMore.isVisible = !selectionMode
                this.ivPlay.isVisible = localVideo.name.endsWith(".mp4", ignoreCase = true)
                this.ivPlayIcon.isVisible = this.ivPlay.isVisible
                this.ivSelect.setImageResource(
                    if (isSelected) R.drawable.downloaded_ic_selection_checked else R.drawable.downloaded_ic_selection_unchecked
                )
                Glide.with(this@VideoViewHolder.itemView.context).load(localVideo.uri).fitCenter()
                    .error(R.drawable.ic_video_24dp)
                    .placeholder(R.drawable.ic_video_24dp)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .apply(RequestOptions().override(size.first / 8, size.second / 8))
                    .into(this.ivThumbnail)

                root.setOnClickListener {
                    if (selectionMode) {
                        videoListener.onItemLongClicked(localVideo)
                    } else {
                        videoListener.onItemClicked(localVideo)
                    }
                }
                root.setOnLongClickListener {
                    if (selectionMode) {
                        videoListener.onItemLongClicked(localVideo)
                        true
                    } else {
                        false
                    }
                }
                ivSelect.setOnClickListener {
                    videoListener.onItemLongClicked(localVideo)
                }
                ivMore.setOnClickListener { view ->
                    videoListener.onMenuClicked(view, localVideo)
                }

                executePendingBindings()
            }
        }

        private fun getScreenResolution(context: Context): Pair<Int, Int> {
            val displayMetrics = DisplayMetrics()
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            windowManager.defaultDisplay.getMetrics(displayMetrics)

            val widthPixels = displayMetrics.widthPixels
            val heightPixels = displayMetrics.heightPixels

            return Pair(widthPixels, heightPixels)
        }
    }

    fun setData(localVideos: List<LocalVideo>) {
        this.localVideos = localVideos
        notifyDataSetChanged()
    }

    fun setSelectionState(selectionMode: Boolean, selectedVideoIds: Set<Long>) {
        this.selectionMode = selectionMode
        this.selectedVideoIds = selectedVideoIds
        notifyDataSetChanged()
    }
}

interface VideoListener {
    fun onItemClicked(localVideo: LocalVideo)
    fun onItemLongClicked(localVideo: LocalVideo)
    fun onMenuClicked(view: View, localVideo: LocalVideo)
}

@GlideModule
class MyGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setLogLevel(Log.ERROR)
    }
}
