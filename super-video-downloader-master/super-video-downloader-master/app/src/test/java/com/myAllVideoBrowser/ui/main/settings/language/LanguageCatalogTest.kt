package com.myAllVideoBrowser.ui.main.settings.language

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageCatalogTest {

    private val systemLanguageLabel = "\u672c\u673a\u8bed\u8a00"

    @Test
    fun `buildLanguageOptions puts device language first and includes supported locales`() {
        val options = buildLanguageOptions(
            systemLabel = systemLanguageLabel,
            deviceLocale = Locale.SIMPLIFIED_CHINESE
        )

        assertEquals(SYSTEM_LANGUAGE_TAG, options.first().tag)
        assertEquals(systemLanguageLabel, options.first().displayName)
        assertTrue(options.first().supportingText.contains(Locale.SIMPLIFIED_CHINESE.toLanguageTag()))
        assertTrue(options.size > 10)
        assertTrue(options.any { it.tag == "en" })
        assertTrue(options.any { it.tag == "pt-BR" })
        assertTrue(options.any { it.tag == "zh-TW" })
    }

    @Test
    fun `resolveLanguageSelectionTag keeps system for blank values`() {
        val availableTags = buildLanguageOptions("System").map { it.tag }

        assertEquals(
            SYSTEM_LANGUAGE_TAG,
            resolveLanguageSelectionTag("", availableTags)
        )
        assertEquals(
            SYSTEM_LANGUAGE_TAG,
            resolveLanguageSelectionTag(SYSTEM_LANGUAGE_TAG, availableTags)
        )
    }

    @Test
    fun `resolveLanguageSelectionTag normalizes locale variants to supported tags`() {
        val availableTags = buildLanguageOptions("System").map { it.tag }

        assertEquals("en", resolveLanguageSelectionTag("en-US", availableTags))
        assertEquals("zh-TW", resolveLanguageSelectionTag("zh-Hant-TW", availableTags))
        assertEquals("pt", resolveLanguageSelectionTag("pt-PT", availableTags))
        assertEquals(SYSTEM_LANGUAGE_TAG, resolveLanguageSelectionTag("ar", availableTags))
    }
}
