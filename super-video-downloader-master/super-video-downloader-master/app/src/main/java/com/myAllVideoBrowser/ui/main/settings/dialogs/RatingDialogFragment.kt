package com.myAllVideoBrowser.ui.main.settings.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.DialogSettingsRatingBinding

class RatingDialogFragment : BaseSettingsDialogFragment() {
    private lateinit var binding: DialogSettingsRatingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_settings_rating, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.icon.setImageResource(R.drawable.ic_star_border_gray_24dp)
        binding.title.text = getString(R.string.settings_dialog_rating_title)
        binding.message.text = getString(R.string.settings_dialog_rating_message)
        binding.primaryButton.text = getString(R.string.settings_dialog_rating_continue)
        binding.secondaryButton.text = getString(R.string.settings_dialog_later)

        binding.ratingBar.rating = 0f
        binding.primaryButton.isEnabled = false

        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            binding.primaryButton.isEnabled = rating > 0f
        }

        binding.primaryButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                SettingsDialogResults.RESULT_KEY,
                bundleOf(
                    SettingsDialogResults.RESULT_ACTION to SettingsDialogResults.ACTION_RATE_NOW,
                    SettingsDialogResults.RESULT_RATING to binding.ratingBar.rating
                )
            )
            dismissAllowingStateLoss()
        }

        binding.secondaryButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                SettingsDialogResults.RESULT_KEY,
                bundleOf(SettingsDialogResults.RESULT_ACTION to SettingsDialogResults.ACTION_LATER)
            )
            dismissAllowingStateLoss()
        }
    }

    companion object {
        const val TAG = "rating_dialog"

        fun newInstance() = RatingDialogFragment()
    }
}

