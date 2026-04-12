package com.myAllVideoBrowser.ui.main.settings.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.DialogSettingsPermissionBinding

class PermissionDialogFragment : BaseSettingsDialogFragment() {
    private lateinit var binding: DialogSettingsPermissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_settings_permission, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.icon.setImageResource(R.drawable.verified_user_24px)
        binding.title.text = getString(R.string.settings_dialog_permission_title)
        binding.message.text = getString(R.string.settings_dialog_permission_message)
        binding.primaryButton.text = getString(R.string.settings_dialog_allow)
        binding.secondaryButton.text = getString(R.string.settings_dialog_block)

        binding.primaryButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                SettingsDialogResults.RESULT_KEY,
                bundleOf(SettingsDialogResults.RESULT_ACTION to SettingsDialogResults.ACTION_ALLOW)
            )
            dismissAllowingStateLoss()
        }

        binding.secondaryButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                SettingsDialogResults.RESULT_KEY,
                bundleOf(SettingsDialogResults.RESULT_ACTION to SettingsDialogResults.ACTION_BLOCK)
            )
            dismissAllowingStateLoss()
        }
    }

    companion object {
        const val TAG = "permission_dialog"

        fun newInstance() = PermissionDialogFragment()
    }
}

