package com.myAllVideoBrowser.ui.main.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LauncherActivationCoordinatorTest {

    @Test
    fun `requestHomeSelection marks return and requests role when available`() {
        val delegate = RecordingLauncherActivationDelegate()

        LauncherActivationCoordinator(delegate).requestHomeSelection()

        assertEquals(
            listOf(
                "mark_return_to_app",
                "request_home_role"
            ),
            delegate.events
        )
        assertFalse(delegate.openedHomeSettings)
    }

    @Test
    fun `requestHomeSelection falls back to home settings when role is unavailable`() {
        val delegate = RecordingLauncherActivationDelegate(isRoleRequestAvailable = false)

        LauncherActivationCoordinator(delegate).requestHomeSelection()

        assertEquals(
            listOf(
                "mark_return_to_app",
                "open_home_settings"
            ),
            delegate.events
        )
        assertEquals(true, delegate.openedHomeSettings)
    }

    @Test
    fun `requestHomeSelection falls back to home settings when role request fails`() {
        val delegate = RecordingLauncherActivationDelegate(throwOnRoleRequest = true)

        LauncherActivationCoordinator(delegate).requestHomeSelection()

        assertEquals(
            listOf(
                "mark_return_to_app",
                "request_home_role",
                "open_home_settings"
            ),
            delegate.events
        )
        assertEquals(true, delegate.openedHomeSettings)
    }

    private class RecordingLauncherActivationDelegate(
        private val isRoleRequestAvailable: Boolean = true,
        private val throwOnRoleRequest: Boolean = false
    ) : LauncherActivationDelegate {
        val events = mutableListOf<String>()
        var openedHomeSettings = false

        override fun markReturnToAppAfterHomeSelection() {
            events += "mark_return_to_app"
        }

        override fun isHomeRoleRequestAvailable(): Boolean = isRoleRequestAvailable

        override fun requestHomeRole() {
            events += "request_home_role"
            if (throwOnRoleRequest) {
                throw IllegalStateException("role request failed")
            }
        }

        override fun openHomeSettings() {
            openedHomeSettings = true
            events += "open_home_settings"
        }
    }
}
