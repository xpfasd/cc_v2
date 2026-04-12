package com.myAllVideoBrowser.ui.main.settings.language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.ItemLanguageRowBinding

class LanguageSettingsAdapter(
    private val onLanguageClicked: (LanguageOption) -> Unit
) : RecyclerView.Adapter<LanguageSettingsAdapter.LanguageViewHolder>() {

    private val items = mutableListOf<LanguageOption>()
    private var selectedTag: String? = null

    fun submitLanguages(languages: List<LanguageOption>, selectedLanguageTag: String) {
        items.clear()
        items.addAll(languages)
        selectedTag = if (items.any { it.tag == selectedLanguageTag }) {
            selectedLanguageTag
        } else {
            items.firstOrNull()?.tag
        }
        notifyDataSetChanged()
    }

    fun updateSelection(languageTag: String) {
        val previousIndex = items.indexOfFirst { it.tag == selectedTag }
        val nextIndex = items.indexOfFirst { it.tag == languageTag }
        if (previousIndex == nextIndex && nextIndex != -1) {
            return
        }
        selectedTag = languageTag
        if (previousIndex != -1) {
            notifyItemChanged(previousIndex)
        }
        if (nextIndex != -1) {
            notifyItemChanged(nextIndex)
        }
    }

    fun getSelectedLanguage(): LanguageOption? {
        return items.firstOrNull { it.tag == selectedTag }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(items[position], items[position].tag == selectedTag)
    }

    override fun getItemCount(): Int = items.size

    inner class LanguageViewHolder(
        private val binding: ItemLanguageRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(language: LanguageOption, isSelected: Boolean) {
            binding.languageName.text = language.displayName
            binding.languageTag.text = language.supportingText
            binding.languageState.isSelected = isSelected
            binding.languageCard.isSelected = isSelected
            binding.languageCard.setCardBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isSelected) R.color.browser_home_shortcut_surface else R.color.white
                )
            )
            binding.languageCard.strokeColor = ContextCompat.getColor(
                binding.root.context,
                if (isSelected) R.color.browser_home_shortcut_stroke else R.color.home_divider
            )
            binding.languageName.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isSelected) R.color.colorPrimary else R.color.textPrimary
                )
            )
            binding.languageTag.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isSelected) R.color.colorPrimaryDark else R.color.home_hint
                )
            )
            binding.languageCard.setOnClickListener {
                onLanguageClicked(language)
            }
        }
    }
}
