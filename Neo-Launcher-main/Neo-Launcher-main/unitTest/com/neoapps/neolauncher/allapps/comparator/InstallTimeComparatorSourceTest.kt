package com.neoapps.neolauncher.allapps.comparator

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallTimeComparatorSourceTest {

    @Test
    fun installTimeComparator_readsFromPrecomputedMapInsteadOfQueryingPackageManager() {
        val source =
            File("Omega/src/com/neoapps/neolauncher/allapps/comparator/InstallTimeComparator.kt")
                .readText()

        assertTrue(source.contains("private val installTimes: Map<String, Long>"))
        assertTrue(source.contains("installTimes[app1.componentName!!.packageName]"))
        assertFalse(source.contains("getPackageInfo"))
        assertFalse(source.contains("PackageManager"))
    }
}
