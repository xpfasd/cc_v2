package com.cc.ads.topon

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.view.View
import android.view.ViewGroup
import com.thinkup.banner.api.TUBannerListener
import com.thinkup.banner.api.TUBannerView
import com.thinkup.core.api.AdError
import com.thinkup.core.api.TUAdInfo
import com.thinkup.core.api.TUSDK
import com.thinkup.interstitial.api.TUInterstitial
import com.thinkup.interstitial.api.TUInterstitialListener
import com.thinkup.nativead.api.NativeAd
import com.thinkup.nativead.api.TUNative
import com.thinkup.nativead.api.TUNativeAdView
import com.thinkup.nativead.api.TUNativeEventListener
import com.thinkup.nativead.api.TUNativeNetworkListener
import com.thinkup.nativead.api.TUNativePrepareInfo
import com.thinkup.rewardvideo.api.TURewardVideoAd
import com.thinkup.rewardvideo.api.TURewardVideoListener
import com.thinkup.splashad.api.TUSplashAd
import com.thinkup.splashad.api.TUSplashAdExtraInfo
import com.thinkup.splashad.api.TUSplashAdListener

object TopOnAds {
    @Volatile
    private var initialized = false

    @Volatile
    private var currentConfig: TopOnAdConfig? = null

    val config: TopOnAdConfig?
        get() = currentConfig

    fun initializeFromManifest(
        context: Context,
        enableDebugLog: Boolean = false
    ): TopOnInitResult = initialize(context, TopOnAdConfig.fromManifest(context), enableDebugLog)

    fun initialize(
        context: Context,
        config: TopOnAdConfig,
        enableDebugLog: Boolean = false
    ): TopOnInitResult {
        currentConfig = config
        TopOnAdScenes.setTestMode(config.isTestMode)
        if (initialized) {
            return TopOnInitResult.AlreadyInitialized
        }
        if (!config.isComplete) {
            return TopOnInitResult.MissingConfig
        }
        if (!context.isMainProcess()) {
            return TopOnInitResult.SkippedNonMainProcess
        }

        synchronized(this) {
            if (initialized) {
                return TopOnInitResult.AlreadyInitialized
            }
            TUSDK.setNetworkLogDebug(enableDebugLog)
            TUSDK.init(context.applicationContext, config.appId, config.appKey)
            initialized = true
        }
        return TopOnInitResult.Initialized
    }

    fun interstitial(
        context: Context,
        placementId: String = currentConfig?.placements?.interstitial.orEmpty(),
        callback: TopOnAdCallback = {}
    ): TopOnInterstitialAd = TopOnInterstitialAd(context, placementId, callback)

    fun rewarded(
        context: Context,
        placementId: String = currentConfig?.placements?.rewarded.orEmpty(),
        callback: TopOnAdCallback = {}
    ): TopOnRewardedAd = TopOnRewardedAd(context, placementId, callback)

    fun splash(
        context: Context,
        placementId: String = currentConfig?.placements?.splash.orEmpty(),
        timeoutMillis: Int = TUSplashAd.DEFAULT_SPLASH_TIMEOUT_TIME,
        callback: TopOnAdCallback = {}
    ): TopOnSplashAd = TopOnSplashAd(context, placementId, timeoutMillis, callback)

    fun bannerView(
        context: Context,
        placementId: String = currentConfig?.placements?.banner.orEmpty(),
        callback: TopOnAdCallback = {}
    ): TUBannerView? {
        if (placementId.isBlank()) {
            callback(TopOnAdEvent.Failed(TopOnAdFormat.BANNER, TopOnAdError.missingPlacement(TopOnAdFormat.BANNER)))
            return null
        }
        return TUBannerView(context).apply {
            setPlacementId(placementId)
            setBannerAdListener(object : TUBannerListener {
                override fun onBannerLoaded() {
                    callback(TopOnAdEvent.Loaded(TopOnAdFormat.BANNER))
                }

                override fun onBannerFailed(error: AdError?) {
                    callback(TopOnAdEvent.Failed(TopOnAdFormat.BANNER, TopOnAdError.from(error)))
                }

                override fun onBannerClicked(info: TUAdInfo?) {
                    callback(TopOnAdEvent.Clicked(TopOnAdFormat.BANNER, TopOnAdInfo.from(info)))
                }

                override fun onBannerShow(info: TUAdInfo?) {
                    callback(TopOnAdEvent.Shown(TopOnAdFormat.BANNER, TopOnAdInfo.from(info)))
                }

                override fun onBannerClose(info: TUAdInfo?) {
                    callback(TopOnAdEvent.Closed(TopOnAdFormat.BANNER, TopOnAdInfo.from(info)))
                }

                override fun onBannerAutoRefreshed(info: TUAdInfo?) {
                    callback(TopOnAdEvent.Loaded(TopOnAdFormat.BANNER))
                    callback(TopOnAdEvent.Shown(TopOnAdFormat.BANNER, TopOnAdInfo.from(info)))
                }

                override fun onBannerAutoRefreshFail(error: AdError?) {
                    callback(TopOnAdEvent.Failed(TopOnAdFormat.BANNER, TopOnAdError.from(error)))
                }
            })
        }
    }

