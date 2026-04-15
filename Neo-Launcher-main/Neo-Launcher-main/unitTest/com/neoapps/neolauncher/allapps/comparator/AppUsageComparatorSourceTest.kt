package com.neoapps.neolauncher.allapps.comparator

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUsageComparatorSourceTest {

    @Test
    fun appUsageComparator_buildsLookupMapInsteadOfScanningListInCompare() {
        val source =
            File("Omega/src/com/neoapps/neolauncher/allapps/comparator/AppUsageComparator.kt")
                .readText()

        assertTrue(source.contains("usageByPackage"))
        assertTrue(source.contains("associate"))
        assertFalse(source.contains("for (i in mApps.indices)"))
    }
}
