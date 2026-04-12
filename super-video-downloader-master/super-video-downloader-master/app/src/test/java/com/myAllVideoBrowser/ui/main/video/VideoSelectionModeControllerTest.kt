package com.myAllVideoBrowser.ui.main.video

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoSelectionModeControllerTest {

    @Test
    fun `first multi select click enters selection mode and selects all items`() {
        val state = VideoSelectionModeController.onMultiSelectClick(
            selectionMode = false,
            selectedIds = emptySet(),
            availableIds = listOf(1L, 2L, 3L)
        )

        assertTrue(state.selectionMode)
        assertEquals(setOf(1L, 2L, 3L), state.selectedIds)
    }

    @Test
    fun `multi select click selects all items when already in selection mode`() {
        val state = VideoSelectionModeController.onMultiSelectClick(
            selectionMode = true,
            selectedIds = emptySet(),
            availableIds = listOf(1L, 2L, 3L)
        )

        assertTrue(state.selectionMode)
        assertEquals(setOf(1L, 2L, 3L), state.selectedIds)
    }

    @Test
    fun `toggle all clears selection but keeps selection mode when all items were selected`() {
        val state = VideoSelectionModeController.toggleAllSelection(
            selectedIds = setOf(1L, 2L, 3L),
            availableIds = listOf(1L, 2L, 3L)
        )

        assertTrue(state.selectionMode)
        assertTrue(state.selectedIds.isEmpty())
    }

    @Test
    fun `toggling the only selected item keeps selection mode and clears only that item`() {
        val state = VideoSelectionModeController.toggleItemSelection(
            selectedIds = setOf(2L),
            toggledId = 2L
        )

        assertTrue(state.selectionMode)
        assertTrue(state.selectedIds.isEmpty())
    }

    @Test
    fun `toggling one item from all selected keeps other selections intact`() {
        val state = VideoSelectionModeController.toggleItemSelection(
            selectedIds = setOf(1L, 2L, 3L),
            toggledId = 2L
        )

        assertTrue(state.selectionMode)
        assertEquals(setOf(1L, 3L), state.selectedIds)
    }
}
