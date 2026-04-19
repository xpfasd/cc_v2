package com.myAllVideoBrowser.ui.main.home

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView

class GuideNativePagerAdapter(
    private val onCenterPageBound: (FrameLayout) -> Unit
) : RecyclerView.Adapter<GuideNativePagerAdapter.GuideNativePageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideNativePageViewHolder {
        val container = FrameLayout(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.TRANSPARENT)
        }
        return GuideNativePageViewHolder(container, parent.context)
    }

    override fun onBindViewHolder(holder: GuideNativePageViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = PAGE_COUNT

    inner class GuideNativePageViewHolder(
        private val root: FrameLayout,
        context: Context
    ) : RecyclerView.ViewHolder(root) {
        private val adContainer = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        fun bind(position: Int) {
            root.removeAllViews()
            if (position == CENTER_PAGE_INDEX) {
                root.addView(adContainer)
                onCenterPageBound(adContainer)
            }
        }
    }

    companion object {
        const val PAGE_COUNT = 3
        const val CENTER_PAGE_INDEX = 1
    }
}
