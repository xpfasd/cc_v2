package com.cc.ads.topon

data class TopOnSceneIds(
    val firstSplash: String,
    val activeSplash: String,
    val firstInterstitial: String,
    val generalInterstitial: String,
    val externalInterstitial: String,
    val tabInterstitial: String,
    val guideNative: String,
    val languageNative: String,
    val homeTopNative: String
)

data class TopOnAdProfile(
    val isTestMode: Boolean,
    val appPackageName: String,
    val appId: String,
    val appKey: String,
    val admobAppId: String,
    val scenes: TopOnSceneIds
)

object TopOnAdProfiles {
    private val productionProfile = TopOnAdProfile(
        isTestMode = false,
        appPackageName = "com.myAllVideoBrowser",
        appId = "",
        appKey = "",
        admobAppId = "",
        scenes = TopOnSceneIds(
            firstSplash = "n69db2a97be495",
            activeSplash = "n69db2a9908bc0",
            firstInterstitial = "n69db2a9c2fd64",
            generalInterstitial = "n69db2a9ce61c8",
            externalInterstitial = "n69db2a9d986e1",
            tabInterstitial = "n69db2a9ce61c8",
            guideNative = "n69db2a9aa11d7",
            languageNative = "n69db2a99c9b03",
            homeTopNative = "n69db2a9b611b4"
        )
    )

    private val testProfile = TopOnAdProfile(
        isTestMode = true,
        appPackageName = "com.test.topon.app",
        appId = "h69e0d96c8e3af",
        appKey = "a46c81dc0bb003d814103dc5b7abbd70a",
        admobAppId = "ca-app-pub-3940256099942544~3347511713",
        scenes = TopOnSceneIds(
            firstSplash = "n1h9mmu52muv52",
            activeSplash = "n1h9mmu52muv52",//""n1h9mmu52mun2",
            firstInterstitial = "n1h9mmu52n0710",
            generalInterstitial = "n1h9mmu52mv3d3",
            externalInterstitial = "n1h9mmu52mvnbp",
            tabInterstitial = "n1h9mmu52mvi86",
            guideNative = "n1h9mmu52mv7pl",
            languageNative = "n1h9mmu52mvbp4",
            homeTopNative = "n1h9mmu52n0afu"
        )
    )

    fun forMode(isTestMode: Boolean): TopOnAdProfile =
        if (isTestMode) testProfile else productionProfile
}
