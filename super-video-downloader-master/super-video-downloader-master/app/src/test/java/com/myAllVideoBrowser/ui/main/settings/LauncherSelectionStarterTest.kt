package com.myAllVideoBrowser.ui.main.settings

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import org.mockito.Mockito.mock

class LauncherSelectionStarterTest {

    @Test
    fun `requestLauncherSelection uses provided context and requests home selection`() {
        val context = mock(Context::class.java)
        @Suppress("UNCHECKED_CAST")
        val roleRequestLauncher = mock(ActivityResultLauncher::class.java) as ActivityResultLauncher<Intent>
        val delegate = RecordingLauncherActivationDelegate()
        var capturedContext: Context? = null
        var capturedLauncher: ActivityResultLauncher<Intent>? = null

        requestLauncherSelection(context, roleRequestLauncher) { providedContext, providedLauncher ->
            capturedContext = providedContext
            capturedLauncher = providedLauncher
            delegate
        }

        assertSame(context, capturedContext)
        assertSame(roleRequestLauncher, capturedLauncher)
        assertEquals(
            listOf(
                "mark_return_to_app",
                "request_home_role"
            ),
            delegate.events
        )
    }

    private class RecordingLauncherActivationDelegate : LauncherActivationDelegate {
        val events = mutableListOf<String>()

        override fun markReturnToAppAfterHomeSelection() {
            events += "mark_return_to_app"
        }

        override fun isHomeRoleRequestAvailable(): Boolean = true

        override fun requestHomeRole() {
            events += "request_home_role"
        }

        override fun openHomeSettings() {
            events += "open_home_settings"
        }
    }
}
