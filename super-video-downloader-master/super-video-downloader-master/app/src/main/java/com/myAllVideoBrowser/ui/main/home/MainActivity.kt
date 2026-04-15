package com.myAllVideoBrowser.ui.main.home

import android.Manifest
import android.annotation.SuppressLint
import android.animation.ValueAnimator
import android.app.Activity
import android.content.res.ColorStateList
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import com.cc.ads.topon.TopOnAdSceneManager
import com.cc.ads.topon.TopOnAdScenes
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.Observable
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.myAllVideoBrowser.DLApplication
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.ActivityMainBinding
import com.myAllVideoBrowser.ui.component.adapter.MainAdapter
import com.myAllVideoBrowser.ui.main.base.BaseActivity
import com.myAllVideoBrowser.ui.main.proxies.ProxiesViewModel
import com.myAllVideoBrowser.ui.main.settings.LAUNCHER_ACTIVATION_PREFS
import com.myAllVideoBrowser.ui.main.settings.RETURN_TO_DOWNLOADER_AFTER_HOME_SELECTION
import com.myAllVideoBrowser.ui.main.settings.requestLauncherSelection
import com.myAllVideoBrowser.ui.main.settings.SettingsViewModel
import com.myAllVideoBrowser.util.AppLogger
import com.myAllVideoBrowser.util.SharedPrefHelper
import com.myAllVideoBrowser.util.downloaders.youtubedl_downloader.YoutubeDlDownloaderWorker
import com.myAllVideoBrowser.util.fragment.FragmentFactory
import com.myAllVideoBrowser.util.proxy_utils.proxy_manager.ProxyManager
import com.myAllVideoBrowser.util.scheduler.BaseSchedulers
import javax.inject.Inject

//@OpenForTesting
class MainActivity : BaseActivity() {

    @Inject
    lateinit var fragmentFactory: FragmentFactory

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var baseSchedulers: BaseSchedulers

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper

    lateinit var mainViewModel: MainViewModel

    lateinit var proxiesViewModel: ProxiesViewModel

    lateinit var settingsViewModel: SettingsViewModel

    private lateinit var dataBinding: ActivityMainBinding

