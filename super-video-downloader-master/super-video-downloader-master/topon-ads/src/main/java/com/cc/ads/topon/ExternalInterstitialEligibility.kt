package com.cc.ads.topon

object ExternalInterstitialEligibility {
    const val MIN_DELAY_MILLIS: Long = 3L * 60L * 60L * 1000L

    fun canLoadOrShow(
        isAdAttributedUser: Boolean,
        attributionMatchedAtMillis: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (!isAdAttributedUser) {
            return false
        }
        if (attributionMatchedAtMillis <= 0L) {
            return true
        }
        return nowMillis >= attributionMatchedAtMillis + MIN_DELAY_MILLIS
    }
}
