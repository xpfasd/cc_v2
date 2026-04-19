package com.myAllVideoBrowser.ads

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class TopOnNativeLoggingSourceTest {
    private val sceneManagerFile =
        File("../topon-ads/src/main/java/com/cc/ads/topon/TopOnAdSceneManager.kt")
    private val topOnAdsFile =
        File("../topon-ads/src/main/java/com/cc/ads/topon/TopOnAds.kt")
    private val browserHomeFile =
        File("src/main/java/com/myAllVideoBrowser/ui/main/home/browser/homeTab/BrowserHomeFragment.kt")

    @Test
    fun sceneManager_logsNativePreloadAndRenderFlow() {
        val source = sceneManagerFile.readText()

        assertTrue(source.contains("TopOn native preload requested"))
        assertTrue(source.contains("TopOn native render requested"))
        assertTrue(source.contains("TopOn native handle missing or invalid"))
        assertTrue(source.contains("TopOn native loaded event received for rerender"))
        assertTrue(source.contains("TopOn native renderExpress result"))
        assertTrue(source.contains("TopOn native renderExpress metadata"))
        assertTrue(source.contains("TopOn native standard render fallback result"))
        assertTrue(source.contains("TopOn native container visible"))
        assertTrue(source.contains("TopOn native render failed, hiding container"))
    }

    @Test
    fun nativeLoader_dispatchesLatestCallbackAndLogsSdkEvents() {
        val source = topOnAdsFile.readText()

        assertTrue(source.contains("private fun dispatchCallback(event: TopOnAdEvent)"))
        assertTrue(source.contains("TopOn native callback updated"))
        assertTrue(source.contains("TopOn native load using existing callback"))
        assertTrue(source.contains("TopOn native loaded: placementId="))
        assertTrue(source.contains("TopOn native load fail: placementId="))
        assertTrue(source.contains("dispatchCallback(TopOnAdEvent.Loaded(TopOnAdFormat.NATIVE))"))
        assertTrue(source.contains("dispatchCallback(TopOnAdEvent.Failed(TopOnAdFormat.NATIVE"))
        assertTrue(source.contains("TopOn native handle requested"))
        assertTrue(source.contains("TopOn native handle is null"))
        assertTrue(source.contains("TopOn native handle created"))
        assertTrue(source.contains("fun renderStandard(adView: TUNativeAdView, fullscreen: Boolean = false): Boolean"))
        assertTrue(source.contains("material.getAdMediaView()"))
        assertTrue(source.contains("TUNativePrepareInfo()"))
        assertTrue(source.contains("TopOn native impressed"))
        assertTrue(source.contains("TopOn native clicked"))
        assertTrue(source.contains("TopOn native video start"))
        assertTrue(source.contains("TopOn native video end"))
        assertTrue(source.contains("val isCompact = !fullscreen"))
        assertTrue(source.contains("orientation = if (isCompact) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL"))
        assertTrue(source.contains("dp(context, 76)"))
        assertTrue(source.contains("if (fullscreen) 0 else dp(context, 180)"))
        assertTrue(source.contains("maxLines = if (isCompact) 2 else 1"))
        assertTrue(source.contains("maxLines = if (isCompact) 1 else if (fullscreen) 4 else 2"))
    }

    @Test
    fun browserHomeFragment_logsHomeTopNativeEntryPoints() {
        val source = browserHomeFile.readText()

        assertTrue(source.contains("Log.d(TAG, \"HOME_TOP_NATIVE preload requested"))
        assertTrue(source.contains("Log.d(TAG, \"HOME_TOP_NATIVE render requested"))
    }
}