    private lateinit var mainAdapter: MainAdapter
    private val launchFlowHandler = Handler(Looper.getMainLooper())
    private var onboardingPageIndex = 0
    private var launcherSelectionRequested = false
    private var suppressLauncherPromptOnResume = false
    private lateinit var launchFlowOverlay: View
    private lateinit var launchSplashRoot: View
    private lateinit var launchSplashProgressIndicator: LinearProgressIndicator
    private lateinit var launchOnboardingRoot: View
    private lateinit var launchLauncherPromptRoot: View
    private lateinit var launchSplashAdContainer: FrameLayout
    private lateinit var onboardingNativeAdContainer: FrameLayout
    private lateinit var onboardingPageOne: View
    private lateinit var onboardingPageTwo: View
    private lateinit var onboardingPageThree: View
    private lateinit var onboardingTitle: TextView
    private lateinit var onboardingIndicatorOne: View
    private lateinit var onboardingIndicatorTwo: View
    private lateinit var onboardingIndicatorThree: View
    private lateinit var onboardingNextButton: MaterialButton
    private lateinit var launcherPromptStepOne: TextView
    private lateinit var launcherPromptStepTwo: TextView
    private var splashProgressAnimator: ValueAnimator? = null
    private var launcherPromptAttempts = 0
    private var notificationPermissionRequested = false
    private val requestHomeRoleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val granted = result.resultCode == RESULT_OK && isAppDefaultHome()
            AppLogger.d("MainActivity.requestHomeRoleLauncher granted=$granted resultCode=${result.resultCode}")
            if (granted) {
                startActivity(
                    Intent(this, MainActivity::class.java)
                        .putExtra(EXTRA_SKIP_LAUNCH_SPLASH, true)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                )
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                AppLogger.d("MainActivity.requestHomeRoleLauncher launcher role request canceled")
            }
        }

    private val screenOrientationCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            val isLock = settingsViewModel.isLockPortrait.get()
            requestedOrientation = if (isLock) {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        (applicationContext as? DLApplication)?.startProxyService()

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        disableBottomBarIndicator(dataBinding.bottomBar)
        normalizeBottomBarIcons(dataBinding.bottomBar)
        bindLaunchFlowViews()

        mainViewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
        proxiesViewModel = ViewModelProvider(this, viewModelFactory)[ProxiesViewModel::class.java]
        settingsViewModel = ViewModelProvider(this, viewModelFactory)[SettingsViewModel::class.java]

        mainAdapter = MainAdapter(supportFragmentManager, lifecycle, fragmentFactory)

        dataBinding.viewPager.isUserInputEnabled = false
        dataBinding.viewPager.adapter = mainAdapter
        dataBinding.viewPager.registerOnPageChangeCallback(onPageChangeListener)
        dataBinding.bottomBar.setOnItemSelectedListener { menuItem ->
            val isBrowser = mainViewModel.currentItem.get() == 0
            var goingToBrowser = false
            when (menuItem.itemId) {
                R.id.tab_browser -> {
                    mainViewModel.currentItem.set(0)
                    goingToBrowser = true
                }

                R.id.tab_progress -> mainViewModel.currentItem.set(1)
                R.id.tab_video -> mainViewModel.currentItem.set(2)
                else -> mainViewModel.currentItem.set(3)
            }

            if (isBrowser && goingToBrowser && mainViewModel.isBrowserCurrent.get()) {
                mainViewModel.openNavDrawerEvent.call()
            }
            return@setOnItemSelectedListener true
        }
        dataBinding.viewModel = mainViewModel

        proxiesViewModel.start()
        settingsViewModel.start()
        mainViewModel.start()

        if (intent.action == Intent.ACTION_VIEW) {
            val videoUrl = intent.dataString
            if (videoUrl != null) {
                mainViewModel.openedUrl.set(videoUrl)
            }
        }

        if (intent.action == Intent.ACTION_SEND) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                mainViewModel.openedText.set(sharedText)
            }
        }

        handleScreenOrientationSettingChange()
        handleScreenOrientationSettingsInit()
        if (intent.getBooleanExtra(EXTRA_SKIP_LAUNCH_SPLASH, false)) {
            continueLaunchFlowAfterLauncherReturn()
        } else {
            maybeStartLaunchFlow(savedInstanceState)
        }

        onNewIntent(intent)
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.getBooleanExtra(EXTRA_SKIP_LAUNCH_SPLASH, false) == true) {
            continueLaunchFlowAfterLauncherReturn()
            return
        }
        if (intent?.getBooleanExtra(
                YoutubeDlDownloaderWorker.IS_FINISHED_DOWNLOAD_ACTION_KEY,
                false
            ) == true
        ) {
            if (intent.getBooleanExtra(
                    YoutubeDlDownloaderWorker.IS_FINISHED_DOWNLOAD_ACTION_ERROR_KEY,
                    false
                )
            ) {
                dataBinding.viewPager.currentItem = 1
            } else {
                dataBinding.viewPager.currentItem = 2
            }

            if (intent.hasExtra(YoutubeDlDownloaderWorker.DOWNLOAD_FILENAME_KEY)) {
                val downloadFileName =
                    intent.getStringExtra(YoutubeDlDownloaderWorker.DOWNLOAD_FILENAME_KEY)
                        .toString()

                Handler(Looper.getMainLooper()).postDelayed({
                    mainViewModel.openDownloadedVideoEvent.value = downloadFileName
                }, 1000)
            }
        } else {
            if (intent?.hasExtra(YoutubeDlDownloaderWorker.IS_FINISHED_DOWNLOAD_ACTION_KEY) == true) {
                dataBinding.viewPager.currentItem = 1
            } else {
                dataBinding.viewPager.currentItem = 0
            }
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (notificationPermissionRequested) {
            return
        }
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionRequested = true
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    ),
                    0
                )
            }
        }
    }

    private val onPageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(p0: Int) {
        }

        override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
        }

        override fun onPageSelected(postion: Int) {
            if (postion == 0) {
                // Если без этого, дровер отркрываетс когда не надо
                Handler(Looper.getMainLooper()).postDelayed({
                    mainViewModel.isBrowserCurrent.set(true)
                }, 1000)
            } else {
                mainViewModel.isBrowserCurrent.set(false)
            }

            val childrenCount = dataBinding.fragmentContainerView.childCount
            if (childrenCount > 0) {
                supportFragmentManager.popBackStack()
            }
            if (postion > 0) {
                dataBinding.viewPager.isUserInputEnabled = true
            } else {
                dataBinding.viewPager.isUserInputEnabled = false
            }

            mainViewModel.currentItem.set(postion)
        }
    }

    override fun onResume() {
        super.onResume()
        if (launcherSelectionRequested) {
            clearLauncherActivationReturnFlag()
            launcherSelectionRequested = false
            suppressLauncherPromptOnResume = false
            val isDefaultHome = isAppDefaultHome()
            if (shouldRequestLauncherBeforeGuide(launcherPromptAttempts, isDefaultHome)) {
                requestLauncherActivation(skipPromptOnResume = true)
            } else {
                showOnboarding()
            }
        }
    }

    override fun onDestroy() {
        mainViewModel.stop()
        settingsViewModel.isLockPortrait.removeOnPropertyChangedCallback(screenOrientationCallback)
        launchFlowHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun handleScreenOrientationSettingsInit() {
        // INIT
        requestedOrientation = if (settingsViewModel.isLockPortrait.get()) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private fun handleScreenOrientationSettingChange() {
        // CHANGES HANDLING
        settingsViewModel.isLockPortrait.addOnPropertyChangedCallback(screenOrientationCallback)
    }

    private fun disableBottomBarIndicator(bottomBar: BottomNavigationView) {
        runCatching {
            BottomNavigationView::class.java
                .getMethod("setItemActiveIndicatorEnabled", java.lang.Boolean.TYPE)
                .invoke(bottomBar, false)
        }
        runCatching {
            BottomNavigationView::class.java
                .getMethod("setItemActiveIndicatorColor", ColorStateList::class.java)
                .invoke(bottomBar, ColorStateList.valueOf(Color.TRANSPARENT))
        }
    }

    private fun normalizeBottomBarIcons(bottomBar: BottomNavigationView) {
        bottomBar.itemIconTintList = null
        bottomBar.menu.findItem(R.id.tab_browser)?.icon =
            AppCompatResources.getDrawable(this, R.drawable.selector_bottom_nav_browser)
        bottomBar.menu.findItem(R.id.tab_progress)?.icon =
            AppCompatResources.getDrawable(this, R.drawable.selector_bottom_nav_processing)
        bottomBar.menu.findItem(R.id.tab_video)?.icon =
            AppCompatResources.getDrawable(this, R.drawable.selector_bottom_nav_downloaded)
        bottomBar.menu.findItem(R.id.tab_settings)?.icon =
            AppCompatResources.getDrawable(this, R.drawable.selector_bottom_nav_settings)
    }

    private fun bindLaunchFlowViews() {
        launchFlowOverlay = findViewById(R.id.launch_flow_overlay)
        launchSplashRoot = findViewById(R.id.launch_splash_root)
        launchSplashProgressIndicator = findViewById(R.id.launch_splash_progress_indicator)
        launchOnboardingRoot = findViewById(R.id.launch_onboarding_root)
        launchLauncherPromptRoot = findViewById(R.id.launch_launcher_prompt_root)
        onboardingPageOne = findViewById(R.id.onboarding_page_one)
        onboardingPageTwo = findViewById(R.id.onboarding_page_two)
        onboardingPageThree = findViewById(R.id.onboarding_page_three)
        onboardingTitle = findViewById(R.id.onboarding_title)
        onboardingIndicatorOne = findViewById(R.id.onboarding_indicator_one)
        onboardingIndicatorTwo = findViewById(R.id.onboarding_indicator_two)
        onboardingIndicatorThree = findViewById(R.id.onboarding_indicator_three)
        onboardingNextButton = findViewById(R.id.onboarding_next_button)
        launcherPromptStepOne = findViewById(R.id.launch_launcher_prompt_step_one)
        launcherPromptStepTwo = findViewById(R.id.launch_launcher_prompt_step_two)
        launchSplashAdContainer = findViewById(R.id.launch_splash_ad_container)
        onboardingNativeAdContainer = findViewById(R.id.onboarding_native_ad_container)

        onboardingNextButton.setOnClickListener {
            if (onboardingPageIndex == 1 && maybeShowGuideNativeAd()) {
                return@setOnClickListener
            }
            if (onboardingPageIndex >= LAST_ONBOARDING_PAGE_INDEX) {
                sharedPrefHelper.setIsFirstStart(false)
                TopOnAdSceneManager.showFirstInterstitial(this) {
                    TopOnAdSceneManager.preloadGeneralInterstitial(applicationContext)
                    finishLaunchFlow()
                }
            } else {
                onboardingPageIndex += 1
                renderOnboardingPage()
            }
        }
        onboardingNativeAdContainer.setOnClickListener {
            onboardingNativeAdContainer.isVisible = false
            onboardingPageIndex += 1
            renderOnboardingPage()
        }

        val requestLauncherClickListener = View.OnClickListener {
            finishLaunchFlow()
            requestLauncherActivation(skipPromptOnResume = true)
        }
        launchLauncherPromptRoot.setOnClickListener(requestLauncherClickListener)
        findViewById<View>(R.id.launch_launcher_continue).setOnClickListener(requestLauncherClickListener)
        findViewById<View>(R.id.launch_launcher_phone_mock).setOnClickListener(requestLauncherClickListener)
    }

    private fun maybeStartLaunchFlow(savedInstanceState: Bundle?) {
        showLaunchSurface(launchSplashRoot)
        startSplashProgress()
        val isFirstStart = sharedPrefHelper.getIsFirstStart()
        TopOnAdSceneManager.preloadSplash(applicationContext, firstOpen = isFirstStart)
        launchFlowHandler.postDelayed({
            val isDefaultHome = isAppDefaultHome()
            TopOnAdSceneManager.preloadFirstInterstitial(applicationContext)
            TopOnAdSceneManager.preloadGuideNative(applicationContext)
            if (shouldRequestLauncherBeforeGuide(launcherPromptAttempts, isDefaultHome)) {
                requestLauncherActivation(skipPromptOnResume = true)
            } else {
                showOnboarding()
            }
        }, SPLASH_DELAY_MS)
    }

    private fun continueLaunchFlowAfterLauncherReturn() {
        launcherSelectionRequested = false
        suppressLauncherPromptOnResume = false
        showOnboarding()
    }

    private fun showOnboarding() {
        onboardingPageIndex = 0
        renderOnboardingPage()
        showLaunchSurface(launchOnboardingRoot)
        showLoadedSplashAd()
    }

    private fun renderOnboardingPage() {
        onboardingPageOne.isVisible = onboardingPageIndex == 0
        onboardingPageTwo.isVisible = onboardingPageIndex == 1
        onboardingPageThree.isVisible = onboardingPageIndex == 2

        onboardingTitle.setText(
            when (onboardingPageIndex) {
                0 -> R.string.launch_onboarding_title_one
                1 -> R.string.launch_onboarding_title_two
                else -> R.string.launch_onboarding_title_three
            }
        )

        onboardingNextButton.setText(
            if (onboardingPageIndex == LAST_ONBOARDING_PAGE_INDEX) {
                R.string.launch_start
            } else {
                R.string.launch_next
            }
        )

        val activeDrawable = AppCompatResources.getDrawable(this, R.drawable.bg_launch_indicator_active)
        val inactiveDrawable = AppCompatResources.getDrawable(this, R.drawable.bg_launch_indicator_inactive)
        onboardingIndicatorOne.background = if (onboardingPageIndex == 0) activeDrawable else inactiveDrawable
        onboardingIndicatorTwo.background = if (onboardingPageIndex == 1) activeDrawable else inactiveDrawable
        onboardingIndicatorThree.background = if (onboardingPageIndex == 2) activeDrawable else inactiveDrawable
    }

    private fun maybeShowLauncherPromptOrFinish() {
        if (shouldShowLauncherPrompt()) {
            showLauncherPrompt()
        } else {
            finishLaunchFlow()
        }
    }

    private fun showLauncherPrompt() {
        configureLauncherPromptText()
        showLaunchSurface(launchLauncherPromptRoot)
    }

    private fun finishLaunchFlow() {
        launchFlowHandler.removeCallbacksAndMessages(null)
        splashProgressAnimator?.cancel()
        splashProgressAnimator = null
        launchFlowOverlay.isVisible = false
        launchSplashRoot.isVisible = false
        launchOnboardingRoot.isVisible = false
        launchLauncherPromptRoot.isVisible = false
        launchSplashAdContainer.isVisible = false
        onboardingNativeAdContainer.isVisible = false
        maybeRequestNotificationPermission()
        if (shouldShowLauncherPrompt()) {
            showLauncherReminder()
        }
    }

    private fun startSplashProgress() {
        splashProgressAnimator?.cancel()
        launchSplashProgressIndicator.progress = 0
        splashProgressAnimator = ValueAnimator.ofInt(0, SPLASH_PROGRESS_MAX).apply {
            duration = SPLASH_DELAY_MS
            addUpdateListener { animator ->
                launchSplashProgressIndicator.setProgressCompat(
                    animator.animatedValue as Int,
                    true
                )
            }
            start()
        }
    }

    private fun showLaunchSurface(surface: View) {
        launchFlowOverlay.isVisible = true
        launchSplashRoot.isVisible = surface === launchSplashRoot
        launchOnboardingRoot.isVisible = surface === launchOnboardingRoot
        launchLauncherPromptRoot.isVisible = surface === launchLauncherPromptRoot
        launchSplashAdContainer.isVisible = false
    }

    private fun showLauncherReminder() {
        Snackbar.make(
            dataBinding.root,
            R.string.launch_launcher_prompt_title,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.launch_continue) {
            requestLauncherActivation(skipPromptOnResume = true)
        }.show()
    }

    private fun shouldShowLauncherPrompt(): Boolean = !isAppDefaultHome()

    private fun requestLauncherActivation(skipPromptOnResume: Boolean = false) {
        launcherPromptAttempts += 1
        launcherSelectionRequested = true
        suppressLauncherPromptOnResume = skipPromptOnResume
        requestLauncherSelection(this, requestHomeRoleLauncher)
    }

    private fun clearLauncherActivationReturnFlag() {
        getSharedPreferences(LAUNCHER_ACTIVATION_PREFS, MODE_PRIVATE)
            .edit()
            .putBoolean(RETURN_TO_DOWNLOADER_AFTER_HOME_SELECTION, false)
            .apply()
    }

    private fun showLoadedSplashAd() {
        launchSplashAdContainer.isVisible = true
        TopOnAdSceneManager.showSplashIfReady(this, launchSplashAdContainer) {
            launchSplashAdContainer.isVisible = false
        }
    }

    private fun maybeShowGuideNativeAd(): Boolean {
        onboardingNativeAdContainer.isVisible = true
        TopOnAdSceneManager.renderNativeInto(
            onboardingNativeAdContainer,
            TopOnAdScenes.GUIDE_NATIVE,
            fullscreen = true,
            renderWhenLoaded = false
        )
        return onboardingNativeAdContainer.isVisible
    }

    private fun isAppDefaultHome(): Boolean {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolvedActivity =
            packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolvedActivity?.activityInfo?.packageName == packageName
    }

    private fun configureLauncherPromptText() {
        launcherPromptStepOne.text = buildHighlightedText(
            getString(R.string.launch_launcher_step_one),
            getString(R.string.launch_continue)
        )
        launcherPromptStepTwo.text = buildHighlightedText(
            getString(R.string.launch_launcher_step_two),
            getString(R.string.launch_splash_app_name)
        )
    }

    private fun buildHighlightedText(text: String, highlight: String): SpannableString {
        val spannable = SpannableString(text)
        val startIndex = text.indexOf(highlight)
        if (startIndex >= 0) {
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.linkText)),
                startIndex,
                startIndex + highlight.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spannable
    }

    companion object {
        const val EXTRA_SKIP_LAUNCH_SPLASH = "extra_skip_launch_splash"
        const val SPLASH_DELAY_MS = 3000L
        const val LAST_ONBOARDING_PAGE_INDEX = 2
        const val SPLASH_PROGRESS_MAX = 100
    }
}
