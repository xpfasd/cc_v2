package com.myAllVideoBrowser.ui.component.adapter

import android.net.Uri
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.ItemWebTabButtonBinding
import com.myAllVideoBrowser.ui.main.home.browser.webTab.WebTab
import com.myAllVideoBrowser.ui.main.home.browser.webTab.WebTabPreviewStateFactory

interface WebTabsListener {
    fun onCloseTabClicked(webTab: WebTab)
    fun onSelectTabClicked(webTab: WebTab)
}

class WebTabsAdapter(
    private var webTabs: List<WebTab>,
    private var webTabsListener: WebTabsListener
) : RecyclerView.Adapter<WebTabsAdapter.WebTabsViewHolder>() {

    private var selectedTabIndex: Int = 0

    class WebTabsViewHolder(val binding: ItemWebTabButtonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(webTab: WebTab, webTabsListener: WebTabsListener, isSelected: Boolean) {
            with(binding)
            {
                val context = this.root.context
                val displayTitle = when {
                    webTab.isHome() -> context.getString(R.string.browser_tabs_overview_home_title)
                    webTab.getTitle().isNotBlank() -> webTab.getTitle()
                    else -> Uri.parse(webTab.getUrl()).host ?: webTab.getUrl()
                }

                this.webTab = webTab
                this.tabListener = webTabsListener

                itemWebTabButton.setCardBackgroundColor(
                    if (isSelected) {
                        Color.parseColor("#FFFDF9E8")
                    } else {
                        ContextCompat.getColor(context, R.color.white)
                    }
                )
                itemWebTabButton.strokeWidth = if (isSelected) {
                    (context.resources.displayMetrics.density * 1.5f).toInt().coerceAtLeast(1)
                } else {
                    1
                }
                itemWebTabButton.strokeColor = ContextCompat.getColor(
                    context,
                    if (isSelected) {
                        R.color.browser_tabs_overview_selected_stroke
                    } else {
                        R.color.browser_tabs_overview_card_stroke
                    }
                )

                this.closeTab.visibility = if (webTab.isHome()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

                this.tabTitle.text = displayTitle

                val previewBytes = webTab.getPreviewBytes()
                val previewState = WebTabPreviewStateFactory.create(
                    previewBytes = previewBytes,
                    isHomeTab = webTab.isHome()
                )
                previewImage.visibility = if (previewState.showPreviewImage) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                previewPlaceholder.visibility = if (previewState.showPlaceholder) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
                if (previewState.showPreviewImage) {
                    Glide.with(previewImage)
                        .load(previewBytes)
                        .centerCrop()
                        .into(previewImage)
                } else {
                    Glide.with(previewImage).clear(previewImage)
                    previewImage.setImageDrawable(null)
                }

                tabTitle.setTypeface(
                    tabTitle.typeface,
                    Typeface.NORMAL
                )
                tabTitle.setTextColor(
                    if (isSelected) {
                        ContextCompat.getColor(context, R.color.black)
                    } else {
                        ContextCompat.getColor(context, R.color.black)
                    }
                )
                previewPlaceholder.alpha = if (isSelected) 0.9f else 1f
                previewImage.alpha = if (isSelected) 0.98f else 1f
                closeTab.alpha = 1f

                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebTabsViewHolder {
        val binding = DataBindingUtil.inflate<ItemWebTabButtonBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_web_tab_button, parent, false
        )

        return WebTabsViewHolder(binding)
    }

    override fun getItemCount() = webTabs.size

    override fun onBindViewHolder(holder: WebTabsViewHolder, position: Int) =
        holder.bind(webTabs[position], webTabsListener, position == selectedTabIndex)

    fun setData(webTabs: List<WebTab>) {
        this.webTabs = webTabs
        notifyDataSetChanged()
    }

    fun setSelectedTabIndex(index: Int) {
        selectedTabIndex = index.coerceAtLeast(0)
        notifyDataSetChanged()
    }
}
