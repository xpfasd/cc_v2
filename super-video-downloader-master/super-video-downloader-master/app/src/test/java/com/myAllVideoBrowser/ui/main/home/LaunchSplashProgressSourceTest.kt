package com.myAllVideoBrowser.ui.main.home

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class LaunchSplashProgressSourceTest {

    @Test
    fun `main activity animates splash progress indicator`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        assertTrue(source.contains("private lateinit var launchSplashProgressIndicator"))
        assertTrue(source.contains("ValueAnimator.ofInt(0, SPLASH_PROGRESS_MAX)"))
        assertTrue(source.contains("launchSplashProgressIndicator.setProgressCompat"))
    }
}
