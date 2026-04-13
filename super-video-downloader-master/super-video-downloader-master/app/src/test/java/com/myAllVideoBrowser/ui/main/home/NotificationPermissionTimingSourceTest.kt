package com.myAllVideoBrowser.ui.main.home

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationPermissionTimingSourceTest {

    @Test
    fun `main activity does not request notification permission before launch flow completes`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        val onCreateBody = source.substringAfter("override fun onCreate(savedInstanceState: Bundle?) {")
            .substringBefore("\n    @SuppressLint(\"MissingSuperCall\")")

        assertFalse(onCreateBody.contains("grantPermissions()"))
    }

    @Test
    fun `finish launch flow requests notification permission after overlay is dismissed`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        val methodBody = source.substringAfter("private fun finishLaunchFlow() {")
            .substringBefore("\n    private fun startSplashProgress() {")

        assertTrue(methodBody.contains("maybeRequestNotificationPermission()"))
    }
}
