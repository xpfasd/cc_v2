package com.myAllVideoBrowser.ui.main.home.browser

//import com.allVideoDownloaderXmaster.OpenForTesting

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.*
import android.webkit.ServiceWorkerClient
import android.webkit.ServiceWorkerController
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.GravityCompat
import androidx.databinding.Observable
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.FragmentBrowserBinding
import com.myAllVideoBrowser.ui.component.adapter.WebTabsAdapter
import com.myAllVideoBrowser.ui.component.adapter.WebTabsListener
import com.myAllVideoBrowser.ui.main.base.BaseFragment
import com.myAllVideoBrowser.ui.main.history.HistoryViewModel
import com.myAllVideoBrowser.ui.main.home.MainActivity
import com.myAllVideoBrowser.ui.main.home.MainViewModel
import com.myAllVideoBrowser.ui.main.home.browser.detectedVideos.GlobalVideoDetectionModel
import com.myAllVideoBrowser.ui.main.home.browser.homeTab.BrowserHomeFragment
import com.myAllVideoBrowser.ui.main.home.browser.tabsOverview.TabsOverviewFragment
import com.myAllVideoBrowser.ui.main.home.browser.webTab.WebTab
import com.myAllVideoBrowser.ui.main.home.browser.webTab.WebTabFactory
import com.myAllVideoBrowser.ui.main.home.browser.webTab.WebTabFragment
import com.myAllVideoBrowser.ui.main.home.browser.webTab.WebTabPreviewCapture
import com.myAllVideoBrowser.ui.main.settings.SettingsViewModel
import com.myAllVideoBrowser.util.*
import com.myAllVideoBrowser.util.proxy_utils.CustomProxyController
import com.myAllVideoBrowser.util.proxy_utils.OkHttpProxyClient
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


interface BrowserServicesProvider : TabManagerProvider, PageTabProvider, HistoryProvider,
    WorkerEventProvider, CurrentTabIndexProvider, CurrentTabReplacementProvider

interface TabManagerProvider {
    fun getOpenTabEvent(): SingleLiveEvent<WebTab>

    fun getCloseTabEvent(): SingleLiveEvent<WebTab>

    fun getUpdateTabEvent(): SingleLiveEvent<WebTab>

    fun getTabsListChangeEvent(): ObservableField<List<WebTab>>
}

interface PageTabProvider {
    fun getPageTab(position: Int): WebTab
}

interface HistoryProvider {
    fun getHistoryVModel(): HistoryViewModel
}

interface WorkerEventProvider {
    fun getWorkerM3u8MpdEvent(): MutableLiveData<DownloadButtonState>

    fun getWorkerMP4Event(): MutableLiveData<DownloadButtonState>
}

interface CurrentTabIndexProvider {
    fun getCurrentTabIndex(): ObservableInt
}

interface CurrentTabReplacementProvider {
    fun replaceCurrentTab(webTab: WebTab)
}

interface BrowserListener {
    fun onBrowserHomeClicked()

    fun onTabsSwitcherClicked()

    fun onBrowserReloadClicked()

    fun onBrowserStopClicked()

    fun onBrowserBackClicked()

    fun onBrowserForwardClicked()
}

const val HOME_TAB_INDEX = 0

const val TAB_INDEX_KEY = "TAB_INDEX_KEY"
const val TABS_OVERVIEW_TAG = "tabs_overview"

//@OpenForTesting
class BrowserFragment : BaseFragment(), BrowserServicesProvider {

    companion object {
        fun newInstance() = BrowserFragment()
        var DESKTOP_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36"

        // TODO different agents for different androids
        var MOBILE_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 12; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Mobile Safari/537.36"
    }

    private lateinit var tabsAdapter: TabsFragmentStateAdapter

    private lateinit var drawerAdapter: WebTabsAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var mainActivity: MainActivity

    @Inject
    lateinit var appUtil: AppUtil

    @Inject
    lateinit var proxyController: CustomProxyController

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper

    @Inject
    lateinit var okHttpProxyClient: OkHttpProxyClient

    @VisibleForTesting
    internal lateinit var dataBinding: FragmentBrowserBinding

