package com.myAllVideoBrowser.ads

import com.cc.ads.topon.TopOnAdProfiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TopOnTestModeConfigTest {
    private val gradlePropertiesFile = File("../gradle.properties")
    private val appBuildFile = File("../app/build.gradle.kts")
    private val manifestFile = File("../app/src/main/AndroidManifest.xml")

    @Test
    fun testProfile_usesRequestedIdentifiers() {
        val profile = TopOnAdProfiles.forMode(isTestMode = true)

        assertTrue(profile.isTestMode)
        assertEquals("com.test.topon.app", profile.appPackageName)
        assertEquals("h69e0d96c8e3af", profile.appId)
        assertEquals("a46c81dc0bb003d814103dc5b7abbd70a", profile.appKey)
        assertEquals("ca-app-pub-3940256099942544~3347511713", profile.admobAppId)
        assertEquals("n1h9mmu52n0afu", profile.scenes.homeTopNative)
        assertEquals("n1h9mmu52n0710", profile.scenes.firstInterstitial)
        assertEquals("n1h9mmu52mvnbp", profile.scenes.externalInterstitial)
        assertEquals("n1h9mmu52mvi86", profile.scenes.tabInterstitial)
        assertEquals("n1h9mmu52mvbp4", profile.scenes.languageNative)
        assertEquals("n1h9mmu52mv7pl", profile.scenes.guideNative)
        assertEquals("n1h9mmu52mv3d3", profile.scenes.generalInterstitial)
        assertEquals("n1h9mmu52muv52", profile.scenes.firstSplash)
        assertEquals("n1h9mmu52mun2", profile.scenes.activeSplash)
    }

    @Test
    fun productionProfile_preservesCurrentSceneIdentifiers() {
        val profile = TopOnAdProfiles.forMode(isTestMode = false)

        assertFalse(profile.isTestMode)
        assertEquals("n69db2a97be495", profile.scenes.firstSplash)
        assertEquals("n69db2a9908bc0", profile.scenes.activeSplash)
        assertEquals("n69db2a9c2fd64", profile.scenes.firstInterstitial)
        assertEquals("n69db2a9ce61c8", profile.scenes.generalInterstitial)
        assertEquals("n69db2a9d986e1", profile.scenes.externalInterstitial)
        assertEquals("n69db2a9aa11d7", profile.scenes.guideNative)
        assertEquals("n69db2a99c9b03", profile.scenes.languageNative)
        assertEquals("n69db2a9b611b4", profile.scenes.homeTopNative)
    }

    @Test
    fun buildFiles_defineTestModeSwitchAndManifestMetadata() {
        val gradleProperties = gradlePropertiesFile.readText()
        val appBuild = appBuildFile.readText()
        val manifest = manifestFile.readText()

        assertTrue(gradleProperties.contains("TOPON_TEST_MODE=false"))
        assertTrue(gradleProperties.contains("TEST_TOPON_APP_ID=h69e0d96c8e3af"))
        assertTrue(gradleProperties.contains("TEST_TOPON_APP_KEY=a46c81dc0bb003d814103dc5b7abbd70a"))
        assertTrue(gradleProperties.contains("TEST_TOPON_APP_PACKAGE_NAME=com.test.topon.app"))
        assertTrue(gradleProperties.contains("TEST_ADMOB_APP_ID=ca-app-pub-3940256099942544~3347511713"))

        assertTrue(appBuild.contains("TOPON_TEST_MODE"))
        assertTrue(appBuild.contains("TEST_TOPON_APP_ID"))
        assertTrue(appBuild.contains("TEST_TOPON_APP_PACKAGE_NAME"))
        assertTrue(appBuild.contains("TEST_ADMOB_APP_ID"))
        assertTrue(appBuild.contains("com.android.application"))
        assertTrue(appBuild.contains("applicationId = activeTopOnPackageName"))
        assertTrue(appBuild.contains("toponTestMode"))
        assertTrue(appBuild.contains("toponAppPackageName"))
        assertTrue(appBuild.contains("admobAppId"))

        assertTrue(manifest.contains("com.cc.ads.TOPON_TEST_MODE"))
        assertTrue(manifest.contains("com.cc.ads.TOPON_APP_PACKAGE_NAME"))
        assertTrue(manifest.contains("com.google.android.gms.ads.APPLICATION_ID"))
    }
}
