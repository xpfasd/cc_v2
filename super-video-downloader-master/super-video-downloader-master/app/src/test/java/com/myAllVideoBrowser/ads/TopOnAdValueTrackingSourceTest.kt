package com.myAllVideoBrowser.ads

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class TopOnAdValueTrackingSourceTest {
    private val topOnModelsFile =
        File("../topon-ads/src/main/java/com/cc/ads/topon/TopOnAdModels.kt")
    private val topOnAdsFile =
        File("../topon-ads/src/main/java/com/cc/ads/topon/TopOnAds.kt")
    private val applicationFile =
        File("src/main/java/com/myAllVideoBrowser/DLApplication.kt")
    private val trackerFile =
        File("src/main/java/com/myAllVideoBrowser/ads/TopOnAdValueTracker.kt")
    private val appBuildFile =
        File("build.gradle.kts")

    @Test
    fun topOnAdInfo_exposesRevenueFieldsNeededForValueTracking() {
        val source = topOnModelsFile.readText()

        assertTrue(source.contains("val country: String"))
        assertTrue(source.contains("val networkFirmId: String"))
        assertTrue(source.contains("val publisherRevenue: Double"))
        assertTrue(source.contains("country = info.country.orEmpty()"))
        assertTrue(source.contains("networkFirmId = info.networkFirmId.toString()"))
        assertTrue(source.contains("publisherRevenue = info.publisherRevenue"))
    }

    @Test
    fun topOnAds_supportsGlobalAdEventListeners() {
        val source = topOnAdsFile.readText()

        assertTrue(source.contains("private val globalCallbacks"))
        assertTrue(source.contains("fun addGlobalAdEventListener(callback: TopOnAdCallback)"))
        assertTrue(source.contains("fun removeGlobalAdEventListener(callback: TopOnAdCallback)"))
        assertTrue(source.contains("private fun notifyGlobalCallbacks(event: TopOnAdEvent)"))
        assertTrue(source.contains("notifyGlobalCallbacks(event)"))
        assertTrue(source.contains("setAdRevenueListener"))
        assertTrue(source.contains("TopOnAdEvent.RevenuePaid"))
        assertTrue(source.contains("override fun onAdRevenuePaid(info: TUAdInfo?)"))
    }

    @Test
    fun valueTracker_buildsFirebaseRevenuePayloadAndLogsEvents() {
        val source = trackerFile.readText()

        assertTrue(source.contains("object TopOnAdValueTracker"))
        assertTrue(source.contains("TopOnAds.addGlobalAdEventListener(listener)"))
        assertTrue(source.contains("event is TopOnAdEvent.RevenuePaid"))
        assertTrue(source.contains("AppLogger.d(\"TopOn ad value tracker installed\")"))
        assertTrue(source.contains("AppLogger.w(\"TopOn ad value tracking skipped: ad info is null\")"))
        assertTrue(source.contains("AppLogger.d("))
        assertTrue(source.contains("TopOn ad value tracking start: placementId="))
        assertTrue(source.contains("TopOn ad impression reported: placementId="))
        assertTrue(source.contains("TopOn ad revenue reported: placementId="))
        assertTrue(source.contains("TopOn ad value tracking failed:"))
        assertTrue(source.contains("FirebaseAnalytics.Event.AD_IMPRESSION"))
        assertTrue(source.contains("FirebaseAnalytics.Param.VALUE"))
        assertTrue(source.contains("FirebaseAnalytics.Param.CURRENCY"))
        assertTrue(source.contains("FirebaseAnalytics.Param.AD_PLATFORM"))
        assertTrue(source.contains("bundle.putDouble(ReportEventConstants.KEY_ECPM"))
        assertTrue(source.contains("bundle.putString(ReportEventConstants.KEY_COUNTRY"))
        assertTrue(source.contains("bundle.putString(ReportEventConstants.KEY_PLACEMENT_ID"))
        assertTrue(source.contains("bundle.putString(ReportEventConstants.KEY_FORMAT"))
        assertTrue(source.contains("bundle.putString(FirebaseAnalytics.Param.CURRENCY"))
        assertTrue(source.contains("bundle.putString(FirebaseAnalytics.Param.AD_PLATFORM"))
        assertTrue(source.contains("bundle.putDouble(FirebaseAnalytics.Param.VALUE"))
        assertTrue(source.contains("reportAdImpress(bundle)"))
        assertTrue(source.contains("reportAdRevenue(bundle)"))
    }

    @Test
    fun topOnAdEvent_definesDedicatedRevenuePaidSignal() {
        val source = topOnModelsFile.readText()

        assertTrue(source.contains("data class RevenuePaid(val format: TopOnAdFormat, val info: TopOnAdInfo?)"))
    }

    @Test
    fun application_installsTopOnValueTrackerOnStartup() {
        val source = applicationFile.readText()

        assertTrue(source.contains("TopOnAdValueTracker.install(applicationContext)"))
    }

    @Test
    fun appBuild_addsFirebaseAnalyticsDependency() {
        val source = appBuildFile.readText()

        assertTrue(source.contains("com.google.firebase:firebase-analytics-ktx"))
    }
}
