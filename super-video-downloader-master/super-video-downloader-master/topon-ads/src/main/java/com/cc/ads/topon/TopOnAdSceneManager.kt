package com.cc.ads.topon

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.thinkup.nativead.api.TUNativeAdView

object TopOnAdSceneManager {
    private var splashAd: TopOnSplashAd? = null
    private val interstitialAds = mutableMapOf<String, TopOnInterstitialAd>()
    private val nativeLoaders = mutableMapOf<String, TopOnNativeAdLoader>()

    fun preloadSplash(context: Context, firstOpen: Boolean) {
        val placementId = if (firstOpen) TopOnAdScenes.FIRST_SPLASH else TopOnAdScenes.ACTIVE_SPLASH
        splashAd = TopOnAds.splash(context.applicationContext, placementId)
        splashAd?.load()
    }

    fun showSplashIfReady(activity: Activity, container: ViewGroup, onFinished: () -> Unit = {}): Boolean {
        val ad = splashAd ?: return false
        val shown = ad.show(activity, container) { event ->
            if (event is TopOnAdEvent.Closed || event is TopOnAdEvent.Failed) {
                onFinished()
            }
        }
        if (!shown) {
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
        fun finishOnce() {
            if (!finished) {
                finished = true
                onFinished()
            }
        }
        if (!ad.isReady()) {
            ad.load()
            finishOnce()
            return
        }
        val shown = ad.show(activity) { event ->
            when (event) {
                is TopOnAdEvent.Closed,
                is TopOnAdEvent.Failed -> finishOnce()
                else -> Unit
            }
        }
        if (!shown) {
            finishOnce()
        }
    }

    fun preloadGuideNative(context: Context) {
        preloadNative(context, TopOnAdScenes.GUIDE_NATIVE)
    }

    fun preloadNative(context: Context, placementId: String) {
        val loader = nativeLoaders.getOrPut(placementId) {
            TopOnAds.native(context.applicationContext, placementId)
        }
        loader.load()
    }

    fun renderNativeInto(
        container: ViewGroup,
        placementId: String,
        fullscreen: Boolean = false,
        renderWhenLoaded: Boolean = true
    ) {
        val loader = nativeLoaders.getOrPut(placementId) {
            TopOnAds.native(container.context.applicationContext, placementId)
        }
        val handle = loader.takeLoadedAd()
        if (handle == null || !handle.isValid) {
            container.visibility = View.GONE
            if (renderWhenLoaded) {
                loader.load { event ->
                    if (event is TopOnAdEvent.Loaded) {
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
            if (fullscreen) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
        )
        container.removeAllViews()
        container.addView(nativeView)
        if (!handle.renderExpress(nativeView)) {
            container.visibility = View.GONE
            handle.destroy()
        } else {
            container.visibility = View.VISIBLE
        }
    }
}
