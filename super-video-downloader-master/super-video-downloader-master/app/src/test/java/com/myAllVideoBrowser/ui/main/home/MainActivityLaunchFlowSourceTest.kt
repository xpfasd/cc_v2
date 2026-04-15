package com.myAllVideoBrowser.ui.main.home

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainActivityLaunchFlowSourceTest {

    @Test
    fun `maybeStartLaunchFlow does not short circuit when activity is recreated`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        val methodBody = source.substringAfter("private fun maybeStartLaunchFlow(savedInstanceState: Bundle?) {")
            .substringBefore("\n    private fun showOnboarding()")

        assertTrue(
            "maybeStartLaunchFlow should still schedule the splash delay",
            methodBody.contains("launchFlowHandler.postDelayed")
        )
        assertFalse(
            "maybeStartLaunchFlow should not return early just because savedInstanceState exists",
            methodBody.contains("if (savedInstanceState != null)")
        )
    }

    @Test
    fun `onCreate can skip launch splash when returning from launcher activation`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        val onCreateBody = source.substringAfter("override fun onCreate(savedInstanceState: Bundle?) {")
            .substringBefore("\n    @SuppressLint(\"MissingSuperCall\")")

        assertTrue(onCreateBody.contains("intent.getBooleanExtra(EXTRA_SKIP_LAUNCH_SPLASH, false)"))
        assertTrue(onCreateBody.contains("continueLaunchFlowAfterLauncherReturn()"))
        assertTrue(onCreateBody.contains("maybeStartLaunchFlow(savedInstanceState)"))
    }

    @Test
    fun `onNewIntent can skip launch splash when launcher authorization brings app back`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        val onNewIntentBody = source.substringAfter("override fun onNewIntent(intent: Intent?) {")
            .substringBefore("\n        if (intent?.getBooleanExtra(\n                YoutubeDlDownloaderWorker.IS_FINISHED_DOWNLOAD_ACTION_KEY,")

        assertTrue(onNewIntentBody.contains("intent?.getBooleanExtra(EXTRA_SKIP_LAUNCH_SPLASH, false) == true"))
        assertTrue(onNewIntentBody.contains("continueLaunchFlowAfterLauncherReturn()"))
    }
}
