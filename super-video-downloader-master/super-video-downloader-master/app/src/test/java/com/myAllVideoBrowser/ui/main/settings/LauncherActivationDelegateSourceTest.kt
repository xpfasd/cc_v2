package com.myAllVideoBrowser.ui.main.settings

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherActivationDelegateSourceTest {

    @Test
    fun `android delegate uses role manager and home settings fallback`() {
        val source = File("src/main/java/com/myAllVideoBrowser/ui/main/settings/LauncherActivationCoordinator.kt").readText()

        assertTrue(source.contains("RoleManager.ROLE_HOME"))
        assertTrue(source.contains("createRequestRoleIntent(RoleManager.ROLE_HOME)"))
        assertTrue(source.contains("Settings.ACTION_HOME_SETTINGS"))
        assertFalse(source.contains("setComponentEnabledSetting"))
        assertFalse(source.contains("Intent.CATEGORY_HOME"))
        assertFalse(source.contains("FakeLauncher"))
    }
}