    fun native(
        context: Context,
        placementId: String = currentConfig?.placements?.native.orEmpty(),
        callback: TopOnAdCallback = {}
    ): TopOnNativeAdLoader = TopOnNativeAdLoader(context, placementId, callback)

    fun destroyBanner(view: TUBannerView?) {
        view?.destroy()
    }

    private fun Context.isMainProcess(): Boolean {
        val currentProcessName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Application.getProcessNameCompat()
        } else {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val myPid = Process.myPid()
            activityManager?.runningAppProcesses
                ?.firstOrNull { processInfo -> processInfo.pid == myPid }
                ?.processName
        }
        return currentProcessName.isNullOrBlank() || currentProcessName == packageName
    }
}

class TopOnInterstitialAd internal constructor(
    context: Context,
    private val placementId: String,
    callback: TopOnAdCallback
) {
    private var callback: TopOnAdCallback = callback
    private val ad: TUInterstitial? = if (placementId.isBlank()) {
        callback(TopOnAdEvent.Failed(TopOnAdFormat.INTERSTITIAL, TopOnAdError.missingPlacement(TopOnAdFormat.INTERSTITIAL)))
        null
    } else {
        TUInterstitial(context.applicationContext, placementId)
    }

    init {
        ad?.setAdListener(object : TUInterstitialListener {
            override fun onInterstitialAdLoaded() {
                callback(TopOnAdEvent.Loaded(TopOnAdFormat.INTERSTITIAL))
            }

            override fun onInterstitialAdLoadFail(error: AdError?) {
                callback(TopOnAdEvent.Failed(TopOnAdFormat.INTERSTITIAL, TopOnAdError.from(error)))
            }

            override fun onInterstitialAdClicked(info: TUAdInfo?) {
                callback(TopOnAdEvent.Clicked(TopOnAdFormat.INTERSTITIAL, TopOnAdInfo.from(info)))
            }

            override fun onInterstitialAdShow(info: TUAdInfo?) {
                callback(TopOnAdEvent.Shown(TopOnAdFormat.INTERSTITIAL, TopOnAdInfo.from(info)))
            }

            override fun onInterstitialAdClose(info: TUAdInfo?) {
                callback(TopOnAdEvent.Closed(TopOnAdFormat.INTERSTITIAL, TopOnAdInfo.from(info)))
            }

            override fun onInterstitialAdVideoStart(info: TUAdInfo?) {
                callback(TopOnAdEvent.VideoStarted(TopOnAdFormat.INTERSTITIAL, TopOnAdInfo.from(info)))
            }

            override fun onInterstitialAdVideoEnd(info: TUAdInfo?) {
                callback(TopOnAdEvent.VideoEnded(TopOnAdFormat.INTERSTITIAL, TopOnAdInfo.from(info)))
            }

            override fun onInterstitialAdVideoError(error: AdError?) {
                callback(TopOnAdEvent.Failed(TopOnAdFormat.INTERSTITIAL, TopOnAdError.from(error)))
            }
        })
    }

    fun load(callback: TopOnAdCallback? = null) {
        callback?.let { this.callback = it }
        ad?.load()
    }

    fun isReady(): Boolean = ad?.isAdReady == true

    fun show(activity: Activity, scenarioId: String = "", callback: TopOnAdCallback? = null): Boolean {
        callback?.let { this.callback = it }
        val interstitial = ad ?: return false
        if (!interstitial.isAdReady) {
            this.callback(TopOnAdEvent.Failed(TopOnAdFormat.INTERSTITIAL, TopOnAdError.notReady(TopOnAdFormat.INTERSTITIAL)))
            return false
        }
        if (scenarioId.isBlank()) {
            interstitial.show(activity)
        } else {
            interstitial.show(activity, scenarioId)
        }
        return true
    }
}

