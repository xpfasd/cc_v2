package com.myAllVideoBrowser.ui.main.home

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class GuideNativeFlowSourceTest {

    @Test
    fun `guide native ad can be dismissed by swipe edges or close button`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        assertTrue(source.contains("onboardingNativeAdPager"))
        assertTrue(source.contains("dismissGuideNativeAdAndAdvance()"))
        assertTrue(source.contains("findViewById<View>(R.id.onboarding_native_ad_close).setOnClickListener"))
        assertTrue(source.contains("position != GUIDE_NATIVE_AD_CENTER_PAGE"))
        assertTrue(source.contains("dismissGuideNativeAdAndAdvance()"))
    }

    @Test
    fun `guide native pager binding only updates render container and does not trigger duplicate render`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        val adapterBlock = source.substringAfter("private val guideNativePagerAdapter = GuideNativePagerAdapter { container ->")
            .substringBefore("\n    }")

        assertTrue(adapterBlock.contains("guideNativeRenderContainer = container"))
        assertTrue(!adapterBlock.contains("renderGuideNativeAdInto(container)"))
    }
}
