package com.myAllVideoBrowser.ui.main.home.browser.homeTab

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.databinding.Observable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cc.ads.topon.TopOnAdSceneManager
import com.cc.ads.topon.TopOnAdScenes
import com.myAllVideoBrowser.data.local.model.Suggestion
import com.myAllVideoBrowser.data.local.room.entity.PageInfo
import com.myAllVideoBrowser.databinding.FragmentBrowserHomeBinding
import com.myAllVideoBrowser.ui.component.adapter.SuggestionAdapter
import com.myAllVideoBrowser.ui.component.adapter.SuggestionListener
import com.myAllVideoBrowser.ui.component.adapter.TopPageAdapter
import com.myAllVideoBrowser.ui.main.guide.GuideActivity
import com.myAllVideoBrowser.ui.main.home.MainViewModel
import com.myAllVideoBrowser.ui.main.home.browser.BaseWebTabFragment
import com.myAllVideoBrowser.ui.main.home.browser.TabManagerProvider
import com.myAllVideoBrowser.ui.main.home.browser.webTab.WebTabFactory
import com.myAllVideoBrowser.util.AppUtil
import kotlinx.coroutines.launch
import javax.inject.Inject

class BrowserHomeFragment : BaseWebTabFragment() {
    companion object {
        private const val TAG = "BrowserHomeFragment"

        fun newInstance() = BrowserHomeFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appUtil: AppUtil

    lateinit var binding: FragmentBrowserHomeBinding

    private lateinit var openPageIProvider: TabManagerProvider

    private lateinit var homeViewModel: BrowserHomeViewModel

    private lateinit var mainViewModel: MainViewModel

    private lateinit var topPageAdapter: TopPageAdapter

    private lateinit var suggestionAdapter: SuggestionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel = mainActivity.mainViewModel
        homeViewModel = ViewModelProvider(this, viewModelFactory)[BrowserHomeViewModel::class.java]
        openPageIProvider = mainActivity.mainViewModel.browserServicesProvider!!

        topPageAdapter = TopPageAdapter(requireContext(), emptyList(), itemListener)
        suggestionAdapter = SuggestionAdapter(requireContext(), emptyList(), suggestionListener)

        binding = FragmentBrowserHomeBinding.inflate(inflater, container, false).apply {
            this.viewModel = homeViewModel
            this.mainVModel = mainViewModel
            this.topPagesGrid.adapter = topPageAdapter

            this.homeEtSearch.setAdapter(suggestionAdapter)
            this.homeEtSearch.addTextChangedListener(onInputHomeSearchChangeListener)
            this.homeEtSearch.imeOptions = EditorInfo.IME_ACTION_DONE
            this.homeEtSearch.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    this.homeEtSearch.clearFocus()
                    viewModel?.viewModelScope?.launch {
                        val inputText = (this@apply.homeEtSearch as EditText).text.toString()
                        this@apply.homeEtSearch.text.clear()
                        openNewTab(inputText)
                    }
                    false
                } else false
            }
            this.infoButton.setOnClickListener {
                openGuide()
            }
            this.tabCountContainer.setOnClickListener {
                mainViewModel.openNavDrawerEvent.call()
            }
            this.bookmarkShortcut.setOnClickListener {
                navigateToBookMarks()
            }
            this.historyShortcut.setOnClickListener {
                navigateToHistory()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.start()
        openPageIProvider.getTabsListChangeEvent().addOnPropertyChangedCallback(tabCountCallback)
        updateTabCount()
        val openingUrl = mainViewModel.openedUrl.get()
        val openingText = mainViewModel.openedText.get()

        if (openingUrl != null) {
            openNewTab(openingUrl)
            mainViewModel.openedUrl.set(null)
        }

        if (openingText != null) {
            openNewTab(openingText)
            mainViewModel.openedText.set(null)
        }

        Log.d(TAG, "HOME_TOP_NATIVE preload requested")
        TopOnAdSceneManager.preloadNative(requireContext().applicationContext, TopOnAdScenes.HOME_TOP_NATIVE)
        binding.homeTopNativeAdContainer.post {
            Log.d(TAG, "HOME_TOP_NATIVE render requested")
            TopOnAdSceneManager.renderNativeInto(
                binding.homeTopNativeAdContainer,
                TopOnAdScenes.HOME_TOP_NATIVE
            )
        }
    }

    // Bug fix for not updating home page grid after adding new bookmark
    override fun onResume() {
        super.onResume()
        val bookmarksList = mainViewModel.bookmarksList.get()?.toMutableList()
        mainViewModel.bookmarksList.set(bookmarksList)
        updateTabCount()
    }

    override fun onDestroyView() {
        openPageIProvider.getTabsListChangeEvent().removeOnPropertyChangedCallback(tabCountCallback)
        super.onDestroyView()
    }

    private val suggestionListener = object : SuggestionListener {
        override fun onItemClicked(suggestion: Suggestion) {
            openNewTab(suggestion.content)
        }
    }

    private fun openNewTab(input: String) {
        if (input.isNotEmpty()) {
            openPageIProvider.getOpenTabEvent().value = WebTabFactory.createWebTabFromInput(input)
        }
    }

    private val onInputHomeSearchChangeListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            val input = s.toString()
            homeViewModel.searchTextInput.set(input)
            if (!(input.startsWith("http://") || input.startsWith("https://"))) {
                homeViewModel.showSuggestions()
            }
            homeViewModel.homePublishSubject.onNext(input)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }

    private val itemListener = object : TopPageAdapter.TopPagesListener {
        override fun onItemClicked(pageInfo: PageInfo) {
            openNewTab(pageInfo.link)
        }
    }

    private fun openGuide() {
        startActivity(GuideActivity.createIntent(requireContext()))
    }

    private val tabCountCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            updateTabCount()
        }
    }

    private fun updateTabCount() {
        val count = openPageIProvider.getTabsListChangeEvent().get()?.size ?: 1
        binding.tvTabCount.text = count.toString()
    }

    override fun shareWebLink() {}

    override fun bookmarkCurrentUrl() {}
}