    private lateinit var browserViewModel: BrowserViewModel

    private lateinit var mainViewModel: MainViewModel

    private lateinit var historyModel: HistoryViewModel

    private lateinit var settingsModel: SettingsViewModel

    private lateinit var videoDetectionModel: GlobalVideoDetectionModel

    private val compositeDisposable = CompositeDisposable()

    private var backPressedOnce = false

    private val buttonStateCallback = object :
        Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            lifecycleScope.launch(Dispatchers.Main) {
                browserViewModel.workerM3u8MpdEvent.value =
                    videoDetectionModel.downloadButtonState.get()
            }
        }
    }

    private val currentTabCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            if (!isAdded) {
                return
            }
            lifecycleScope.launch(Dispatchers.Main) {
                if (::drawerAdapter.isInitialized) {
                    drawerAdapter.setSelectedTabIndex(browserViewModel.currentTab.get())
                }
                updateTabsOverviewHeader()
                persistBrowserTabSession()
            }
        }
    }

    private val tabsListCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            if (!isAdded) {
                return
            }
            lifecycleScope.launch(Dispatchers.Main) {
                updateTabsOverviewHeader()
                persistBrowserTabSession()
            }
        }
    }

    private val serviceWorkerClient = object : ServiceWorkerClient() {
        override fun shouldInterceptRequest(request: WebResourceRequest): WebResourceResponse? {
            val url = request.url.toString()

            val isM3u8Check = settingsModel.isCheckIfEveryRequestOnM3u8.get()
            val isMp4Check = settingsModel.getIsCheckEveryRequestOnMp4Video().get()
            val isCheckOnAudio = settingsModel.isCheckOnAudio.get()
            val isCheckOnImage = true

            if (isM3u8Check || isMp4Check || isCheckOnAudio || isCheckOnImage) {
                val requestWithCookies = request.let { resourceRequest ->
                    try {
                        CookieUtils.webResourceRequestToOkHttpRequest(
                            resourceRequest
                        )
                    } catch (_: Throwable) {
                        null
                    }
                }

                val contentType = VideoUtils.getContentTypeByUrl(
                    url, requestWithCookies?.headers, okHttpProxyClient
                )

                if (contentType == ContentType.MPD || contentType == ContentType.M3U8 || url.contains(
                        ".m3u8"
                    ) || url.contains(
                        ".mpd"
                    ) || url.contains(".txt")
                ) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        if (requestWithCookies != null && isM3u8Check) {
                            videoDetectionModel.verifyLinkStatus(requestWithCookies, "", true)
                        }
                    }
                } else if (
                    contentType == ContentType.VIDEO && isMp4Check ||
                    contentType == ContentType.AUDIO && isCheckOnAudio ||
                    contentType == ContentType.IMAGE && isCheckOnImage
                ) {
                    videoDetectionModel.checkRegularVideoOrAudio(
                        requestWithCookies,
                        isCheckOnAudio,
                        isMp4Check,
                        isCheckOnImage
                    )
                }
            }

            return super.shouldInterceptRequest(request)
        }
    }

    inner class TabsFragmentStateAdapter(private var webTabsRoutes: List<WebTab>) :
        FragmentStateAdapter(this) {
        fun setRoutes(newRoutes: List<WebTab>) {
            webTabsRoutes = newRoutes

            notifyDataSetChanged()
        }

        override fun getItemCount(): Int = webTabsRoutes.size

        override fun getItemId(position: Int): Long {
            return webTabsRoutes[position].id.hashCode().toLong()
        }

        override fun containsItem(itemId: Long): Boolean {
            val webTab = webTabsRoutes.find { it.id.hashCode().toLong() == itemId }
            return webTab != null
        }

        override fun createFragment(position: Int): Fragment {
            if (webTabsRoutes.getOrNull(position)?.isHome() == true) {
                return createHomeTabFragment()
            }

            return createTabFragment(position)
        }
    }

    private fun createHomeTabFragment(): Fragment {
        return BrowserHomeFragment.newInstance()
    }

    override fun getOpenTabEvent(): SingleLiveEvent<WebTab> {
        return browserViewModel.openPageEvent
    }

    override fun getCloseTabEvent(): SingleLiveEvent<WebTab> {
        return browserViewModel.closePageEvent
    }

    override fun getUpdateTabEvent(): SingleLiveEvent<WebTab> {
        return browserViewModel.updateWebTabEvent
    }

    override fun getTabsListChangeEvent(): ObservableField<List<WebTab>> {
        return browserViewModel.tabs
    }

    override fun getPageTab(position: Int): WebTab {
        val list = browserViewModel.tabs.get() ?: listOf(WebTab.HOME_TAB)
        if (position in list.indices) {
            return list[position]
        }
        return WebTab("error", "error")
    }

    private fun createTabFragment(index: Int): Fragment {
        val fragment = WebTabFragment.newInstance().apply {
            val args = Bundle().apply {
                putInt(TAB_INDEX_KEY, index)
            }
            arguments = args
        }

        return fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val swController = ServiceWorkerController.getInstance()
        swController.setServiceWorkerClient(serviceWorkerClient)
        swController.serviceWorkerWebSettings.allowContentAccess = true

        mainViewModel = mainActivity.mainViewModel
        browserViewModel = ViewModelProvider(this, viewModelFactory)[BrowserViewModel::class.java]
        historyModel = ViewModelProvider(this, viewModelFactory)[HistoryViewModel::class.java]
        videoDetectionModel =
            ViewModelProvider(this, viewModelFactory)[GlobalVideoDetectionModel::class.java]

        videoDetectionModel.settingsModel = mainActivity.settingsViewModel
        browserViewModel.settingsModel = mainActivity.settingsViewModel
        settingsModel = mainActivity.settingsViewModel

        mainActivity.mainViewModel.browserServicesProvider = this
        restoreBrowserTabSession(isColdStart = savedInstanceState == null)

        tabsAdapter = TabsFragmentStateAdapter(emptyList())

        drawerAdapter = WebTabsAdapter(emptyList(), tabsListener)

        val webTabsManagerLayout = GridLayoutManager(context, 2)

        val color = getThemeBackgroundColor()

        dataBinding = FragmentBrowserBinding.inflate(inflater, container, false).apply {
            this.viewPager.adapter = tabsAdapter
            this.viewPager.setSwipeThreshold(500)
            this.viewPager.setOnGoThroughListener(onGoThroughListener)
            this.viewPager.isUserInputEnabled = false
            this.tabsList.layoutManager = webTabsManagerLayout
            this.tabsList.adapter = drawerAdapter
            this.drawerLayoutContent.setBackgroundColor(color)
            this.drawerLayout.setScrimColor(Color.TRANSPARENT)
            this.newTabButton.setOnClickListener {
                browserViewModel.openPageEvent.value = WebTabFactory.createDefaultNewTab()
            }
            this.clearTabsButton.setOnClickListener {
                browserViewModel.clearAllTabsEvent.call()
            }

            this.viewModel = browserViewModel
        }

        browserViewModel.currentTab.addOnPropertyChangedCallback(currentTabCallback)
        browserViewModel.tabs.addOnPropertyChangedCallback(tabsListCallback)
        drawerAdapter.setSelectedTabIndex(browserViewModel.currentTab.get())
        updateTabsOverviewHeader()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }

        videoDetectionModel.downloadButtonState.addOnPropertyChangedCallback(buttonStateCallback)

        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        browserViewModel.start()
        handlePressWebTabEvent()
        handleOpenTabEvent()
        handleCloseWebTabEventEvent()
        handleClearAllTabsEvent()
        handleOpenNavDrawerEvent()
        handleUpdateWebTabEventEvent()
        handleReplaceCurrentTabEvent()
        checkIsPowerSaveMode()
    }

    override fun onDestroyView() {
        videoDetectionModel.downloadButtonState.removeOnPropertyChangedCallback(buttonStateCallback)
        browserViewModel.currentTab.removeOnPropertyChangedCallback(currentTabCallback)
        browserViewModel.tabs.removeOnPropertyChangedCallback(tabsListCallback)
        super.onDestroyView()
        browserViewModel.stop()
        videoDetectionModel.stop()
        compositeDisposable.clear()
    }

    override fun getHistoryVModel(): HistoryViewModel {
        return this.historyModel
    }

    override fun getWorkerM3u8MpdEvent(): MutableLiveData<DownloadButtonState> {
        return browserViewModel.workerM3u8MpdEvent
    }

    override fun getWorkerMP4Event(): MutableLiveData<DownloadButtonState> {
        return browserViewModel.workerMP4Event
    }

    override fun getCurrentTabIndex(): ObservableInt {
        return browserViewModel.currentTab
    }

    override fun replaceCurrentTab(webTab: WebTab) {
        browserViewModel.replaceCurrentTabEvent.value = webTab
    }

    private val tabsListener = object : WebTabsListener {
        override fun onCloseTabClicked(webTab: WebTab) {
            browserViewModel.closePageEvent.value = webTab
        }

        override fun onSelectTabClicked(webTab: WebTab) {
            browserViewModel.selectWebTabEvent.value = webTab
        }
    }

    private fun handlePressWebTabEvent() {
        browserViewModel.selectWebTabEvent.observe(viewLifecycleOwner) { webTab ->
            applyBrowserTabSession(
                BrowserTabSessionManager.select(
                    currentBrowserTabSession(),
                    webTab.id
                )
            )
            dataBinding.drawerLayout.close()
        }
    }

    // TODO: Show dialog with variants: "Open in New Tab", "Load in Current Tab", "Block", "Don't show again"
    private fun handleOpenTabEvent() {
        browserViewModel.openPageEvent.observe(viewLifecycleOwner) { webTab ->
            applyBrowserTabSession(
                BrowserTabSessionManager.open(
                    currentBrowserTabSession(),
                    webTab
                )
            )
            dataBinding.drawerLayout.close()
        }
    }

    private fun handleCloseWebTabEventEvent() {
        browserViewModel.closePageEvent.observe(viewLifecycleOwner) { webTab ->
            applyBrowserTabSession(
                BrowserTabSessionManager.close(
                    currentBrowserTabSession(),
                    webTab.id
                )
            )
        }
    }

    private fun handleUpdateWebTabEventEvent() {
        browserViewModel.updateWebTabEvent.observe(viewLifecycleOwner) { webTab ->
            applyBrowserTabSession(
                BrowserTabSessionManager.update(
                    currentBrowserTabSession(),
                    webTab
                )
            )
        }
    }

    private fun handleReplaceCurrentTabEvent() {
        browserViewModel.replaceCurrentTabEvent.observe(viewLifecycleOwner) { webTab ->
            applyBrowserTabSession(
                BrowserTabSessionManager.replaceCurrent(
                    currentBrowserTabSession(),
                    webTab
                )
            )
            dataBinding.drawerLayout.close()
        }
    }

    private fun handleClearAllTabsEvent() {
        browserViewModel.clearAllTabsEvent.observe(viewLifecycleOwner) {
            applyBrowserTabSession(BrowserTabSessionManager.clear())
        }
    }

    private fun handleOpenNavDrawerEvent() {
        mainViewModel.openNavDrawerEvent.observe(viewLifecycleOwner) {
            if (isTabsOverviewVisible()) {
                mainActivity.supportFragmentManager.popBackStack()
                return@observe
            }
            publishSelectedTabPreviewSnapshot()
            mainActivity.supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(
                    R.id.fragment_container_view,
                    TabsOverviewFragment.newInstance(),
                    TABS_OVERVIEW_TAG
                )
                .addToBackStack(TABS_OVERVIEW_TAG)
                .commit()
        }
    }

    private fun isTabsOverviewVisible(): Boolean {
        return mainActivity.supportFragmentManager.findFragmentByTag(TABS_OVERVIEW_TAG)?.isVisible == true
    }

    private fun publishSelectedTabPreviewSnapshot() {
        val selectedIndex = browserViewModel.currentTab.get()
        val currentTab = browserViewModel.tabs.get()
            ?.getOrNull(selectedIndex)
            ?.takeIf { !it.isHome() }
            ?: return
        val currentWebView = currentTab.getWebView() ?: return

        browserViewModel.updateWebTabEvent.value = WebTab(
            url = currentWebView.url ?: currentTab.getUrl(),
            title = currentWebView.title ?: currentTab.getTitle(),
            iconBytes = currentWebView.favicon ?: currentTab.getFavicon(),
            previewBytes = WebTabPreviewCapture.capture(currentWebView) ?: currentTab.getPreviewBytes(),
            headers = currentTab.getHeaders() ?: emptyMap(),
            webview = currentWebView,
            id = currentTab.id
        )
    }

    private fun checkIsPowerSaveMode() {
        val context = this.requireContext()
        val pwManager = getSystemService(context, PowerManager::class.java)
        if (pwManager?.isPowerSaveMode == true) {
            MaterialAlertDialogBuilder(context).setTitle(R.string.warning)
                .setMessage(R.string.powerSave).setPositiveButton(
                    R.string.ok
                ) { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }
    }

    private fun onBackPressed() {
        if (mainActivity.supportFragmentManager.backStackEntryCount > 0) {
            mainActivity.supportFragmentManager.popBackStack()
            return
        }
        if (dataBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            dataBinding.drawerLayout.close()
            return
        }
        val rootPagerIndex = mainActivity.mainViewModel.currentItem.get() ?: 0
        if (rootPagerIndex > 0) {
            mainActivity.mainViewModel.currentItem.set(HOME_TAB_INDEX)
        }
        if (rootPagerIndex == HOME_TAB_INDEX) {
            if (backPressedOnce) {
                requireActivity().finish()
                return
            }

            backPressedOnce = true
            Toast.makeText(
                requireContext(),
                R.string.browser_back_exit_hint,
                Toast.LENGTH_SHORT
            ).show()

            Handler(Looper.getMainLooper()).postDelayed({
                backPressedOnce = false
            }, 2000)
        }
    }

    private val onGoThroughListener = object : OnGoThroughListener {
        override fun onRightGoThrough() {
            val currentTabIndex = browserViewModel.currentTab.get()
            val currentTab = browserViewModel.tabs.get()?.getOrNull(currentTabIndex)
            if (currentTab?.isHome() == true) {
                mainViewModel.currentItem.set((mainViewModel.currentItem.get() ?: 0) + 1)
            }
        }
    }

    private fun restoreBrowserTabSession(isColdStart: Boolean) {
        val session = BrowserTabSessionManager.sessionForLaunch(
            restoredSession = sharedPrefHelper.getBrowserTabSession(),
            isColdStart = isColdStart
        )
        browserViewModel.tabs.set(BrowserTabSessionManager.toRuntimeTabs(session))
        browserViewModel.currentTab.set(session.currentTabIndex)
    }

    private fun currentBrowserTabSession(): BrowserTabSession {
        return BrowserTabSessionManager.fromRuntimeTabs(
            tabs = browserViewModel.tabs.get(),
            currentTabIndex = browserViewModel.currentTab.get()
        )
    }

    private fun applyBrowserTabSession(session: BrowserTabSession) {
        val safeSession = BrowserTabSessionManager.sanitize(session.tabs, session.currentTabIndex)
        browserViewModel.tabs.set(BrowserTabSessionManager.toRuntimeTabs(safeSession))
        browserViewModel.currentTab.set(safeSession.currentTabIndex)
        dataBinding.viewPager.currentItem = safeSession.currentTabIndex
        persistBrowserTabSession()
        updateTabsOverviewHeader()
    }

    private fun persistBrowserTabSession() {
        if (!::browserViewModel.isInitialized) {
            return
        }
        sharedPrefHelper.saveBrowserTabSession(currentBrowserTabSession())
    }

    private fun updateTabsOverviewHeader() {
        if (!::dataBinding.isInitialized) {
            return
        }
        val tabCount = browserViewModel.tabs.get()?.size ?: 1
        dataBinding.tabsOverviewTitle.text = resources.getQuantityString(
            R.plurals.browser_tabs_overview_title,
            tabCount,
            tabCount
        )
        val canClear = tabCount > 1
        dataBinding.clearTabsButton.isEnabled = canClear
        dataBinding.clearTabsButton.alpha = if (canClear) 1f else 0.38f
    }
}
