package com.myAllVideoBrowser.ui.main.settings.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.DialogSettingsPositiveReviewBinding

class PositiveReviewDialogFragment : BaseSettingsDialogFragment() {
    private lateinit var binding: DialogSettingsPositiveReviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_settings_positive_review, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.icon.setImageResource(R.drawable.security_question_check_24px)
        binding.title.text = getString(R.string.settings_dialog_positive_review_title)
        binding.message.text = getString(R.string.settings_dialog_positive_review_message)
        binding.primaryButton.text = getString(R.string.settings_dialog_leave_review)
        binding.secondaryButton.text = getString(R.string.settings_dialog_done)
        binding.ratingBar.rating = 5f
        binding.ratingBar.setIsIndicator(true)

        binding.primaryButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                SettingsDialogResults.RESULT_KEY,
                bundleOf(SettingsDialogResults.RESULT_ACTION to SettingsDialogResults.ACTION_OPEN_REVIEW)
            )
            dismissAllowingStateLoss()
        }

        binding.secondaryButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                SettingsDialogResults.RESULT_KEY,
                bundleOf(SettingsDialogResults.RESULT_ACTION to SettingsDialogResults.ACTION_DONE)
            )
            dismissAllowingStateLoss()
        }
    }

    companion object {
        const val TAG = "positive_review_dialog"

        fun newInstance() = PositiveReviewDialogFragment()
    }
}
