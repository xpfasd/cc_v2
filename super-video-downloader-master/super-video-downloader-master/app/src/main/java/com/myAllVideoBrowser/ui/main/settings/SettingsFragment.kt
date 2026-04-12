package com.myAllVideoBrowser.ui.main.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.FragmentSettingsBinding
import com.myAllVideoBrowser.ui.main.base.BaseFragment
import com.myAllVideoBrowser.ui.main.home.MainActivity
import com.myAllVideoBrowser.ui.main.settings.dialogs.SettingsDialogLauncher
import com.myAllVideoBrowser.ui.main.settings.dialogs.SettingsDialogResults
import com.myAllVideoBrowser.ui.main.settings.language.LanguageSettingsActivity
import com.myAllVideoBrowser.util.FileUtil
import com.myAllVideoBrowser.util.IntentUtil
import com.myAllVideoBrowser.util.SharedPrefHelper
import javax.inject.Inject

class SettingsFragment : BaseFragment() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    @Inject
    lateinit var fileUtil: FileUtil

    @Inject
    lateinit var intentUtil: IntentUtil

    @Inject
    lateinit var mainActivity: MainActivity

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper

    private lateinit var dataBinding: FragmentSettingsBinding
    private lateinit var settingsViewModel: SettingsViewModel

    private val storagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.any { it }) {
                openDownloadLocation()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsViewModel = mainActivity.settingsViewModel
        dataBinding = FragmentSettingsBinding.inflate(inflater, container, false)
        dataBinding.viewModel = settingsViewModel
        dataBinding.lifecycleOwner = viewLifecycleOwner
        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDialogResultListener()
        handleUiEvents()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            parentFragmentManager.popBackStack()
        }

        settingsViewModel.start()
    }

    override fun onDestroyView() {
        settingsViewModel.stop()
        super.onDestroyView()
    }

    private fun handleUiEvents() {
        settingsViewModel.downloadLocationClickEvent.observe(viewLifecycleOwner) {
            handleDownloadLocationClick()
        }
        settingsViewModel.languageClickEvent.observe(viewLifecycleOwner) {
            startActivity(
                LanguageSettingsActivity.createIntent(
                    requireContext(),
                    sharedPrefHelper.getSelectedLanguageTag()
                )
            )
        }
        settingsViewModel.scoreClickEvent.observe(viewLifecycleOwner) {
            SettingsDialogLauncher.showRatingDialog(parentFragmentManager)
        }
        settingsViewModel.shareClickEvent.observe(viewLifecycleOwner) {
            shareApp()
        }
        settingsViewModel.privacyPolicyClickEvent.observe(viewLifecycleOwner) {
            startActivity(PrivacyPolicyActivity.createIntent(requireContext()))
        }
    }

    private fun setupDialogResultListener() {
        parentFragmentManager.setFragmentResultListener(
            SettingsDialogResults.RESULT_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            when (bundle.getString(SettingsDialogResults.RESULT_ACTION)) {
                SettingsDialogResults.ACTION_ALLOW -> requestDownloadLocationPermissions()
                SettingsDialogResults.ACTION_RATE_NOW -> handleRatingResult(
                    bundle.getFloat(SettingsDialogResults.RESULT_RATING, 0f)
                )
                SettingsDialogResults.ACTION_SEND -> sendFeedback(
                    bundle.getString(SettingsDialogResults.RESULT_FEEDBACK).orEmpty()
                )
                SettingsDialogResults.ACTION_OPEN_REVIEW -> openReviewPage()
            }
        }
    }

    private fun handleDownloadLocationClick() {
        if (hasDownloadLocationPermissions()) {
            openDownloadLocation()
        } else {
            SettingsDialogLauncher.showPermissionDialog(parentFragmentManager)
        }
    }

    private fun requestDownloadLocationPermissions() {
        storagePermissionLauncher.launch(requiredStoragePermissions())
    }

    private fun hasDownloadLocationPermissions(): Boolean {
        return requiredStoragePermissions().all { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) ==
                PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requiredStoragePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun openDownloadLocation() {
        intentUtil.openVideoFolder(context, fileUtil.folderDir.path)
    }

    private fun handleRatingResult(rating: Float) {
        if (rating >= 4f) {
            SettingsDialogLauncher.showPositiveReviewDialog(parentFragmentManager)
        } else {
            SettingsDialogLauncher.showFeedbackDialog(parentFragmentManager)
        }
    }

    private fun openReviewPage() {
        openUrl(getString(R.string.settings_home_review_url))
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.settings_home_share_message))
        }
        startActivity(
            Intent.createChooser(
                shareIntent,
                getString(R.string.settings_home_share_chooser)
            )
        )
    }

    private fun sendFeedback(feedback: String) {
        val feedbackIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.settings_home_feedback_subject))
            putExtra(Intent.EXTRA_TEXT, feedback)
        }
        if (feedbackIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(
                Intent.createChooser(
                    feedbackIntent,
                    getString(R.string.settings_home_feedback_chooser)
                )
            )
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        }
    }
}
