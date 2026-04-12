package com.myAllVideoBrowser.ui.main.settings.language

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.cc.ads.topon.TopOnAdSceneManager
import com.cc.ads.topon.TopOnAdScenes
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.ActivityLanguageSettingsBinding
import com.myAllVideoBrowser.ui.main.base.BaseActivity
import com.myAllVideoBrowser.util.SharedPrefHelper
import javax.inject.Inject

class LanguageSettingsActivity : BaseActivity() {

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper

    private lateinit var binding: ActivityLanguageSettingsBinding
    private lateinit var adapter: LanguageSettingsAdapter
    private lateinit var selectionState: LanguageSelectionState

    private val languages by lazy {
        buildLanguageOptions(
            systemLabel = resolveSystemLanguageLabel()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_language_settings)

        val requestedTag = intent.getStringExtra(EXTRA_INITIAL_LANGUAGE_TAG)
            ?: sharedPrefHelper.getSelectedLanguageTag()
        val resolvedTag = resolveLanguageSelectionTag(
            requestedTag = requestedTag,
            availableTags = languages.map { it.tag }
        )

        selectionState = LanguageSelectionState(
            initialLanguageTag = resolvedTag,
            selectedLanguageTag = savedInstanceState?.getString(STATE_SELECTED_LANGUAGE_TAG)
                ?: resolvedTag
        )

        setupHeader()
        setupLanguageList()
        setupActions()
        TopOnAdSceneManager.preloadNative(applicationContext, TopOnAdScenes.LANGUAGE_NATIVE)
        binding.languageNativeAdContainer.post {
            TopOnAdSceneManager.renderNativeInto(
                binding.languageNativeAdContainer,
                TopOnAdScenes.LANGUAGE_NATIVE
            )
        }

        onBackPressedDispatcher.addCallback(this) {
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_SELECTED_LANGUAGE_TAG, selectionState.selectedLanguageTag)
        super.onSaveInstanceState(outState)
    }

    private fun setupHeader() {
        binding.languageBackButton.setOnClickListener {
            finish()
        }
    }

    private fun setupLanguageList() {
        adapter = LanguageSettingsAdapter { language ->
            selectionState.select(language.tag)
            adapter.updateSelection(selectionState.selectedLanguageTag)
        }
        binding.languageList.layoutManager = LinearLayoutManager(this)
        binding.languageList.adapter = adapter
        adapter.submitLanguages(languages, selectionState.selectedLanguageTag)
    }

    private fun setupActions() {
        binding.languageDoneButton.setOnClickListener {
            applySelectedLanguage()
        }
    }

    private fun applySelectedLanguage() {
        val selectedTag = selectionState.commitSelection()
        sharedPrefHelper.saveSelectedLanguageTag(selectedTag)
        val locales = if (selectedTag == SYSTEM_LANGUAGE_TAG) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(selectedTag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
        finish()
    }

    companion object {
        private const val EXTRA_INITIAL_LANGUAGE_TAG = "extra_initial_language_tag"
        private const val STATE_SELECTED_LANGUAGE_TAG = "state_selected_language_tag"

        fun createIntent(context: Context, initialLanguageTag: String): Intent {
            return Intent(context, LanguageSettingsActivity::class.java).apply {
                putExtra(EXTRA_INITIAL_LANGUAGE_TAG, initialLanguageTag)
            }
        }
    }

    private fun resolveSystemLanguageLabel(): String {
        val currentLocale = resources.configuration.locales[0]
        return if (currentLocale.language == "zh") {
            if (currentLocale.country.equals("TW", ignoreCase = true)) {
                "\u672c\u6a5f\u8a9e\u8a00"
            } else {
                "\u672c\u673a\u8bed\u8a00"
            }
        } else {
            getString(R.string.language_system_default)
        }
    }
}
