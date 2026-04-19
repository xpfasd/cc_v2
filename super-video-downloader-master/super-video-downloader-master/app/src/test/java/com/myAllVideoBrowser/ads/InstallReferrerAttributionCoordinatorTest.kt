package com.myAllVideoBrowser.ads

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallReferrerAttributionCoordinatorTest {

    @Test
    fun `recordReferrer marks attempt and stores true when referrer matches tracked params`() {
        val store = RecordingAttributionStore()

        InstallReferrerAttributionCoordinator(store) { 1234L }
            .recordReferrer("utm_source=x&fbclid=fb-value")

        assertTrue(store.attempted)
        assertTrue(store.adAttributed)
        assertEquals(1234L, store.matchedAtMillis)
    }

    @Test
    fun `recordReferrer marks attempt and keeps false when referrer does not match`() {
        val store = RecordingAttributionStore()

        InstallReferrerAttributionCoordinator(store) { 1234L }.recordReferrer("utm_source=organic")

        assertTrue(store.attempted)
        assertFalse(store.adAttributed)
        assertEquals(0L, store.matchedAtMillis)
    }

    @Test
    fun `recordReferrer does not downgrade already attributed user`() {
        val store = RecordingAttributionStore(adAttributed = true, matchedAtMillis = 100L)

        InstallReferrerAttributionCoordinator(store) { 1234L }.recordReferrer("utm_source=organic")

        assertTrue(store.adAttributed)
        assertEquals(0, store.markAttributedTrueCalls)
        assertFalse(store.attempted)
        assertEquals(100L, store.matchedAtMillis)
    }

    private class RecordingAttributionStore(
        var adAttributed: Boolean = false,
        var attempted: Boolean = false,
        var matchedAtMillis: Long = 0L
    ) : InstallReferrerAttributionStore {
        var markAttributedTrueCalls: Int = 0

        override fun isAdAttributedUser(): Boolean = adAttributed

        override fun hasAttemptedInstallReferrerAttribution(): Boolean = attempted

        override fun getAdAttributionMatchedAtMillis(): Long = matchedAtMillis

        override fun markInstallReferrerAttempted() {
            attempted = true
        }

        override fun markAdAttributedUser() {
            adAttributed = true
            markAttributedTrueCalls += 1
        }

        override fun setAdAttributionMatchedAtMillis(matchedAtMillis: Long) {
            this.matchedAtMillis = matchedAtMillis
        }
    }
}