class TopOnRewardedAd internal constructor(
    context: Context,
    private val placementId: String,
    callback: TopOnAdCallback
) {
    private var callback: TopOnAdCallback = callback
    private val ad: TURewardVideoAd? = if (placementId.isBlank()) {
        callback(TopOnAdEvent.Failed(TopOnAdFormat.REWARDED, TopOnAdError.missingPlacement(TopOnAdFormat.REWARDED)))
        null
    } else {
        TURewardVideoAd(context.applicationContext, placementId)
    }

    init {
        ad?.setAdListener(object : TURewardVideoListener {
            override fun onRewardedVideoAdLoaded() {
                callback(TopOnAdEvent.Loaded(TopOnAdFormat.REWARDED))
            }

            override fun onRewardedVideoAdFailed(error: AdError?) {
                callback(TopOnAdEvent.Failed(TopOnAdFormat.REWARDED, TopOnAdError.from(error)))
            }

            override fun onRewardedVideoAdPlayStart(info: TUAdInfo?) {
                callback(TopOnAdEvent.VideoStarted(TopOnAdFormat.REWARDED, TopOnAdInfo.from(info)))
            }

            override fun onRewardedVideoAdPlayEnd(info: TUAdInfo?) {
                callback(TopOnAdEvent.VideoEnded(TopOnAdFormat.REWARDED, TopOnAdInfo.from(info)))
            }

            override fun onRewardedVideoAdPlayFailed(error: AdError?, info: TUAdInfo?) {
                callback(TopOnAdEvent.Failed(TopOnAdFormat.REWARDED, TopOnAdError.from(error)))
            }

            override fun onRewardedVideoAdClosed(info: TUAdInfo?) {
                callback(TopOnAdEvent.Closed(TopOnAdFormat.REWARDED, TopOnAdInfo.from(info)))
            }

            override fun onRewardedVideoAdPlayClicked(info: TUAdInfo?) {
                callback(TopOnAdEvent.Clicked(TopOnAdFormat.REWARDED, TopOnAdInfo.from(info)))
            }

            override fun onReward(info: TUAdInfo?) {
                callback(TopOnAdEvent.Rewarded(TopOnAdInfo.from(info)))
            }
        })
    }

    fun load(callback: TopOnAdCallback? = null) {
        callback?.let { this.callback = it }
        ad?.load()
    }

    fun isReady(): Boolean = ad?.isAdReady == true

    fun show(activity: Activity, scenarioId: String = "", callback: TopOnAdCallback? = null): Boolean {
        callback?.let { this.callback = it }
        val rewarded = ad ?: return false
        if (!rewarded.isAdReady) {
            this.callback(TopOnAdEvent.Failed(TopOnAdFormat.REWARDED, TopOnAdError.notReady(TopOnAdFormat.REWARDED)))
            return false
        }
        if (scenarioId.isBlank()) {
            rewarded.show(activity)
        } else {
            rewarded.show(activity, scenarioId)
        }
        return true
    }
}

class TopOnSplashAd internal constructor(
    context: Context,
    private val placementId: String,
    timeoutMillis: Int,
    callback: TopOnAdCallback
) {
    private var callback: TopOnAdCallback = callback
    private val ad: TUSplashAd? = if (placementId.isBlank()) {
        callback(TopOnAdEvent.Failed(TopOnAdFormat.SPLASH, TopOnAdError.missingPlacement(TopOnAdFormat.SPLASH)))
        null
    } else {
        TUSplashAd(context.applicationContext, placementId, listener(), timeoutMillis)
    }

    fun load(callback: TopOnAdCallback? = null) {
        callback?.let { this.callback = it }
        ad?.loadAd()
    }

    fun isReady(): Boolean = ad?.isAdReady == true

    fun show(
        activity: Activity,
        container: ViewGroup,
        scenarioId: String = "",
        callback: TopOnAdCallback? = null
    ): Boolean {
        callback?.let { this.callback = it }
        val splash = ad ?: return false
        if (!splash.isAdReady) {
            this.callback(TopOnAdEvent.Failed(TopOnAdFormat.SPLASH, TopOnAdError.notReady(TopOnAdFormat.SPLASH)))
            return false
        }
        if (scenarioId.isBlank()) {
            splash.show(activity, container)
        } else {
            splash.show(activity, container, scenarioId)
        }
        return true
    }

    fun destroy() {
        ad?.onDestory()
    }

    private fun listener(): TUSplashAdListener = object : TUSplashAdListener {
        override fun onAdLoaded(isTimeout: Boolean) {
            callback(TopOnAdEvent.Loaded(TopOnAdFormat.SPLASH))
        }

        override fun onAdLoadTimeout() {
            callback(TopOnAdEvent.SplashTimeout)
        }

        override fun onNoAdError(error: AdError?) {
            callback(TopOnAdEvent.Failed(TopOnAdFormat.SPLASH, TopOnAdError.from(error)))
        }

        override fun onAdShow(info: TUAdInfo?) {
            callback(TopOnAdEvent.Shown(TopOnAdFormat.SPLASH, TopOnAdInfo.from(info)))
        }

        override fun onAdClick(info: TUAdInfo?) {
            callback(TopOnAdEvent.Clicked(TopOnAdFormat.SPLASH, TopOnAdInfo.from(info)))
        }

        override fun onAdDismiss(info: TUAdInfo?, extraInfo: TUSplashAdExtraInfo?) {
            callback(TopOnAdEvent.Closed(TopOnAdFormat.SPLASH, TopOnAdInfo.from(info)))
        }
    }
}

