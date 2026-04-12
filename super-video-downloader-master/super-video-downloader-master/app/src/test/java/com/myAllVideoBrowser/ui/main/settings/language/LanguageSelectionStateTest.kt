package com.myAllVideoBrowser.ui.main.settings.language

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageSelectionStateTest {

    @Test
    fun `initial selection uses provided tag and starts clean`() {
        val state = LanguageSelectionState("fr")

        assertEquals("fr", state.selectedLanguageTag)
        assertFalse(state.hasPendingChanges())
        assertEquals("fr", state.commitSelection())
    }

    @Test
    fun `selecting language only stages change until commit`() {
        val state = LanguageSelectionState("en")

        state.select("es")

        assertEquals("es", state.selectedLanguageTag)
        assertTrue(state.hasPendingChanges())
        assertEquals("es", state.commitSelection())
    }

    @Test
    fun `blank initial tag falls back to default language`() {
        val state = LanguageSelectionState("   ")

        assertEquals(DEFAULT_LANGUAGE_TAG, state.selectedLanguageTag)
        assertFalse(state.hasPendingChanges())
    }

    @Test
    fun `system language tag can be selected and committed`() {
        val state = LanguageSelectionState("en")

        state.select(SYSTEM_LANGUAGE_TAG)

        assertEquals(SYSTEM_LANGUAGE_TAG, state.commitSelection())
        assertTrue(state.hasPendingChanges())
    }
}
