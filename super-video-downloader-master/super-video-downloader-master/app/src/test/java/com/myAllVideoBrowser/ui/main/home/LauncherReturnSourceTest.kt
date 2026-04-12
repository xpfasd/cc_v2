package com.myAllVideoBrowser.ui.main.home

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherReturnSourceTest {

    @Test
    fun `launcher selection return always finishes launch flow for onboarding path`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        val onResumeBody = source.substringAfter("override fun onResume() {")
            .substringBefore("\n    override fun onDestroy() {")

        assertTrue(onResumeBody.contains("if (suppressLauncherPromptOnResume)"))
        assertTrue(onResumeBody.contains("finishLaunchFlow()"))
        assertTrue(onResumeBody.contains("showLauncherReminder()"))
        assertFalse(onResumeBody.contains("showLauncherPrompt()"))
    }

    @Test
    fun `launcher prompt click exits onboarding before opening system picker`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        val bindViewsBody = source.substringAfter("private fun bindLaunchFlowViews() {")
            .substringBefore("\n    private fun maybeStartLaunchFlow(savedInstanceState: Bundle?) {")

        assertTrue(bindViewsBody.contains("finishLaunchFlow()"))
        assertTrue(bindViewsBody.contains("requestLauncherActivation(skipPromptOnResume = true)"))
    }
}
