package com.neoapps.neolauncher.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseTelemetryConfigTest {

    @Test
    fun shouldEnableCollection_disablesDebugBuilds() {
        assertFalse(FirebaseTelemetryConfig.shouldEnableCollection("debug"))
    }

    @Test
    fun shouldEnableCollection_enablesNeoBuilds() {
        assertTrue(FirebaseTelemetryConfig.shouldEnableCollection("neo"))
    }

    @Test
    fun shouldEnableCollection_enablesReleaseBuilds() {
        assertTrue(FirebaseTelemetryConfig.shouldEnableCollection("release"))
    }
}
