package com.cc.ads.topon

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

data class TopOnAdConfig(
    val appId: String,
    val appKey: String,
    val isTestMode: Boolean = false,
    val appPackageName: String = "",
    val admobAppId: String = "",
    val placements: TopOnPlacementIds = TopOnPlacementIds()
) {
    val isComplete: Boolean
        get() = appId.isNotBlank() && appKey.isNotBlank()

    companion object {
        const val META_APP_ID = "com.cc.ads.TOPON_APP_ID"
        const val META_APP_KEY = "com.cc.ads.TOPON_APP_KEY"
        const val META_TEST_MODE = "com.cc.ads.TOPON_TEST_MODE"
        const val META_APP_PACKAGE_NAME = "com.cc.ads.TOPON_APP_PACKAGE_NAME"
        const val META_ADMOB_APP_ID = "com.google.android.gms.ads.APPLICATION_ID"
        const val META_SPLASH_PLACEMENT_ID = "com.cc.ads.TOPON_SPLASH_PLACEMENT_ID"
        const val META_INTERSTITIAL_PLACEMENT_ID = "com.cc.ads.TOPON_INTERSTITIAL_PLACEMENT_ID"
        const val META_REWARDED_PLACEMENT_ID = "com.cc.ads.TOPON_REWARDED_PLACEMENT_ID"
        const val META_BANNER_PLACEMENT_ID = "com.cc.ads.TOPON_BANNER_PLACEMENT_ID"
        const val META_NATIVE_PLACEMENT_ID = "com.cc.ads.TOPON_NATIVE_PLACEMENT_ID"

        fun fromManifest(context: Context): TopOnAdConfig {
            val metaData = context.applicationMetaData()
            val isTestMode = metaData.booleanValue(META_TEST_MODE)
            val profile = TopOnAdProfiles.forMode(isTestMode)
            return TopOnAdConfig(
                appId = metaData.stringValue(META_APP_ID).ifBlank { profile.appId },
                appKey = metaData.stringValue(META_APP_KEY).ifBlank { profile.appKey },
                isTestMode = isTestMode,
                appPackageName = metaData.stringValue(META_APP_PACKAGE_NAME)
                    .ifBlank { profile.appPackageName.ifBlank { context.packageName } },
                admobAppId = metaData.stringValue(META_ADMOB_APP_ID).ifBlank { profile.admobAppId },
                placements = TopOnPlacementIds(
                    splash = metaData.stringValue(META_SPLASH_PLACEMENT_ID)
                        .ifBlank { profile.scenes.firstSplash },
                    interstitial = metaData.stringValue(META_INTERSTITIAL_PLACEMENT_ID)
                        .ifBlank { profile.scenes.generalInterstitial },
                    rewarded = metaData.stringValue(META_REWARDED_PLACEMENT_ID),
                    banner = metaData.stringValue(META_BANNER_PLACEMENT_ID),
                    native = metaData.stringValue(META_NATIVE_PLACEMENT_ID)
                        .ifBlank { profile.scenes.homeTopNative }
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

        private fun Bundle.booleanValue(key: String): Boolean {
            val rawValue = get(key) ?: return false
            return when (rawValue) {
                is Boolean -> rawValue
                else -> rawValue.toString().equals("true", ignoreCase = true)
            }
        }
    }
}

data class TopOnPlacementIds(
    val splash: String = "",
    val interstitial: String = "",
    val rewarded: String = "",
    val banner: String = "",
    val native: String = ""
)
