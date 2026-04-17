package com.cc.ads.topon

object TopOnAdScenes {
    @Volatile
    private var currentProfile = TopOnAdProfiles.forMode(isTestMode = false)

    val FIRST_SPLASH: String
        get() = currentProfile.scenes.firstSplash

    val ACTIVE_SPLASH: String
        get() = currentProfile.scenes.activeSplash

    val FIRST_INTERSTITIAL: String
        get() = currentProfile.scenes.firstInterstitial

    val GENERAL_INTERSTITIAL: String
        get() = currentProfile.scenes.generalInterstitial

    val LAUNCHER_APP_INTERSTITIAL: String
        get() = currentProfile.scenes.externalInterstitial

    val EXTERNAL_INTERSTITIAL: String
        get() = currentProfile.scenes.externalInterstitial

    val TAB_INTERSTITIAL: String
        get() = currentProfile.scenes.tabInterstitial

    val GUIDE_NATIVE: String
        get() = currentProfile.scenes.guideNative

    val LANGUAGE_NATIVE: String
        get() = currentProfile.scenes.languageNative

    val HOME_TOP_NATIVE: String
        get() = currentProfile.scenes.homeTopNative

    const val SCENARIO_FIRST_SPLASH = "first_splash"
    const val SCENARIO_ACTIVE_SPLASH = "active_splash"
    const val SCENARIO_FIRST_INTERSTITIAL = "first_interstitial"
    const val SCENARIO_GENERAL_INTERSTITIAL = "general_interstitial"
    const val SCENARIO_LAUNCHER_APP_INTERSTITIAL = "launcher_app_interstitial"
    const val SCENARIO_GUIDE_NATIVE = "guide_native"
    const val SCENARIO_LANGUAGE_NATIVE = "language_native"
    const val SCENARIO_HOME_TOP_NATIVE = "home_top_native"

    internal fun setTestMode(isTestMode: Boolean) {
        currentProfile = TopOnAdProfiles.forMode(isTestMode)
    }
}
