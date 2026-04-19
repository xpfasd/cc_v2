package com.cc.ads.topon

import android.content.Context

internal object TopOnAdAttributionStore {
    private const val PREF_KEY = "settings_prefs"
    private const val IS_AD_ATTRIBUTED_USER = "IS_AD_ATTRIBUTED_USER"
    private const val AD_ATTRIBUTION_MATCHED_AT_MILLIS = "AD_ATTRIBUTION_MATCHED_AT_MILLIS"

    fun canUseExternalInterstitial(context: Context, nowMillis: Long = System.currentTimeMillis()): Boolean {
        val prefs = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
        return ExternalInterstitialEligibility.canLoadOrShow(
            isAdAttributedUser = prefs.getBoolean(IS_AD_ATTRIBUTED_USER, false),
            attributionMatchedAtMillis = prefs.getLong(AD_ATTRIBUTION_MATCHED_AT_MILLIS, 0L),
            nowMillis = nowMillis
        )
    }
}
