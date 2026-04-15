package com.android.launcher3.allapps

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class AlphabeticalAppsListSourceTest {

    @Test
    fun alphabeticalAppsList_precomputesInstallTimeSortKeysBeforeSorting() {
        val source = File("src/com/android/launcher3/allapps/AlphabeticalAppsList.java").readText()

        assertTrue(source.contains("Config.SORT_BY_INSTALL_DATE"))
        assertTrue(source.contains("collectInstallTimes"))
        assertTrue(source.contains("getAllAppsComparator("))
    }
}
