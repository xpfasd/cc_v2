package com.myAllVideoBrowser.ui.main.home

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class MainActivityLauncherAuthorizationSourceTest {

    @Test
    fun `main activity registers launcher role callback and passes it to launcher selection`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        assertTrue(source.contains("registerForActivityResult(ActivityResultContracts.StartActivityForResult())"))
        assertTrue(source.contains("requestLauncherSelection(this, requestHomeRoleLauncher)"))
        assertTrue(source.contains("result.resultCode == RESULT_OK && isAppDefaultHome()"))
    }

    @Test
    fun `launcher role success reopens main activity instead of leaving user on launcher desktop`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt").readText()

        assertTrue(source.contains("Intent(this, MainActivity::class.java)"))
        assertTrue(source.contains("putExtra(EXTRA_SKIP_LAUNCH_SPLASH, true)"))
        assertTrue(source.contains("Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP"))
        assertTrue(source.contains("if (granted) {"))
    }
}
