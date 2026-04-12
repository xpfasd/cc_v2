package com.myAllVideoBrowser.ui.main.settings.language

internal const val DEFAULT_LANGUAGE_TAG = SYSTEM_LANGUAGE_TAG

internal class LanguageSelectionState(
    initialLanguageTag: String,
    selectedLanguageTag: String = initialLanguageTag
) {

    private val initialTag: String = initialLanguageTag
        .trim()
        .ifBlank { DEFAULT_LANGUAGE_TAG }

    var selectedLanguageTag: String = selectedLanguageTag
        .trim()
        .ifBlank { initialTag }
        private set

    fun select(languageTag: String) {
        selectedLanguageTag = languageTag.trim().ifBlank { initialTag }
    }

    fun hasPendingChanges(): Boolean {
        return selectedLanguageTag != initialTag
    }

    fun commitSelection(): String {
        return selectedLanguageTag
    }
}
