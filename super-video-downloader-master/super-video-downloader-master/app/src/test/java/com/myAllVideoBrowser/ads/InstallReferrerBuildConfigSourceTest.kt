package com.myAllVideoBrowser.ads

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallReferrerBuildConfigSourceTest {

    @Test
    fun `version catalog and app build file define install referrer dependency`() {
        val libs = File("../gradle/libs.versions.toml").readText()
        val appBuild = File("../app/build.gradle.kts").readText()

        assertTrue(libs.contains("installReferrer"))
        assertTrue(libs.contains("com.android.installreferrer:installreferrer"))
        assertTrue(appBuild.contains("downloaderLibs.installReferrer"))
    }
}
