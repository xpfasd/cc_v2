package com.myAllVideoBrowser.ads

import com.cc.ads.topon.ExternalInterstitialEligibility
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalInterstitialEligibilityTest {

    @Test
    fun `non attributed user is never eligible`() {
        assertFalse(
            ExternalInterstitialEligibility.canLoadOrShow(
                isAdAttributedUser = false,
                attributionMatchedAtMillis = 0L,
                nowMillis = 10_000L
            )
        )
    }

    @Test
    fun `attributed user without timestamp stays immediately eligible for backward compatibility`() {
        assertTrue(
            ExternalInterstitialEligibility.canLoadOrShow(
                isAdAttributedUser = true,
                attributionMatchedAtMillis = 0L,
                nowMillis = 10_000L
            )
        )
    }

    @Test
    fun `attributed user before three hours is ineligible`() {
        assertFalse(
            ExternalInterstitialEligibility.canLoadOrShow(
                isAdAttributedUser = true,
                attributionMatchedAtMillis = 1_000L,
                nowMillis = 1_000L + ExternalInterstitialEligibility.MIN_DELAY_MILLIS - 1L
            )
        )
    }

    @Test
    fun `attributed user after three hours is eligible`() {
        assertTrue(
            ExternalInterstitialEligibility.canLoadOrShow(
                isAdAttributedUser = true,
                attributionMatchedAtMillis = 1_000L,
                nowMillis = 1_000L + ExternalInterstitialEligibility.MIN_DELAY_MILLIS
            )
        )
    }
}
