package com.android.launcher3.model

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class AllAppsListSourceTest {

    @Test
    fun allAppsList_tracksExistingAppsInDedicatedMembershipSet() {
        val source = File("src/com/android/launcher3/model/AllAppsList.java").readText()

        assertTrue(source.contains("private final Set<ComponentKey> mExistingApps = new HashSet<>();"))
        assertTrue(source.contains("mExistingApps.contains"))
        assertTrue(source.contains("mExistingApps.add"))
        assertTrue(source.contains("mExistingApps.clear()"))
        assertTrue(source.contains("mAppFilter.clearCache()"))
    }
}
