package com.myAllVideoBrowser.ui.main.progress

import com.myAllVideoBrowser.util.downloaders.generic_downloader.models.VideoTaskState
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressItemActionStateFactoryTest {

    @Test
    fun `downloading state uses pause as primary action`() {
        val state = ProgressItemActionStateFactory.create(VideoTaskState.DOWNLOADING)

        assertEquals(ProgressPrimaryAction.PAUSE, state.primaryAction)
        assertEquals(true, state.showPrimaryAction)
    }

    @Test
    fun `paused state uses resume as primary action`() {
        val state = ProgressItemActionStateFactory.create(VideoTaskState.PAUSE)

        assertEquals(ProgressPrimaryAction.RESUME, state.primaryAction)
        assertEquals(true, state.showPrimaryAction)
    }

    @Test
    fun `completed state hides primary action`() {
        val state = ProgressItemActionStateFactory.create(VideoTaskState.SUCCESS)

        assertEquals(ProgressPrimaryAction.NONE, state.primaryAction)
        assertEquals(false, state.showPrimaryAction)
    }
}
