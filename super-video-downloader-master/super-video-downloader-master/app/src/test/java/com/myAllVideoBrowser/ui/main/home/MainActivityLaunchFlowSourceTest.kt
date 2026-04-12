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
}
