package com.cc.ads.topon

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Process
import android.text.TextUtils
import android.util.TypedValue
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
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
    companion object {
        private const val TAG = "TopOnInterstitialAd"
    }

    private var callback: TopOnAdCallback = callback
    private var callbackDebugOwner: String = "constructor"
    private var callbackDebugVersion: Int = 0
    private val ad: TUInterstitial? = if (placementId.isBlank()) {
        callback(TopOnAdEvent.Failed(TopOnAdFormat.INTERSTITIAL, TopOnAdError.missingPlacement(TopOnAdFormat.INTERSTITIAL)))
        null
    } else {
        TUInterstitial(context.applicationContext, placementId)
    }

    init {
        ad?.setAdListener(object : TUInterstitialListener {
            override fun onInterstitialAdLoaded() {
                Log.d(TAG, "TopOn launch interstitial loaded: placementId=$placementId, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
                dispatchCallback(TopOnAdEvent.Loaded(TopOnAdFormat.INTERSTITIAL))
            }

            override fun onInterstitialAdLoadFail(error: AdError?) {
                Log.d(TAG, "TopOn launch interstitial load fail: placementId=$placementId, error=$error, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
                dispatchCallback(TopOnAdEvent.Failed(TopOnAdFormat.INTERSTITIAL, TopOnAdError.from(error)))
            }

            override fun onInterstitialAdClicked(info: TUAdInfo?) {
                Log.d(TAG, "TopOn launch interstitial clicked: placementId=$placementId, info=$info, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
                dispatchCallback(TopOnAdEvent.Clicked(TopOnAdFormat.INTERSTITIAL, TopOnAdInfo.from(info)))
            }

            override fun onInterstitialAdShow(info: TUAdInfo?) {
                Log.d(TAG, "TopOn launch interstitial shown: placementId=$placementId, info=$info, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
                dispatchCallback(TopOnAdEvent.Shown(TopOnAdFormat.INTERSTITIAL, TopOnAdInfo.from(info)))
            }

            override fun onInterstitialAdClose(info: TUAdInfo?) {
                Log.d(TAG, "TopOn launch interstitial closed: placementId=$placementId, info=$info, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
                dispatchCallback(TopOnAdEvent.Closed(TopOnAdFormat.INTERSTITIAL, TopOnAdInfo.from(info)))
            }

            override fun onInterstitialAdVideoStart(info: TUAdInfo?) {
                Log.d(TAG, "TopOn launch interstitial video start: placementId=$placementId, info=$info, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
                dispatchCallback(TopOnAdEvent.VideoStarted(TopOnAdFormat.INTERSTITIAL, TopOnAdInfo.from(info)))
            }

            override fun onInterstitialAdVideoEnd(info: TUAdInfo?) {
                Log.d(TAG, "TopOn launch interstitial video end: placementId=$placementId, info=$info, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
                dispatchCallback(TopOnAdEvent.VideoEnded(TopOnAdFormat.INTERSTITIAL, TopOnAdInfo.from(info)))
            }

            override fun onInterstitialAdVideoError(error: AdError?) {
                Log.d(TAG, "TopOn launch interstitial video error: placementId=$placementId, error=$error, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
                dispatchCallback(TopOnAdEvent.Failed(TopOnAdFormat.INTERSTITIAL, TopOnAdError.from(error)))
            }
        })
    }

    fun load(callback: TopOnAdCallback? = null) {
        callback?.let { updateCallback(it, "load") }
        if (callback == null) {
            Log.d(TAG, "TopOn SDK interstitial load using existing callback: placementId=$placementId, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
        }
        ad?.load()
    }

    fun isReady(): Boolean = ad?.isAdReady == true

    fun show(activity: Activity, scenarioId: String = "", callback: TopOnAdCallback? = null): Boolean {
        callback?.let { updateCallback(it, "show") }
        val interstitial = ad ?: return false
        if (!interstitial.isAdReady) {
            this.callback(TopOnAdEvent.Failed(TopOnAdFormat.INTERSTITIAL, TopOnAdError.notReady(TopOnAdFormat.INTERSTITIAL)))
            return false
        }
        Log.d(TAG, "TopOn SDK interstitial show call: placementId=$placementId, scenarioId=$scenarioId, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
        if (scenarioId.isBlank()) {
            interstitial.show(activity)
        } else {
            interstitial.show(activity, scenarioId)
        }
        return true
    }

    private fun updateCallback(callback: TopOnAdCallback, owner: String) {
        this.callback = callback
        callbackDebugOwner = owner
        callbackDebugVersion += 1
        Log.d(TAG, "TopOn SDK interstitial callback updated: placementId=$placementId, owner=$owner, callbackVersion=$callbackDebugVersion")
    }

    private fun dispatchCallback(event: TopOnAdEvent) {
        callback(event)
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
    companion object {
        private const val TAG = "TopOnSplashAd"
    }

    private var callback: TopOnAdCallback = callback
    private var callbackDebugOwner: String = "constructor"
    private var callbackDebugVersion: Int = 0
    private val ad: TUSplashAd? = if (placementId.isBlank()) {
        callback(TopOnAdEvent.Failed(TopOnAdFormat.SPLASH, TopOnAdError.missingPlacement(TopOnAdFormat.SPLASH)))
        null
    } else {
        TUSplashAd(context.applicationContext, placementId, listener(), timeoutMillis)
    }

    fun load(callback: TopOnAdCallback? = null) {
        callback?.let { updateCallback(it, "load") }
        if (callback == null) {
            Log.d(TAG, "TopOn splash load using existing callback: placementId=$placementId, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
        }
        ad?.loadAd()
    }

    fun isReady(): Boolean = ad?.isAdReady == true

    fun show(
        activity: Activity,
        container: ViewGroup,
        scenarioId: String = "",
        callback: TopOnAdCallback? = null
    ): Boolean {
        callback?.let { updateCallback(it, "show") }
        val splash = ad ?: return false
        if (!splash.isAdReady) {
            dispatchCallback(TopOnAdEvent.Failed(TopOnAdFormat.SPLASH, TopOnAdError.notReady(TopOnAdFormat.SPLASH)))
            return false
        }
        Log.d(TAG, "TopOn splash show call: placementId=$placementId, scenarioId=$scenarioId, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
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
            Log.d(TAG, "TopOn splash loaded: placementId=$placementId, isTimeout=$isTimeout, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
            dispatchCallback(TopOnAdEvent.Loaded(TopOnAdFormat.SPLASH))
        }

        override fun onAdLoadTimeout() {
            Log.d(TAG, "TopOn splash load timeout: placementId=$placementId, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
            dispatchCallback(TopOnAdEvent.SplashTimeout)
        }

        override fun onNoAdError(error: AdError?) {
            Log.d(TAG, "TopOn splash no ad error: placementId=$placementId, error=$error, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
            dispatchCallback(TopOnAdEvent.Failed(TopOnAdFormat.SPLASH, TopOnAdError.from(error)))
        }

        override fun onAdShow(info: TUAdInfo?) {
            Log.d(TAG, "TopOn splash shown: placementId=$placementId, info=$info, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
            dispatchCallback(TopOnAdEvent.Shown(TopOnAdFormat.SPLASH, TopOnAdInfo.from(info)))
        }

        override fun onAdClick(info: TUAdInfo?) {
            Log.d(TAG, "TopOn splash clicked: placementId=$placementId, info=$info, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
            dispatchCallback(TopOnAdEvent.Clicked(TopOnAdFormat.SPLASH, TopOnAdInfo.from(info)))
        }

        override fun onAdDismiss(info: TUAdInfo?, extraInfo: TUSplashAdExtraInfo?) {
            Log.d(TAG, "TopOn splash dismissed: placementId=$placementId, info=$info, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
            dispatchCallback(TopOnAdEvent.Closed(TopOnAdFormat.SPLASH, TopOnAdInfo.from(info)))
        }
    }

    private fun updateCallback(callback: TopOnAdCallback, owner: String) {
        this.callback = callback
        callbackDebugOwner = owner
        callbackDebugVersion += 1
        Log.d(TAG, "TopOn splash callback updated: placementId=$placementId, owner=$owner, callbackVersion=$callbackDebugVersion")
    }

    private fun dispatchCallback(event: TopOnAdEvent) {
        callback(event)
    }
}

class TopOnNativeAdLoader internal constructor(
    context: Context,
    private val placementId: String,
    callback: TopOnAdCallback
) {
    companion object {
        private const val TAG = "TopOnNativeAdLoader"
    }

    private var callback: TopOnAdCallback = callback
    private var callbackDebugOwner: String = "constructor"
    private var callbackDebugVersion: Int = 0
    private val native: TUNative? = if (placementId.isBlank()) {
        callback(TopOnAdEvent.Failed(TopOnAdFormat.NATIVE, TopOnAdError.missingPlacement(TopOnAdFormat.NATIVE)))
        null
    } else {
        TUNative(context.applicationContext, placementId, object : TUNativeNetworkListener {
            override fun onNativeAdLoaded() {
                Log.d(TAG, "TopOn native loaded: placementId=$placementId, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
                dispatchCallback(TopOnAdEvent.Loaded(TopOnAdFormat.NATIVE))
            }

            override fun onNativeAdLoadFail(error: AdError?) {
                Log.d(TAG, "TopOn native load fail: placementId=$placementId, error=$error, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
                dispatchCallback(TopOnAdEvent.Failed(TopOnAdFormat.NATIVE, TopOnAdError.from(error)))
            }
        })
    }

    fun load(callback: TopOnAdCallback? = null) {
        callback?.let { updateCallback(it, "load") }
        if (callback == null) {
            Log.d(TAG, "TopOn native load using existing callback: placementId=$placementId, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
        }
        native?.makeAdRequest()
    }

    fun takeLoadedAd(callback: TopOnAdCallback? = null): TopOnNativeAdHandle? {
        callback?.let { updateCallback(it, "takeLoadedAd") }
        Log.d(TAG, "TopOn native handle requested: placementId=$placementId, callbackOwner=$callbackDebugOwner, callbackVersion=$callbackDebugVersion")
        val ad = native?.getNativeAd()
        if (ad == null) {
            Log.d(TAG, "TopOn native handle is null: placementId=$placementId")
            return null
        }
        Log.d(TAG, "TopOn native handle created: placementId=$placementId, isNativeExpress=${ad.isNativeExpress}, nativeType=${ad.nativeType}, objectClass=${ad.`object`?.javaClass?.name ?: "null"}")
        return TopOnNativeAdHandle(ad, this.callback, placementId)
    }

    private fun updateCallback(callback: TopOnAdCallback, owner: String) {
        this.callback = callback
        callbackDebugOwner = owner
        callbackDebugVersion += 1
        Log.d(TAG, "TopOn native callback updated: placementId=$placementId, owner=$owner, callbackVersion=$callbackDebugVersion")
    }

    private fun dispatchCallback(event: TopOnAdEvent) {
        callback(event)
    }
}

class TopOnNativeAdHandle internal constructor(
    private val nativeAd: NativeAd,
    private val callback: TopOnAdCallback,
    private val placementId: String
) {
    companion object {
        private const val TAG = "TopOnNativeAdHandle"
    }

    val isValid: Boolean
        get() = nativeAd.isValid

    fun isExpressAd(): Boolean = nativeAd.isNativeExpress

    fun describeRenderState(): String {
        val adObject = nativeAd.`object`
        return "isNativeExpress=${nativeAd.isNativeExpress}, " +
            "nativeType=${nativeAd.nativeType}, " +
            "objectClass=${adObject?.javaClass?.name ?: "null"}"
    }

    fun render(
        adView: TUNativeAdView,
        contentView: View,
        prepareInfo: TUNativePrepareInfo? = null
    ) {
        nativeAd.renderAdContainer(adView, contentView)
        prepareInfo?.let { nativeAd.prepare(adView, it) }
        nativeAd.setNativeEventListener(object : TUNativeEventListener {
            override fun onAdImpressed(view: TUNativeAdView?, info: TUAdInfo?) {
                Log.d(TAG, "TopOn native impressed: placementId=$placementId, info=$info")
                callback(TopOnAdEvent.Shown(TopOnAdFormat.NATIVE, TopOnAdInfo.from(info)))
            }

            override fun onAdClicked(view: TUNativeAdView?, info: TUAdInfo?) {
                Log.d(TAG, "TopOn native clicked: placementId=$placementId, info=$info")
                callback(TopOnAdEvent.Clicked(TopOnAdFormat.NATIVE, TopOnAdInfo.from(info)))
            }

            override fun onAdVideoStart(view: TUNativeAdView?) {
                Log.d(TAG, "TopOn native video start: placementId=$placementId")
                callback(TopOnAdEvent.VideoStarted(TopOnAdFormat.NATIVE, nativeAd.adInfo?.let { TopOnAdInfo.from(it) }))
            }

            override fun onAdVideoEnd(view: TUNativeAdView?) {
                Log.d(TAG, "TopOn native video end: placementId=$placementId")
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

    fun renderStandard(adView: TUNativeAdView, fullscreen: Boolean = false): Boolean {
        val material = nativeAd.adMaterial ?: return false
        val context = adView.context
        val isCompact = !fullscreen

        val root = LinearLayout(context).apply {
            orientation = if (isCompact) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            gravity = if (isCompact) Gravity.CENTER_VERTICAL else Gravity.NO_GRAVITY
            val horizontalPadding = dp(context, if (isCompact) 10 else 12)
            val verticalPadding = dp(context, if (isCompact) 10 else 12)
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(context, 12).toFloat()
                setColor(Color.WHITE)
            }
        }

        val mediaContainer = FrameLayout(context).apply {
            layoutParams = if (isCompact) {
                LinearLayout.LayoutParams(
                    dp(context, 76),
                    dp(context, 76)
                ).apply {
                    marginEnd = dp(context, 10)
                }
            } else {
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    if (fullscreen) 0 else dp(context, 180),
                    if (fullscreen) 1f else 0f
                ).apply {
                    bottomMargin = dp(context, 12)
                }
            }
        }

        val contentColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                if (isCompact) 0 else ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                if (isCompact) 1f else 0f
            )
            gravity = if (isCompact) Gravity.CENTER_VERTICAL else Gravity.NO_GRAVITY
        }

        val iconContainer = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                if (isCompact) dp(context, 32) else dp(context, 44),
                if (isCompact) dp(context, 32) else dp(context, 44)
            ).apply {
                marginEnd = dp(context, if (isCompact) 8 else 10)
            }
        }

        val headerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL
        }

        val titleView = TextView(context).apply {
            setTextColor(Color.parseColor("#111827"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isCompact) 13f else 15f)
            typeface = Typeface.DEFAULT_BOLD
            maxLines = if (isCompact) 2 else 1
            ellipsize = TextUtils.TruncateAt.END
            text = material.title.orEmpty()
        }

        val descView = TextView(context).apply {
            setTextColor(Color.parseColor("#4B5563"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isCompact) 11f else 13f)
            maxLines = if (isCompact) 1 else if (fullscreen) 4 else 2
            ellipsize = TextUtils.TruncateAt.END
            text = material.descriptionText.orEmpty()
        }

        val footerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(context, if (isCompact) 6 else 12)
            }
            gravity = Gravity.CENTER_VERTICAL
        }

        val adFromView = TextView(context).apply {
            setTextColor(Color.parseColor("#6B7280"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isCompact) 10f else 12f)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            maxLines = if (isCompact) 1 else 1
            ellipsize = TextUtils.TruncateAt.END
            text = material.adFrom?.ifBlank { material.advertiserName }.orEmpty()
        }

        val ctaView = TextView(context).apply {
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isCompact) 12f else 13f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            minWidth = dp(context, if (isCompact) 72 else 88)
            setPadding(
                dp(context, if (isCompact) 12 else 14),
                dp(context, if (isCompact) 8 else 10),
                dp(context, if (isCompact) 12 else 14),
                dp(context, if (isCompact) 8 else 10)
            )
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(context, 8).toFloat()
                setColor(Color.parseColor("#2563EB"))
            }
            text = material.callToActionText?.ifBlank { "Open" } ?: "Open"
        }

        val mediaView = material.getAdMediaView()
        mediaView?.let {
            mediaContainer.removeAllViews()
            mediaContainer.addView(
                it,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        val iconView = material.adIconView
        iconView?.let {
            val targetContainer = if (isCompact && mediaView == null) mediaContainer else iconContainer
            targetContainer.removeAllViews()
            targetContainer.addView(
                it,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }

        val compactTopRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL
        }

        val textColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        textColumn.addView(titleView)
        if (!material.descriptionText.isNullOrBlank()) {
            textColumn.addView(descView)
        }
        footerRow.addView(adFromView)

        if (isCompact) {
            compactTopRow.addView(textColumn)
            compactTopRow.addView(ctaView)
            contentColumn.addView(compactTopRow)
            contentColumn.addView(footerRow)
            root.addView(mediaContainer)
            root.addView(contentColumn)
        } else {
            headerRow.addView(iconContainer)
            headerRow.addView(textColumn)
            footerRow.addView(ctaView)
            root.addView(mediaContainer)
            root.addView(headerRow)
            root.addView(footerRow)
        }

        val prepareInfo = TUNativePrepareInfo().apply {
            setParentView(root)
            setTitleView(titleView)
            setIconView(iconView ?: if (isCompact) mediaContainer else iconContainer)
            setMainImageView(mediaView ?: mediaContainer)
            setDescView(descView)
            setCtaView(ctaView)
            setAdFromView(adFromView)
            setClickViewList(listOf(root, ctaView, mediaView ?: mediaContainer, titleView))
        }

        render(adView, root, prepareInfo)
        return true
    }

    fun clear(adView: TUNativeAdView) {
        nativeAd.clear(adView)
    }

    fun destroy() {
        nativeAd.destory()
    }

    private fun dp(context: Context, value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            context.resources.displayMetrics
        ).toInt()
}

private object Application {
    fun getProcessNameCompat(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            android.app.Application.getProcessName()
        } else {
            null
        }
}
