package com.myAllVideoBrowser.ui.main.home

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class LaunchSplashDelaySourceTest {

    @Test
    fun `main activity keeps splash delay at ten seconds`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        assertTrue(source.contains("const val SPLASH_DELAY_MS = 10000L"))
    }
}
