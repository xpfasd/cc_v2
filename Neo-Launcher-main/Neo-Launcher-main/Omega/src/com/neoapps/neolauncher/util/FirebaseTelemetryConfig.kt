package com.neoapps.neolauncher.util

object FirebaseTelemetryConfig {

    fun shouldEnableCollection(buildType: String): Boolean = buildType != "debug"
}
