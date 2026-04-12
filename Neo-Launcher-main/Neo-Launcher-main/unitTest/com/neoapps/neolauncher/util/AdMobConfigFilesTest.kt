package com.neoapps.neolauncher.util

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AdMobConfigFilesTest {

    private val omegaManifest = File("Omega/AndroidManifest.xml")
    private val admobConfig = File("Omega/res/values/admob.xml")
    private val neoApp = File("Omega/src/com/neoapps/neolauncher/NeoApp.kt")

    @Test
    fun omegaManifest_declaresAdMobApplicationIdMetadata() {
        val manifest = omegaManifest.readText()

        assertTrue(manifest.contains("com.google.android.gms.ads.APPLICATION_ID"))
        assertTrue(manifest.contains("@string/admob_app_id"))
    }

    @Test
    fun admobConfig_usesConfiguredAppId() {
        assertTrue("Expected AdMob config file to exist", admobConfig.exists())
        assertTrue(
            admobConfig.readText().contains("ca-app-pub-3777590112281158~1785263760")
        )
    }

    @Test
    fun neoApp_initializesMobileAdsSdk() {
        assertTrue(
            neoApp.readText().contains("MobileAds.initialize(this)")
        )
    }
}
