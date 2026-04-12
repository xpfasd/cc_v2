package com.myAllVideoBrowser.ui.main.home

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LaunchFlowDecisionsTest {

    @Test
    fun `auto launcher activation is skipped on first start`() {
        assertFalse(shouldAutoRequestLauncherOnLaunch(isFirstStart = true, isDefaultHome = false))
    }

    @Test
    fun `auto launcher activation runs for returning users when app is not default home`() {
        assertTrue(shouldAutoRequestLauncherOnLaunch(isFirstStart = false, isDefaultHome = false))
    }

    @Test
    fun `auto launcher activation is skipped when app is already default home`() {
        assertFalse(shouldAutoRequestLauncherOnLaunch(isFirstStart = false, isDefaultHome = true))
    }
}
