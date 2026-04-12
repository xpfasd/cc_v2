package com.myAllVideoBrowser.ui.main.home.browser

import android.app.Activity
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ShareCompat
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.ui.main.base.BaseFragment
import com.myAllVideoBrowser.ui.main.bookmarks.BookmarksActivity
import com.myAllVideoBrowser.ui.main.help.HelpFragment
import com.myAllVideoBrowser.ui.main.history.HistoryActivity
import com.myAllVideoBrowser.ui.main.home.MainActivity
import com.myAllVideoBrowser.ui.main.proxies.ProxiesFragment
import com.myAllVideoBrowser.ui.main.settings.SettingsFragment
import com.myAllVideoBrowser.ui.main.home.browser.webTab.WebTab
import com.myAllVideoBrowser.util.AppLogger
import javax.inject.Inject
abstract class BaseWebTabFragment : BaseFragment() {
    @Inject
    lateinit var mainActivity: MainActivity

    private val bookmarkSelectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleSelectionResult(result)
        }

    private val historySelectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleSelectionResult(result)
        }

    abstract fun shareWebLink()

    abstract fun bookmarkCurrentUrl()

    protected fun navigateToHistory() {
        historySelectionLauncher.launch(HistoryActivity.createIntent(requireContext()))
    }

    fun shareLink(url: String?) {
        ShareCompat.IntentBuilder(mainActivity).setType("text/plain").setChooserTitle("Share Link")
            .setText(url).startChooser()
    }

    private fun navigateToSettings() {
        try {
            val currentFragment = this
            val activityFragmentContainer =
                currentFragment.activity?.findViewById<FragmentContainerView>(R.id.fragment_container_view)
            activityFragmentContainer?.let {
                val transaction =
                    currentFragment.requireActivity().supportFragmentManager.beginTransaction()
                transaction.add(it.id, SettingsFragment.newInstance())
                transaction.addToBackStack("settings")
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                transaction.commit()
            }
        } catch (e: ClassCastException) {
            AppLogger.d("Can't get the fragment manager with this")
        }
    }

    private fun navigateToProxies() {
        try {
            val currentFragment = this
            val activityFragmentContainer =
                currentFragment.activity?.findViewById<FragmentContainerView>(R.id.fragment_container_view)
            activityFragmentContainer?.let {
                val transaction =
                    currentFragment.requireActivity().supportFragmentManager.beginTransaction()
                transaction.add(it.id, ProxiesFragment.newInstance())
                transaction.addToBackStack("proxies")
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                transaction.commit()
            }
        } catch (e: ClassCastException) {
            AppLogger.d("Can't get the fragment manager with this")
        }
    }

    fun navigateToHelp() {
        try {
            val currentFragment = this
            val activityFragmentContainer =
                currentFragment.activity?.findViewById<FragmentContainerView>(R.id.fragment_container_view)
            activityFragmentContainer?.let {
                val transaction =
                    currentFragment.requireActivity().supportFragmentManager.beginTransaction()
                transaction.add(it.id, HelpFragment.newInstance())
                transaction.addToBackStack("help")
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                transaction.commit()
            }
        } catch (e: ClassCastException) {
            AppLogger.d("Can't get the fragment manager with this")
        }
    }

    protected fun navigateToBookMarks() {
        bookmarkSelectionLauncher.launch(BookmarksActivity.createIntent(requireContext()))
    }

    private fun handleSelectionResult(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) {
            return
        }

        val data = result.data ?: return
        val request = BrowserSelectionResultContract.toRequest(
            data.getStringExtra(BrowserSelectionResultContract.EXTRA_SELECTED_URL),
            data.getStringExtra(BrowserSelectionResultContract.EXTRA_SELECTED_TITLE)
        ) ?: return

        mainActivity.mainViewModel.browserServicesProvider?.replaceCurrentTab(
            WebTab(
                url = request.url,
                title = request.title,
                headers = emptyMap()
            )
        )
    }
}
