package com.myAllVideoBrowser.ui.main.settings

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.mockito.Mockito.mock

class LauncherSelectionStarterTest {

    @Test
    fun `requestLauncherSelection uses provided context and requests home selection`() {
        val context = mock(Context::class.java)
        val delegate = RecordingLauncherActivationDelegate()
        var capturedContext: Context? = null

        requestLauncherSelection(context) {
            capturedContext = it
            delegate
        }

        assertSame(context, capturedContext)
        assertEquals(
            listOf(
                "enable:com.neoapps.neolauncher.FakeLauncher",
                "open_home_picker",
                "restore:com.neoapps.neolauncher.FakeLauncher"
            ),
            delegate.events
        )
    }

    private class RecordingLauncherActivationDelegate : LauncherActivationDelegate {
        val events = mutableListOf<String>()

        override fun enableFakeLauncher(className: String) {
            events += "enable:$className"
        }

        override fun openHomePicker() {
            events += "open_home_picker"
        }

        override fun restoreFakeLauncher(className: String) {
            events += "restore:$className"
        }

        override fun openHomeSettings() {
            events += "open_home_settings"
        }
    }
}
