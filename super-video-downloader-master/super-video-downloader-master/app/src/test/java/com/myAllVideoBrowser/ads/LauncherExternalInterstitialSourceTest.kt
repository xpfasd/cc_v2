package com.myAllVideoBrowser.ads

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherExternalInterstitialSourceTest {
    private val launcherFile =
        File("../../../Neo-Launcher-main/Neo-Launcher-main/src/com/android/launcher3/Launcher.java")

    @Test
    fun launcherInterstitialCallback_continuesLaunchOutsideInitialStartActivityFlow() {
        val source = launcherFile.readText()
        val methodBody = source.substringAfter("public RunnableList startActivitySafely(View v, Intent intent, ItemInfo item) {")
            .substringBefore("\n    private boolean shouldShowTopOnLaunchInterstitial(Intent intent) {")
        val callbackBody = methodBody.substringAfter("TopOnAdSceneManager.INSTANCE.showLauncherAppInterstitial(this, () -> {")
            .substringBefore("return kotlin.Unit.INSTANCE;")

        assertTrue(methodBody.contains("if (shouldShowTopOnLaunchInterstitial(intent)) {"))
        assertTrue(source.contains("private Runnable mPendingPostInterstitialLaunch;"))
        assertTrue(callbackBody.contains("scheduleLaunchAfterTopOnInterstitial(v, intent, item);"))
    }

    @Test
    fun postInterstitialLaunch_waitsForDeferredResumeAndWindowFocus() {
        val source = launcherFile.readText()
        val scheduleBody = source.substringAfter("private void scheduleLaunchAfterTopOnInterstitial(View v, Intent intent, ItemInfo item) {")
            .substringBefore("\n    private void maybeContinuePostInterstitialLaunch() {")
        val helperBody = source.substringAfter("private void maybeContinuePostInterstitialLaunch() {")
            .substringBefore("\n    private boolean shouldShowTopOnLaunchInterstitial(Intent intent) {")
        val deferredResumeBody = source.substringAfter("protected void onDeferredResumed() {")
            .substringBefore("\n    private void checkIfOverlayStillDeferred() {")
        val windowFocusBody = source.substringAfter("public void onWindowFocusChanged(boolean hasFocus) {")
            .substringBefore("\n    @Override\n    public void onStateSetStart(LauncherState state) {")

        assertTrue(scheduleBody.contains("mPendingPostInterstitialLaunch = () -> {"))
        assertTrue(scheduleBody.contains("RunnableList result = super.startActivitySafely(v, intent, item);"))
        assertTrue(scheduleBody.contains("maybeContinuePostInterstitialLaunch();"))
        assertTrue(helperBody.contains("int activityFlags = getActivityFlags();"))
        assertTrue(helperBody.contains("ACTIVITY_STATE_DEFERRED_RESUMED"))
        assertTrue(helperBody.contains("ACTIVITY_STATE_WINDOW_FOCUSED"))
        assertTrue(helperBody.contains("Runnable pendingLaunch = mPendingPostInterstitialLaunch;"))
        assertTrue(deferredResumeBody.contains("maybeContinuePostInterstitialLaunch();"))
        assertTrue(windowFocusBody.contains("if (hasFocus) {"))
        assertTrue(windowFocusBody.contains("maybeContinuePostInterstitialLaunch();"))
    }

    @Test
    fun launcherInterstitialFlow_emitsDebugLogsForRootCauseTracing() {
        val source = launcherFile.readText()

        assertTrue(source.contains("FileLog.d(TAG, \"TopOn launch interstitial requested"))
        assertTrue(source.contains("FileLog.d(TAG, \"TopOn launch interstitial callback"))
        assertTrue(source.contains("FileLog.d(TAG, \"Scheduling post-interstitial launch"))
        assertTrue(source.contains("FileLog.d(TAG, \"Post-interstitial launch still waiting"))
        assertTrue(source.contains("FileLog.d(TAG, \"Running post-interstitial launch"))
    }
}
