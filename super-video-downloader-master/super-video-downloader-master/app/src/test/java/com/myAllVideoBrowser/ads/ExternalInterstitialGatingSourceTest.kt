package com.myAllVideoBrowser.ads

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalInterstitialGatingSourceTest {
    private val sceneManagerFile =
        File("../topon-ads/src/main/java/com/cc/ads/topon/TopOnAdSceneManager.kt")

    @Test
    fun `launcher external interstitial preload and show are both gated`() {
        val source = sceneManagerFile.readText()

        assertTrue(source.contains("if (!canUseExternalInterstitial(context))"))
        assertTrue(source.contains("if (!canUseExternalInterstitial(activity))"))
        assertTrue(source.contains("TopOn launcher interstitial preload skipped"))
        assertTrue(source.contains("TopOn launcher interstitial show skipped"))
    }
}
