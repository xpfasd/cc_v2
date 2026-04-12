package com.neoapps.neolauncher.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OmegaLocaleResourcesTest {

    private val downloaderResDir = File(
        "../../super-video-downloader-master/super-video-downloader-master/app/src/main/res"
    )

    @Test
    fun downloaderlibRequestedLanguageFiles_exist() {
        listOf(
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
        ).forEach { language ->
            val stringsFile = File(downloaderResDir, "values-$language/strings.xml")
            assertTrue("Missing translation file for $language", stringsFile.exists())
        }
    }

    @Test
    fun downloaderlibNewLanguageFiles_areNotDefaultEnglishCopies() {
        val defaultStrings = File(downloaderResDir, "values/strings.xml").readText()

        listOf(
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
            "pt",
            "sv",
            "th",
            "tr",
            "vi",
            "zh-rTW",
        ).forEach { language ->
            val localizedStrings = File(downloaderResDir, "values-$language/strings.xml").readText()
            assertNotEquals(
                "Translation file for $language is still identical to the default strings",
                defaultStrings,
                localizedStrings,
            )
        }
    }

    @Test
    fun downloaderlibNewLanguageFiles_preserveFormatPlaceholders() {
        val defaultStrings = File(downloaderResDir, "values/strings.xml").readText()
        val decimalPlaceholderRegex = Regex("%1\\\\${'$'}d")
        val stringPlaceholderRegex = Regex("%1\\\\${'$'}s")
        val expectedDecimalPlaceholderCount = decimalPlaceholderRegex.findAll(defaultStrings).count()
        val expectedStringPlaceholderCount = stringPlaceholderRegex.findAll(defaultStrings).count()

        listOf(
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
            "pt",
            "sv",
            "th",
            "tr",
            "vi",
            "zh-rTW",
        ).forEach { language ->
            val localizedStrings = File(downloaderResDir, "values-$language/strings.xml").readText()

            assertEquals(
                "Decimal placeholder count changed for $language",
                expectedDecimalPlaceholderCount,
                decimalPlaceholderRegex.findAll(localizedStrings).count(),
            )
            assertEquals(
                "String placeholder count changed for $language",
                expectedStringPlaceholderCount,
                stringPlaceholderRegex.findAll(localizedStrings).count(),
            )
        }
    }
}
