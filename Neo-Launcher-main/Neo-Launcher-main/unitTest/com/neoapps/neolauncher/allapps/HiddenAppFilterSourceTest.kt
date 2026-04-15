package com.neoapps.neolauncher.allapps

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HiddenAppFilterSourceTest {

    @Test
    fun hiddenAppFilter_cachesHiddenAppsAndExposesCacheReset() {
        val source =
            File("Omega/src/com/neoapps/neolauncher/allapps/HiddenAppFilter.kt").readText()

        assertTrue(source.contains("hiddenAppsCache"))
        assertTrue(source.contains("fun clearCache()"))
        assertTrue(source.contains("private fun hiddenApps(): Set<String>"))
        assertFalse(source.contains("HashSet(NeoPrefs.getInstance().drawerHiddenAppSet.getValue())"))
    }
}
