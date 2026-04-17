package com.myAllVideoBrowser.ads

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TopOnAdScenesSourceTest {
    private val profilesFile = File("../topon-ads/src/main/java/com/cc/ads/topon/TopOnAdProfiles.kt")
    private val mainActivityFile =
        File("src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt")

    @Test
    fun topOnAdProfiles_declareProductionAndTestPlacementIds() {
        val source = profilesFile.readText()

        listOf(
            "n69db2a97be495",
            "n69db2a9908bc0",
            "n69db2a9c2fd64",
            "n69db2a9ce61c8",
            "n69db2a9d986e1",
            "n69db2a9aa11d7",
            "n69db2a99c9b03",
            "n69db2a9b611b4",
            "n1h9mmu52n0afu",
            "n1h9mmu52n0710",
            "n1h9mmu52mvnbp",
            "n1h9mmu52mvi86",
            "n1h9mmu52mvbp4",
            "n1h9mmu52mv7pl",
            "n1h9mmu52mv3d3",
            "n1h9mmu52muv52",
            "n1h9mmu52mun2"
        ).forEach { placementId ->
            assertTrue("Missing placement id $placementId", source.contains(placementId))
        }
    }

    @Test
    fun guideNativeAd_doesNotWaitForLateNativeLoad() {
        val source = mainActivityFile.readText()

        assertTrue(
            "Guide full-screen native ad should be skipped when it is not already loaded",
            source.contains("renderWhenLoaded = false")
        )
    }
}
