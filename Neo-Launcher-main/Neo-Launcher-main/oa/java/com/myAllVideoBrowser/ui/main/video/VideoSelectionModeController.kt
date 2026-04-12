package com.myAllVideoBrowser.ui.main.video

data class VideoSelectionState(
    val selectionMode: Boolean,
    val selectedIds: Set<Long>
)

object VideoSelectionModeController {

    fun onMultiSelectClick(
        selectionMode: Boolean,
        selectedIds: Set<Long>,
        availableIds: List<Long>
    ): VideoSelectionState {
        return toggleAllSelection(selectedIds, availableIds)
    }

    fun toggleAllSelection(
        selectedIds: Set<Long>,
        availableIds: List<Long>
    ): VideoSelectionState {
        val availableIdSet = LinkedHashSet(availableIds)
        return if (availableIdSet.isNotEmpty() && selectedIds.size == availableIdSet.size && selectedIds.containsAll(availableIdSet)) {
            VideoSelectionState(
                selectionMode = true,
                selectedIds = emptySet()
            )
        } else {
            VideoSelectionState(
                selectionMode = true,
                selectedIds = availableIdSet
            )
        }
    }

    fun toggleItemSelection(
        selectedIds: Set<Long>,
        toggledId: Long
    ): VideoSelectionState {
        val nextSelectedIds = LinkedHashSet(selectedIds)
        if (!nextSelectedIds.add(toggledId)) {
            nextSelectedIds.remove(toggledId)
        }
        return VideoSelectionState(
            selectionMode = true,
            selectedIds = nextSelectedIds
        )
    }
}
