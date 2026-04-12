package com.myAllVideoBrowser.ui.main.settings.language

import java.util.Locale

internal const val SYSTEM_LANGUAGE_TAG = "system"
private val SUPPORTED_LANGUAGE_TAGS = listOf(
    "en",
    "bn",
    "de",
    "el",
    "es",
    "fr",
    "hi",
    "hu",
    "id",
    "it",
    "ja",
    "ko",
    "nl",
    "pl",
    "pt",
    "pt-BR",
    "ru",
    "sv",
    "th",
    "tr",
    "vi",
    "zh-CN",
    "zh-TW",
)

internal fun buildLanguageOptions(
    systemLabel: String,
    deviceLocale: Locale = Locale.getDefault()
): List<LanguageOption> {
    val systemSupportingText = listOf(
        deviceLocale.toLanguageTag(),
        buildLocaleDisplayName(deviceLocale)
    ).filter { it.isNotBlank() }
        .joinToString(" | ")

    return buildList {
        add(
            LanguageOption(
                tag = SYSTEM_LANGUAGE_TAG,
                displayName = systemLabel,
                supportingText = systemSupportingText
            )
        )
        SUPPORTED_LANGUAGE_TAGS.forEach { tag ->
            val locale = Locale.forLanguageTag(tag)
            add(
                LanguageOption(
                    tag = tag,
                    displayName = buildLocaleDisplayName(locale),
                    supportingText = tag
                )
            )
        }
    }
}

internal fun resolveLanguageSelectionTag(
    requestedTag: String?,
    availableTags: Collection<String>
): String {
    val normalizedTag = requestedTag?.trim().orEmpty()
    if (normalizedTag.isBlank() || normalizedTag == SYSTEM_LANGUAGE_TAG) {
        return SYSTEM_LANGUAGE_TAG
    }
    if (normalizedTag in availableTags) {
        return normalizedTag
    }

    val locale = Locale.forLanguageTag(normalizedTag)
    if (locale.language.isBlank()) {
        return SYSTEM_LANGUAGE_TAG
    }

    val sameLanguageAndRegion = availableTags.firstOrNull { candidate ->
        if (candidate == SYSTEM_LANGUAGE_TAG) {
            return@firstOrNull false
        }
        val candidateLocale = Locale.forLanguageTag(candidate)
        candidateLocale.language == locale.language &&
            candidateLocale.country.equals(locale.country, ignoreCase = true) &&
            candidateLocale.country.isNotBlank()
    }
    if (sameLanguageAndRegion != null) {
        return sameLanguageAndRegion
    }

    return availableTags.firstOrNull { candidate ->
        candidate != SYSTEM_LANGUAGE_TAG &&
            Locale.forLanguageTag(candidate).language == locale.language
    } ?: SYSTEM_LANGUAGE_TAG
}

private fun buildLocaleDisplayName(locale: Locale): String {
    val displayName = locale.getDisplayName(locale).trim()
    if (displayName.isEmpty()) {
        return locale.toLanguageTag()
    }
    return displayName.replaceFirstChar { character ->
        if (character.isLowerCase()) {
            character.titlecase(locale)
        } else {
            character.toString()
        }
    }
}
