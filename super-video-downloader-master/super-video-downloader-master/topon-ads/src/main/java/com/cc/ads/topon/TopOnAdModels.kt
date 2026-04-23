package com.cc.ads.topon

import com.thinkup.core.api.AdError
import com.thinkup.core.api.TUAdInfo

enum class TopOnAdFormat {
    SPLASH,
    INTERSTITIAL,
    REWARDED,
    BANNER,
    NATIVE
}

sealed class TopOnInitResult {
    data object Initialized : TopOnInitResult()
    data object AlreadyInitialized : TopOnInitResult()
    data object MissingConfig : TopOnInitResult()
    data object SkippedNonMainProcess : TopOnInitResult()
}

data class TopOnAdError(
    val code: String,
    val message: String,
    val platformCode: String = "",
    val platformMessage: String = ""
) {
    companion object {
        fun from(error: AdError?): TopOnAdError =
            TopOnAdError(
                code = error?.code.orEmpty(),
                message = error?.desc ?: error?.toString().orEmpty(),
                platformCode = error?.platformCode.orEmpty(),
                platformMessage = error?.platformMSG.orEmpty()
            )

        fun missingPlacement(format: TopOnAdFormat): TopOnAdError =
            TopOnAdError(
                code = "missing_placement",
                message = "TopOn ${format.name.lowercase()} placement id is empty."
            )

        fun notReady(format: TopOnAdFormat): TopOnAdError =
            TopOnAdError(
                code = "ad_not_ready",
                message = "TopOn ${format.name.lowercase()} ad is not ready."
            )
    }
}

data class TopOnAdInfo(
    val placementId: String,
    val format: String,
    val networkName: String,
    val networkFirmId: String,
    val networkPlacementId: String,
    val scenarioId: String,
    val ecpm: Double,
    val currency: String,
    val country: String,
    val publisherRevenue: Double
) {
    companion object {
        fun from(info: TUAdInfo?): TopOnAdInfo? {
            if (info == null) {
                return null
            }
            return TopOnAdInfo(
                placementId = info.placementId.orEmpty(),
                format = info.format.orEmpty(),
                networkName = info.networkName.orEmpty(),
                networkFirmId = info.networkFirmId.toString(),
                networkPlacementId = info.networkPlacementId.orEmpty(),
                scenarioId = info.scenarioId.orEmpty(),
                ecpm = info.ecpm,
                currency = info.currency.orEmpty(),
                country = info.country.orEmpty(),
                publisherRevenue = info.publisherRevenue ?: 0.0
            )
        }
    }
}

sealed class TopOnAdEvent {
    data class Loaded(val format: TopOnAdFormat) : TopOnAdEvent()
    data class Failed(val format: TopOnAdFormat, val error: TopOnAdError) : TopOnAdEvent()
    data class Shown(val format: TopOnAdFormat, val info: TopOnAdInfo?) : TopOnAdEvent()
    data class RevenuePaid(val format: TopOnAdFormat, val info: TopOnAdInfo?) : TopOnAdEvent()
    data class Clicked(val format: TopOnAdFormat, val info: TopOnAdInfo?) : TopOnAdEvent()
    data class Closed(val format: TopOnAdFormat, val info: TopOnAdInfo?) : TopOnAdEvent()
    data class VideoStarted(val format: TopOnAdFormat, val info: TopOnAdInfo?) : TopOnAdEvent()
    data class VideoEnded(val format: TopOnAdFormat, val info: TopOnAdInfo?) : TopOnAdEvent()
    data class Rewarded(val info: TopOnAdInfo?) : TopOnAdEvent()
    data object SplashTimeout : TopOnAdEvent()
}

typealias TopOnAdCallback = (TopOnAdEvent) -> Unit
