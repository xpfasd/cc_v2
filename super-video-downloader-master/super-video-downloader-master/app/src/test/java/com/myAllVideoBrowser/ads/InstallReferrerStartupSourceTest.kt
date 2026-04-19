package com.myAllVideoBrowser.ads

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallReferrerStartupSourceTest {

    @Test
    fun `application startup launches install referrer attribution check`() {
        val source = File("src/main/java/com/myAllVideoBrowser/DLApplication.kt").readText()

        assertTrue(source.contains("InstallReferrerAttributionChecker"))
        assertTrue(source.contains("checkAndPersistIfNeeded()"))
    }

    @Test
    fun `shared preferences expose ad attribution and attempt flags`() {
        val source = File("src/main/java/com/myAllVideoBrowser/util/SharedPrefHelper.kt").readText()

        assertTrue(source.contains("IS_AD_ATTRIBUTED_USER"))
        assertTrue(source.contains("HAS_ATTEMPTED_INSTALL_REFERRER_ATTRIBUTION"))
        assertTrue(source.contains("fun getIsAdAttributedUser()"))
        assertTrue(source.contains("fun markAdAttributedUser()"))
        assertTrue(source.contains("fun hasAttemptedInstallReferrerAttribution()"))
        assertTrue(source.contains("fun markInstallReferrerAttributionAttempted()"))
    }
}
