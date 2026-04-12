package com.neoapps.neolauncher.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.Locale

class PrefUtilsTest {

    @Test
    fun buildLanguageOptions_returnsOnlySystemDefaultAndApprovedLanguages() {
        val options = buildLanguageOptions(systemLocale = Locale.US)

        assertEquals(
            listOf(
                "",
                "en",
                "de",
                "el",
                "es",
                "fr",
                "hi",
                "hu",
                "it",
                "ja",
                "ko",
                "nl",
                "pl",
                "pt",
                "ru",
                "sv",
                "th",
                "tr",
                "vi",
                "zh-rTW",
            ),
            options.keys.toList()
        )
        assertFalse(options.containsKey("zh-rCN"))
    }
}
