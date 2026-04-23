package com.myAllVideoBrowser.ads

import android.content.Context
import android.os.Bundle
import androidx.annotation.NonNull
import com.cc.ads.topon.TopOnAdEvent
import com.cc.ads.topon.TopOnAdInfo
import com.cc.ads.topon.TopOnAds
import com.google.firebase.analytics.FirebaseAnalytics
import com.myAllVideoBrowser.util.AppLogger

object TopOnAdValueTracker {
    @Volatile
    private var isInitialized = false

    private val listener: (TopOnAdEvent) -> Unit = { event ->
        if (event is TopOnAdEvent.RevenuePaid) {
            trackAdShownValue(event.info)
        }
    }

    fun install(context: Context) {
        if (isInitialized) {
            return
        }
        synchronized(this) {
            if (isInitialized) {
                return
            }
            FirebaseAnalytics.getInstance(context.applicationContext)
            TopOnAds.addGlobalAdEventListener(listener)
            isInitialized = true
            AppLogger.d("TopOn ad value tracker installed")
        }
    }

    private fun trackAdShownValue(atAdInfo: TopOnAdInfo?) {
        if (atAdInfo == null) {
            AppLogger.w("TopOn ad value tracking skipped: ad info is null")
            return
        }

        try {
            val bundle = Bundle()
            bundle.putDouble(ReportEventConstants.KEY_ECPM, atAdInfo.ecpm / 1000)
            bundle.putString(ReportEventConstants.KEY_COUNTRY, atAdInfo.country)
            bundle.putString(ReportEventConstants.KEY_PLACEMENT_ID, atAdInfo.placementId)
            bundle.putString(ReportEventConstants.KEY_FORMAT, atAdInfo.format)
            bundle.putString(FirebaseAnalytics.Param.CURRENCY, atAdInfo.currency)
            bundle.putString(FirebaseAnalytics.Param.AD_PLATFORM, atAdInfo.networkFirmId)

            val publisherRevenue = atAdInfo.publisherRevenue * publisherRevenueRate()
            bundle.putDouble(FirebaseAnalytics.Param.VALUE, publisherRevenue)

            AppLogger.d(
                "TopOn ad value tracking start: placementId=${atAdInfo.placementId}, " +
                    "format=${atAdInfo.format}, networkFirmId=${atAdInfo.networkFirmId}, " +
                    "ecpm=${atAdInfo.ecpm}, publisherRevenue=$publisherRevenue"
            )
            reportAdImpress(bundle)
            reportAdRevenue(bundle)
        } catch (throwable: Throwable) {
            AppLogger.e("TopOn ad value tracking failed: $throwable")
        }
    }

    private fun publisherRevenueRate(): Double = 1.0

    private fun firebaseAnalytics(): FirebaseAnalytics? {
        if (!isInitialized) {
            return null
        }
        return try {
            FirebaseAnalytics.getInstance(com.myAllVideoBrowser.util.ContextUtils.getApplicationContext())
        } catch (_: Throwable) {
            null
        }
    }

    fun reportAdImpress(@NonNull bundle: Bundle) {
        val analytics = firebaseAnalytics()
        if (analytics == null) {
            AppLogger.w("TopOn ad impression skipped: FirebaseAnalytics unavailable")
            return
        }
        analytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION, bundle)
        AppLogger.d(
            "TopOn ad impression reported: placementId=${bundle.getString(ReportEventConstants.KEY_PLACEMENT_ID)}"
        )
    }

    fun reportAdRevenue(@NonNull bundle: Bundle) {
        val analytics = firebaseAnalytics()
        if (analytics == null) {
            AppLogger.w("TopOn ad revenue skipped: FirebaseAnalytics unavailable")
            return
        }
        analytics.logEvent(ReportEventConstants.KEY_AD_REVENUE, bundle)
        AppLogger.d(
            "TopOn ad revenue reported: placementId=${bundle.getString(ReportEventConstants.KEY_PLACEMENT_ID)}"
        )
    }
}
