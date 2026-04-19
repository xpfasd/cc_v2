package com.myAllVideoBrowser.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallReferrerAttributionDeciderTest {

    @Test
    fun `isAdAttributed returns true when referrer contains gclid`() {
        assertTrue(
            InstallReferrerAttributionDecider.isAdAttributed("utm_source=google-play&gclid=test-gclid")
        )
    }

    @Test
    fun `isAdAttributed returns true when referrer contains gbraid`() {
        assertTrue(InstallReferrerAttributionDecider.isAdAttributed("gbraid=test-gbraid"))
    }

    @Test
    fun `isAdAttributed returns true when referrer contains wbraid`() {
        assertTrue(InstallReferrerAttributionDecider.isAdAttributed("wbraid=test-wbraid"))
    }

    @Test
    fun `isAdAttributed returns true when referrer contains fbclid`() {
        assertTrue(InstallReferrerAttributionDecider.isAdAttributed("fbclid=test-fbclid"))
    }

    @Test
    fun `isAdAttributed returns true when referrer contains ttclid`() {
        assertTrue(InstallReferrerAttributionDecider.isAdAttributed("ttclid=test-ttclid"))
    }

    @Test
    fun `isAdAttributed returns false for blank tracked values`() {
        assertFalse(InstallReferrerAttributionDecider.isAdAttributed("gclid=&fbclid="))
    }

    @Test
    fun `isAdAttributed returns false when no tracked parameters exist`() {
        assertFalse(
            InstallReferrerAttributionDecider.isAdAttributed("utm_source=organic&utm_campaign=spring")
        )
    }

    @Test
    fun `isAdAttributed returns false for null referrer`() {
        assertFalse(InstallReferrerAttributionDecider.isAdAttributed(null))
    }
}
