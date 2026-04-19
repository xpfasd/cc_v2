package com.myAllVideoBrowser.ads

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherExternalInterstitialExclusionSourceTest {
    private val launcherFile =
        File("../../../Neo-Launcher-main/Neo-Launcher-main/src/com/android/launcher3/Launcher.java")

    @Test
    fun launcherInterstitialFlow_excludesKnownAppsAndGooglePrefixPackages() {
        val source = launcherFile.readText()

        assertTrue(source.contains("com.tencent.mm"))
        assertTrue(source.contains("com.ss.android.ugc.aweme"))
        assertTrue(source.contains("com.instagram.android"))
        assertTrue(source.contains("com.zhiliaoapp.musically"))
        assertTrue(source.contains("com.facebook.katana"))
        assertTrue(source.contains("com.android.chrome"))
        assertTrue(source.contains("com.google.android.gm"))
        assertTrue(source.contains("com.google.android.apps.maps"))
        assertTrue(source.contains("com.google.android.youtube"))
        assertTrue(source.contains("com.google.android.apps.photos"))
        assertTrue(source.contains("com.google.android.apps.docs"))
        assertTrue(source.contains("targetPackage.startsWith(\"com.google.android.\")"))
    }
}
