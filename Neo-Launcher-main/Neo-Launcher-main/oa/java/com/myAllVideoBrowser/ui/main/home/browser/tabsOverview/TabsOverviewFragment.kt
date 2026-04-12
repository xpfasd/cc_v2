package com.myAllVideoBrowser.ui.main.home.browser.tabsOverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.Observable
import androidx.recyclerview.widget.GridLayoutManager
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.FragmentTabsOverviewBinding
import com.myAllVideoBrowser.ui.component.adapter.WebTabsAdapter
import com.myAllVideoBrowser.ui.component.adapter.WebTabsListener
import com.myAllVideoBrowser.ui.main.base.BaseFragment
import com.myAllVideoBrowser.ui.main.home.browser.BrowserViewModel
import com.myAllVideoBrowser.ui.main.home.browser.webTab.WebTab
import com.myAllVideoBrowser.ui.main.home.browser.webTab.WebTabFactory

class TabsOverviewFragment : BaseFragment() {

    companion object {
        fun newInstance() = TabsOverviewFragment()
    }

    private lateinit var binding: FragmentTabsOverviewBinding
    private lateinit var tabsAdapter: WebTabsAdapter

    private val browserViewModel: BrowserViewModel
        get() = requireNotNull(BrowserViewModel.instance) {
            "BrowserViewModel is not ready"
        }

    private val tabsListCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            scheduleRenderTabs()
        }
    }

    private val currentTabCallback = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            scheduleRenderTabs()
        }
    }

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            dismissOverview()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTabsOverviewBinding.inflate(inflater, container, false)
        tabsAdapter = WebTabsAdapter(emptyList(), tabsListener)

        binding.tabsList.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.tabsList.adapter = tabsAdapter
        binding.backButton.setOnClickListener {
            dismissOverview()
        }
        binding.newTabButton.setOnClickListener {
            browserViewModel.openPageEvent.value = WebTabFactory.createDefaultNewTab()
            dismissOverview()
        }
        binding.clearTabsButton.setOnClickListener {
            browserViewModel.clearAllTabsEvent.call()
            dismissOverview()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
        scheduleRenderTabs()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        browserViewModel.tabs.addOnPropertyChangedCallback(tabsListCallback)
        browserViewModel.currentTab.addOnPropertyChangedCallback(currentTabCallback)
        scheduleRenderTabs()
    }

    override fun onDestroyView() {
        browserViewModel.tabs.removeOnPropertyChangedCallback(tabsListCallback)
        browserViewModel.currentTab.removeOnPropertyChangedCallback(currentTabCallback)
        super.onDestroyView()
    }

    private fun renderTabs() {
        if (!this::binding.isInitialized) {
            return
        }

        val tabs = browserViewModel.tabs.get().orEmpty()
        val currentTabIndex = browserViewModel.currentTab.get()

        tabsAdapter.setData(tabs)
        tabsAdapter.setSelectedTabIndex(currentTabIndex)

        binding.tabsCountBadge.text = tabs.size.coerceAtLeast(1).toString()
        binding.clearTabsButton.isEnabled = tabs.size > 1
        binding.clearTabsButton.alpha = if (tabs.size > 1) 1f else 0.38f
    }

    private fun scheduleRenderTabs() {
        if (!this::binding.isInitialized) {
            return
        }
        binding.root.post {
            if (this::binding.isInitialized) {
                renderTabs()
            }
        }
    }

    private fun dismissOverview() {
        if (parentFragmentManager.backStackEntryCount > 0) {
            parentFragmentManager.popBackStack()
        }
    }

    private val tabsListener = object : WebTabsListener {
        override fun onCloseTabClicked(webTab: WebTab) {
            browserViewModel.closePageEvent.value = webTab
        }

        override fun onSelectTabClicked(webTab: WebTab) {
            browserViewModel.selectWebTabEvent.value = webTab
            dismissOverview()
        }
    }
}
