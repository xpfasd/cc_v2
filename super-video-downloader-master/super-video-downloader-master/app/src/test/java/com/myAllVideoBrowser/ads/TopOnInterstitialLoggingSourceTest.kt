package com.myAllVideoBrowser.ads

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class TopOnInterstitialLoggingSourceTest {
    private val sceneManagerFile =
        File("../topon-ads/src/main/java/com/cc/ads/topon/TopOnAdSceneManager.kt")
    private val topOnAdsFile =
        File("../topon-ads/src/main/java/com/cc/ads/topon/TopOnAds.kt")

    @Test
    fun sceneManager_logsInterstitialLifecycleAndCompletion() {
        val source = sceneManagerFile.readText()

        assertTrue(source.contains("TopOn interstitial show requested"))
        assertTrue(source.contains("TopOn interstitial not ready"))
        assertTrue(source.contains("TopOn interstitial event"))
        assertTrue(source.contains("TopOn interstitial finish"))
        assertTrue(source.contains("TopOn interstitial show returned false"))
    }

    @Test
    fun topOnAds_logsSdkInterstitialCallbacks() {
        val source = topOnAdsFile.readText()

        assertTrue(source.contains("private fun dispatchCallback(event: TopOnAdEvent)"))
        assertTrue(source.contains("dispatchCallback(TopOnAdEvent.Loaded"))
        assertTrue(source.contains("dispatchCallback(TopOnAdEvent.Failed"))
        assertTrue(source.contains("dispatchCallback(TopOnAdEvent.Shown"))
        assertTrue(source.contains("dispatchCallback(TopOnAdEvent.Closed"))
        assertTrue(source.contains("TopOn SDK interstitial callback updated"))
        assertTrue(source.contains("callbackOwner"))
        assertTrue(source.contains("callbackVersion"))
        assertTrue(source.contains("TopOn launch interstitial loaded"))
        assertTrue(source.contains("TopOn launch interstitial load fail"))
        assertTrue(source.contains("TopOn launch interstitial shown"))
        assertTrue(source.contains("TopOn launch interstitial clicked"))
        assertTrue(source.contains("TopOn launch interstitial closed"))
        assertTrue(source.contains("TopOn launch interstitial video start"))
        assertTrue(source.contains("TopOn launch interstitial video end"))
        assertTrue(source.contains("TopOn launch interstitial video error"))
        assertTrue(source.contains("TopOn SDK interstitial show call"))
    }
}