class TopOnNativeAdLoader internal constructor(
    context: Context,
    private val placementId: String,
    callback: TopOnAdCallback
) {
    private var callback: TopOnAdCallback = callback
    private val native: TUNative? = if (placementId.isBlank()) {
        callback(TopOnAdEvent.Failed(TopOnAdFormat.NATIVE, TopOnAdError.missingPlacement(TopOnAdFormat.NATIVE)))
        null
    } else {
        TUNative(context.applicationContext, placementId, object : TUNativeNetworkListener {
            override fun onNativeAdLoaded() {
                callback(TopOnAdEvent.Loaded(TopOnAdFormat.NATIVE))
            }

            override fun onNativeAdLoadFail(error: AdError?) {
                callback(TopOnAdEvent.Failed(TopOnAdFormat.NATIVE, TopOnAdError.from(error)))
            }
        })
    }

    fun load(callback: TopOnAdCallback? = null) {
        callback?.let { this.callback = it }
        native?.makeAdRequest()
    }

    fun takeLoadedAd(callback: TopOnAdCallback? = null): TopOnNativeAdHandle? {
        callback?.let { this.callback = it }
        return native?.getNativeAd()?.let { TopOnNativeAdHandle(it, this.callback) }
    }
}

class TopOnNativeAdHandle internal constructor(
    private val nativeAd: NativeAd,
    private val callback: TopOnAdCallback
) {
    val isValid: Boolean
        get() = nativeAd.isValid

    fun render(
        adView: TUNativeAdView,
        contentView: View,
        prepareInfo: TUNativePrepareInfo? = null
    ) {
        nativeAd.renderAdContainer(adView, contentView)
        prepareInfo?.let { nativeAd.prepare(adView, it) }
        nativeAd.setNativeEventListener(object : TUNativeEventListener {
            override fun onAdImpressed(view: TUNativeAdView?, info: TUAdInfo?) {
                callback(TopOnAdEvent.Shown(TopOnAdFormat.NATIVE, TopOnAdInfo.from(info)))
            }

            override fun onAdClicked(view: TUNativeAdView?, info: TUAdInfo?) {
                callback(TopOnAdEvent.Clicked(TopOnAdFormat.NATIVE, TopOnAdInfo.from(info)))
            }

            override fun onAdVideoStart(view: TUNativeAdView?) {
                callback(TopOnAdEvent.VideoStarted(TopOnAdFormat.NATIVE, nativeAd.adInfo?.let { TopOnAdInfo.from(it) }))
            }

            override fun onAdVideoEnd(view: TUNativeAdView?) {
                callback(TopOnAdEvent.VideoEnded(TopOnAdFormat.NATIVE, nativeAd.adInfo?.let { TopOnAdInfo.from(it) }))
            }

            override fun onAdVideoProgress(view: TUNativeAdView?, progress: Int) = Unit
        })
    }

    fun renderExpress(adView: TUNativeAdView): Boolean {
        val adObject = nativeAd.`object`
        val adContentView = adObject as? View ?: return false
        render(adView, adContentView)
        return true
    }

    fun clear(adView: TUNativeAdView) {
        nativeAd.clear(adView)
    }

    fun destroy() {
        nativeAd.destory()
    }
}

private object Application {
    fun getProcessNameCompat(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            android.app.Application.getProcessName()
        } else {
            null
        }
}
