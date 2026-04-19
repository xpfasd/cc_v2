package com.myAllVideoBrowser.ads

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class TopOnSplashLoggingSourceTest {
    private val sceneManagerFile =
        File("../topon-ads/src/main/java/com/cc/ads/topon/TopOnAdSceneManager.kt")
    private val topOnAdsFile =
        File("../topon-ads/src/main/java/com/cc/ads/topon/TopOnAds.kt")

    @Test
    fun sceneManager_logsSplashPreloadAndShowFlow() {
        val source = sceneManagerFile.readText()

        assertTrue(source.contains("TopOn splash preload requested"))
        assertTrue(source.contains("TopOn splash show requested"))
        assertTrue(source.contains("TopOn splash event"))
        assertTrue(source.contains("TopOn splash show returned false"))
    }

    @Test
    fun topOnAds_logsSdkSplashCallbacks() {
        val source = topOnAdsFile.readText()

        assertTrue(source.contains("TopOn splash callback updated"))
        assertTrue(source.contains("TopOn splash load using existing callback"))
        assertTrue(source.contains("TopOn splash loaded"))
        assertTrue(source.contains("TopOn splash load timeout"))
        assertTrue(source.contains("TopOn splash no ad error"))
        assertTrue(source.contains("TopOn splash shown"))
        assertTrue(source.contains("TopOn splash clicked"))
        assertTrue(source.contains("TopOn splash dismissed"))
        assertTrue(source.contains("TopOn splash show call"))
        assertTrue(source.contains("private fun dispatchCallback(event: TopOnAdEvent)"))
    }
}
