package com.cc.ads.topon

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.thinkup.nativead.api.TUNativeAdView

object TopOnAdSceneManager {
    private const val TAG = "TopOnAdSceneManager"
    private var splashAd: TopOnSplashAd? = null
    private val interstitialAds = mutableMapOf<String, TopOnInterstitialAd>()
    private val nativeLoaders = mutableMapOf<String, TopOnNativeAdLoader>()

    fun preloadSplash(context: Context, firstOpen: Boolean) {
        val placementId = if (firstOpen) TopOnAdScenes.FIRST_SPLASH else TopOnAdScenes.ACTIVE_SPLASH
        Log.d(TAG, "TopOn splash preload requested: placementId=$placementId, firstOpen=$firstOpen")
        splashAd = TopOnAds.splash(context.applicationContext, placementId)
        splashAd?.load()
    }

    fun showSplashIfReady(activity: Activity, container: ViewGroup, onFinished: () -> Unit = {}): Boolean {
        val ad = splashAd ?: run {
            Log.d(TAG, "TopOn splash show requested but ad is null")
            return false
        }
        Log.d(TAG, "TopOn splash show requested: ready=${ad.isReady()}, containerVisibility=${container.visibility}")
        val shown = ad.show(activity, container) { event ->
            Log.d(TAG, "TopOn splash event: event=$event")
            if (event is TopOnAdEvent.Closed || event is TopOnAdEvent.Failed) {
                onFinished()
            }
        }
        if (!shown) {
            Log.d(TAG, "TopOn splash show returned false")
            onFinished()
        }
        return shown
    }

    fun preloadFirstInterstitial(context: Context) {
        preloadInterstitial(context, TopOnAdScenes.FIRST_INTERSTITIAL)
    }

    fun preloadGeneralInterstitial(context: Context) {
        preloadInterstitial(context, TopOnAdScenes.GENERAL_INTERSTITIAL)
    }

    fun preloadLauncherAppInterstitial(context: Context) {
        if (!canUseExternalInterstitial(context)) {
            Log.d(TAG, "TopOn launcher interstitial preload skipped: user is not yet eligible")
            return
        }
        preloadInterstitial(context, TopOnAdScenes.LAUNCHER_APP_INTERSTITIAL)
    }

    fun preloadInterstitial(context: Context, placementId: String) {
        val ad = interstitialAds.getOrPut(placementId) {
            TopOnAds.interstitial(context.applicationContext, placementId)
        }
        ad.load()
    }

    fun showFirstInterstitial(activity: Activity, onFinished: () -> Unit) {
        showInterstitial(activity, TopOnAdScenes.FIRST_INTERSTITIAL, onFinished)
    }

    fun showGeneralInterstitial(activity: Activity, onFinished: () -> Unit) {
        showReloadingInterstitial(activity, TopOnAdScenes.GENERAL_INTERSTITIAL, onFinished)
    }

    fun showLauncherAppInterstitial(activity: Activity, onFinished: () -> Unit) {
        if (!canUseExternalInterstitial(activity)) {
            Log.d(TAG, "TopOn launcher interstitial show skipped: user is not yet eligible")
            onFinished()
            return
        }
        showReloadingInterstitial(activity, TopOnAdScenes.LAUNCHER_APP_INTERSTITIAL, onFinished)
    }

    fun showReloadingInterstitial(activity: Activity, placementId: String, onFinished: () -> Unit) {
        showInterstitial(activity, placementId) {
            preloadInterstitial(activity.applicationContext, placementId)
            onFinished()
        }
    }

    fun showInterstitial(activity: Activity, placementId: String, onFinished: () -> Unit) {
        val ad = interstitialAds.getOrPut(placementId) {
            TopOnAds.interstitial(activity.applicationContext, placementId)
        }
        var finished = false
        Log.d(TAG, "TopOn launch interstitial show requested: placementId=$placementId, ready=${ad.isReady()}")
        fun finishOnce() {
            if (!finished) {
                finished = true
                Log.d(TAG, "TopOn launch interstitial finish: placementId=$placementId")
                onFinished()
            }
        }
        if (!ad.isReady()) {
            Log.d(TAG, "TopOn launch interstitial not ready: placementId=$placementId")
            ad.load()
            finishOnce()
            return
        }
        val shown = ad.show(activity) { event ->
            Log.d(TAG, "TopOn launch interstitial event: placementId=$placementId, event=$event")
            when (event) {
                is TopOnAdEvent.Closed,
                is TopOnAdEvent.Failed -> finishOnce()
                else -> Unit
            }
        }
        if (!shown) {
            Log.d(TAG, "TopOn launch interstitial show returned false: placementId=$placementId")
            finishOnce()
        }
    }

    private fun canUseExternalInterstitial(context: Context): Boolean {
        return TopOnAdAttributionStore.canUseExternalInterstitial(context.applicationContext)
    }

    fun preloadGuideNative(context: Context) {
        preloadNative(context, TopOnAdScenes.GUIDE_NATIVE)
    }

    fun preloadNative(context: Context, placementId: String) {
        val loader = nativeLoaders.getOrPut(placementId) {
            TopOnAds.native(context.applicationContext, placementId)
        }
        Log.d(TAG, "TopOn native preload requested: placementId=$placementId")
        loader.load()
    }

    fun renderNativeInto(
        container: ViewGroup,
        placementId: String,
        fullscreen: Boolean = false,
        renderWhenLoaded: Boolean = true
    ) {
        Log.d(
            TAG,
            "TopOn native render requested: placementId=$placementId, " +
                "renderWhenLoaded=$renderWhenLoaded, fullscreen=$fullscreen, " +
                "containerVisibility=${container.visibility}, childCount=${container.childCount}"
        )
        val loader = nativeLoaders.getOrPut(placementId) {
            TopOnAds.native(container.context.applicationContext, placementId)
        }
        val handle = loader.takeLoadedAd()
        if (handle == null || !handle.isValid) {
            Log.d(
                TAG,
                "TopOn native handle missing or invalid: placementId=$placementId, " +
                    "handleNull=${handle == null}, isValid=${handle?.isValid}"
            )
            container.visibility = View.GONE
            if (renderWhenLoaded) {
                loader.load { event ->
                    Log.d(TAG, "TopOn native async load event: placementId=$placementId, event=$event")
                    if (event is TopOnAdEvent.Loaded) {
                        Log.d(TAG, "TopOn native loaded event received for rerender: placementId=$placementId")
                        container.post {
                            renderNativeInto(container, placementId, fullscreen, renderWhenLoaded)
                        }
                    }
                }
            } else {
                loader.load()
            }
            return
        }
        val nativeView = TUNativeAdView(container.context)
        nativeView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        container.removeAllViews()
        container.addView(nativeView)
        val renderResult = handle.renderExpress(nativeView)
        Log.d(TAG, "TopOn native renderExpress result: placementId=$placementId, result=$renderResult")
        Log.d(TAG, "TopOn native renderExpress metadata: placementId=$placementId, ${handle.describeRenderState()}")
        val standardRenderResult =
            if (!renderResult && !handle.isExpressAd()) {
                handle.renderStandard(nativeView, fullscreen)
            } else {
                false
            }
        Log.d(TAG, "TopOn native standard render fallback result: placementId=$placementId, result=$standardRenderResult")
        if (!renderResult && !standardRenderResult) {
            container.visibility = View.GONE
            Log.d(TAG, "TopOn native render failed, hiding container: placementId=$placementId")
            handle.destroy()
        } else {
            container.visibility = View.VISIBLE
            Log.d(TAG, "TopOn native container visible: placementId=$placementId, childCount=${container.childCount}")
        }
    }
}
