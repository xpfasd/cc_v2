package com.myAllVideoBrowser.ads

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TopOnAdScenesSourceTest {
    private val scenesFile = File("../topon-ads/src/main/java/com/cc/ads/topon/TopOnAdScenes.kt")

    @Test
    fun topOnAdScenes_declaresAllRequestedPlacementIds() {
        val source = scenesFile.readText()

        listOf(
            "n69db2a97be495",
            "n69db2a9908bc0",
            "n69db2a9c2fd64",
            "n69db2a9ce61c8",
            "n69db2a9d986e1",
            "n69db2a9aa11d7",
            "n69db2a99c9b03",
            "n69db2a9b611b4"
        ).forEach { placementId ->
            assertTrue("Missing placement id $placementId", source.contains(placementId))
        }
    }
}
