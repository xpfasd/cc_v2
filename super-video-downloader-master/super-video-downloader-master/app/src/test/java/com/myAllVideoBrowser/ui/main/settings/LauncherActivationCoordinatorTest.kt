package com.myAllVideoBrowser.ui.main.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LauncherActivationCoordinatorTest {

    @Test
    fun `requestHomeSelection enables fake launcher opens picker and restores component`() {
        val delegate = RecordingLauncherActivationDelegate()

        LauncherActivationCoordinator(delegate).requestHomeSelection()

        assertEquals(
            listOf(
                "enable:com.neoapps.neolauncher.FakeLauncher",
                "open_home_picker",
                "restore:com.neoapps.neolauncher.FakeLauncher"
            ),
            delegate.events
        )
        assertFalse(delegate.openedHomeSettings)
    }

    @Test
    fun `requestHomeSelection falls back to home settings when picker launch fails`() {
        val delegate = RecordingLauncherActivationDelegate(throwOnHomePicker = true)

        LauncherActivationCoordinator(delegate).requestHomeSelection()

        assertEquals(
            listOf(
                "enable:com.neoapps.neolauncher.FakeLauncher",
                "open_home_picker",
                "open_home_settings",
                "restore:com.neoapps.neolauncher.FakeLauncher"
            ),
            delegate.events
        )
        assertEquals(true, delegate.openedHomeSettings)
    }

    private class RecordingLauncherActivationDelegate(
        private val throwOnHomePicker: Boolean = false
    ) : LauncherActivationDelegate {
        val events = mutableListOf<String>()
        var openedHomeSettings = false

        override fun enableFakeLauncher(className: String) {
            events += "enable:$className"
        }

        override fun openHomePicker() {
            events += "open_home_picker"
            if (throwOnHomePicker) {
                throw IllegalStateException("picker failed")
            }
        }

        override fun restoreFakeLauncher(className: String) {
            events += "restore:$className"
        }

        override fun openHomeSettings() {
            openedHomeSettings = true
            events += "open_home_settings"
        }
    }
}
