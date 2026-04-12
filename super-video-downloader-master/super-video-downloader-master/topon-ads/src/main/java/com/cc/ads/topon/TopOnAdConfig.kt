package com.cc.ads.topon

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

data class TopOnAdConfig(
    val appId: String,
    val appKey: String,
    val placements: TopOnPlacementIds = TopOnPlacementIds()
) {
    val isComplete: Boolean
        get() = appId.isNotBlank() && appKey.isNotBlank()

    companion object {
        const val META_APP_ID = "com.cc.ads.TOPON_APP_ID"
        const val META_APP_KEY = "com.cc.ads.TOPON_APP_KEY"
        const val META_SPLASH_PLACEMENT_ID = "com.cc.ads.TOPON_SPLASH_PLACEMENT_ID"
        const val META_INTERSTITIAL_PLACEMENT_ID = "com.cc.ads.TOPON_INTERSTITIAL_PLACEMENT_ID"
        const val META_REWARDED_PLACEMENT_ID = "com.cc.ads.TOPON_REWARDED_PLACEMENT_ID"
        const val META_BANNER_PLACEMENT_ID = "com.cc.ads.TOPON_BANNER_PLACEMENT_ID"
        const val META_NATIVE_PLACEMENT_ID = "com.cc.ads.TOPON_NATIVE_PLACEMENT_ID"

        fun fromManifest(context: Context): TopOnAdConfig {
            val metaData = context.applicationMetaData()
            return TopOnAdConfig(
                appId = metaData.stringValue(META_APP_ID),
                appKey = metaData.stringValue(META_APP_KEY),
                placements = TopOnPlacementIds(
                    splash = metaData.stringValue(META_SPLASH_PLACEMENT_ID),
                    interstitial = metaData.stringValue(META_INTERSTITIAL_PLACEMENT_ID),
                    rewarded = metaData.stringValue(META_REWARDED_PLACEMENT_ID),
                    banner = metaData.stringValue(META_BANNER_PLACEMENT_ID),
                    native = metaData.stringValue(META_NATIVE_PLACEMENT_ID)
                )
            )
        }

        private fun Context.applicationMetaData(): Bundle {
            val appInfo: ApplicationInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            }
            return appInfo.metaData ?: Bundle.EMPTY
        }

        private fun Bundle.stringValue(key: String): String =
            getString(key) ?: get(key)?.toString().orEmpty()
    }
}

data class TopOnPlacementIds(
    val splash: String = "",
    val interstitial: String = "",
    val rewarded: String = "",
    val banner: String = "",
    val native: String = ""
)
