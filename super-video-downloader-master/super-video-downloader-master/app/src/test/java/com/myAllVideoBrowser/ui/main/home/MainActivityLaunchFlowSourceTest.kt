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

    @Test
    fun `splash delay completion checks splash ad before onboarding`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        val startFlowBody = source.substringAfter("private fun maybeStartLaunchFlow(savedInstanceState: Bundle?) {")
            .substringBefore("\n    private fun continueLaunchFlowAfterLauncherReturn()")

        assertTrue(startFlowBody.contains("continueLaunchFlowAfterSplashDelay()"))
        assertTrue(startFlowBody.contains("TopOnAdSceneManager.preloadSplash(applicationContext, firstOpen = isFirstStart)"))
    }

    @Test
    fun `show onboarding no longer tries to show splash ad`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        val showOnboardingBody = source.substringAfter("private fun showOnboarding() {")
            .substringBefore("\n    private fun renderOnboardingPage()")

        assertTrue(showOnboardingBody.contains("showLaunchSurface(launchOnboardingRoot)"))
        assertFalse(showOnboardingBody.contains("showLoadedSplashAd()"))
    }

    @Test
    fun `splash ad ready path shows ad before onboarding and fallback goes straight to onboarding`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        val methodBody = source.substringAfter("private fun continueLaunchFlowAfterSplashDelay() {")
            .substringBefore("\n    private fun continueLaunchFlowAfterLauncherReturn()")

        assertTrue(methodBody.contains("TopOnAdSceneManager.showSplashIfReady(this, launchSplashAdContainer)"))
        assertTrue(methodBody.contains("showOnboarding()"))
        assertTrue(methodBody.contains("if (!shown && !finished)"))
    }

    @Test
    fun `returning users skip onboarding ad preloads and finish launch flow directly`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        val methodBody = source.substringAfter("private fun continueLaunchFlowAfterSplashDelay() {")
            .substringBefore("\n    private fun continueLaunchFlowAfterLauncherReturn()")

        assertTrue(methodBody.contains("val isFirstStart = sharedPrefHelper.getIsFirstStart()"))
        assertTrue(methodBody.contains("if (isFirstStart) {"))
        assertTrue(methodBody.contains("finishLaunchFlow()"))
    }

    @Test
    fun `splash flow emits decision logs for ready and fallback cases`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        assertTrue(source.contains("Splash delay completed"))
        assertTrue(source.contains("Splash flow showing onboarding because launcher prompt is required"))
        assertTrue(source.contains("Splash flow attempted showSplashIfReady"))
        assertTrue(source.contains("Splash flow splash ad finished"))
        assertTrue(source.contains("Splash flow splash ad unavailable, continuing to onboarding"))
    }

    @Test
    fun `returning users request launcher activation directly instead of showing snackbar reminder`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        val finishBody = source.substringAfter("private fun finishLaunchFlow() {")
            .substringBefore("\n    fun isStoragePermissionPromptReady(): Boolean = storagePermissionPromptReady")

        assertTrue(finishBody.contains("shouldAutoRequestLauncherOnLaunch"))
        assertTrue(finishBody.contains("requestLauncherActivation(skipPromptOnResume = true)"))
        assertFalse(source.contains("Snackbar.make("))
        assertFalse(source.contains("private fun showLauncherReminder()"))
    }

    @Test
    fun `launcher authorization resume path does not issue a second launcher prompt`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        val onResumeBody = source.substringAfter("override fun onResume() {")
            .substringBefore("\n    override fun onDestroy() {")

        assertFalse(onResumeBody.contains("requestLauncherActivation(skipPromptOnResume = true)"))
        assertFalse(onResumeBody.contains("shouldRequestLauncherBeforeGuide(launcherPromptAttempts"))
        assertTrue(onResumeBody.contains("if (sharedPrefHelper.getIsFirstStart()) {"))
        assertTrue(onResumeBody.contains("showOnboarding()"))
    }
}
