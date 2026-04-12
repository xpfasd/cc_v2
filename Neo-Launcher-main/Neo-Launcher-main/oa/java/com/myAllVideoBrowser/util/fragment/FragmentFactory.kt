package com.myAllVideoBrowser.util.fragment

import androidx.fragment.app.Fragment
import com.myAllVideoBrowser.ui.main.history.HistoryFragment
import com.myAllVideoBrowser.ui.main.home.browser.BrowserFragment
import com.myAllVideoBrowser.ui.main.home.browser.homeTab.BrowserHomeFragment
import com.myAllVideoBrowser.ui.main.home.browser.webTab.WebTabFragment
import com.myAllVideoBrowser.ui.main.home.browser.detectedVideos.DetectedVideosTabFragment
import com.myAllVideoBrowser.ui.main.progress.ProgressFragment
import com.myAllVideoBrowser.ui.main.settings.SettingsFragment
import com.myAllVideoBrowser.ui.main.settings.password.PasswordConfirmFragment
import com.myAllVideoBrowser.ui.main.settings.password.PasswordSetFragment
import com.myAllVideoBrowser.ui.main.settings.password.PasswordSuccessDialogFragment
import com.myAllVideoBrowser.ui.main.settings.password.SecurityQuestionDialogFragment
import com.myAllVideoBrowser.ui.main.settings.password.SecurityQuestionFragment
import com.myAllVideoBrowser.ui.main.video.VideoFragment
import javax.inject.Inject

interface FragmentFactory {
    fun createBrowserFragment(): Fragment
    fun createProgressFragment(): Fragment
    fun createVideoFragment(): Fragment
    fun createSettingsFragment(): Fragment
    fun createPasswordSetFragment(): Fragment
    fun createPasswordConfirmFragment(pin: String): Fragment
    fun createSecurityQuestionFragment(pin: String): Fragment
    fun createSecurityQuestionDialogFragment(selectedQuestion: String): Fragment
    fun createPasswordSuccessDialogFragment(): Fragment
    fun createHistoryFragment(): Fragment

    fun createBrowserHomeFragment(): Fragment

    fun createWebTabFragment(): Fragment

    fun createDetectedVideosTabFragment(): Fragment
}

class FragmentFactoryImpl @Inject constructor() : FragmentFactory {
    override fun createBrowserFragment() = BrowserFragment.newInstance()

    override fun createProgressFragment() = ProgressFragment.newInstance()

    override fun createVideoFragment() = VideoFragment.newInstance()

    override fun createSettingsFragment() = SettingsFragment.newInstance()

    override fun createPasswordSetFragment() = PasswordSetFragment.newInstance()

    override fun createPasswordConfirmFragment(pin: String) = PasswordConfirmFragment.newInstance(pin)

    override fun createSecurityQuestionFragment(pin: String) = SecurityQuestionFragment.newInstance(pin)

    override fun createSecurityQuestionDialogFragment(selectedQuestion: String) =
        SecurityQuestionDialogFragment.newInstance(selectedQuestion)

    override fun createPasswordSuccessDialogFragment() = PasswordSuccessDialogFragment.newInstance()

    override fun createHistoryFragment() = HistoryFragment.newInstance()

    override fun createBrowserHomeFragment() = BrowserHomeFragment.newInstance()

    override fun createWebTabFragment() = WebTabFragment.newInstance()

    override fun createDetectedVideosTabFragment() = DetectedVideosTabFragment.newInstance()
}
