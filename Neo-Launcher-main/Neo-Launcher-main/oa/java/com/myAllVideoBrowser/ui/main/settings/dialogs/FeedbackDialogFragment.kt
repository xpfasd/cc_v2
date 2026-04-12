package com.myAllVideoBrowser.ui.main.settings.dialogs

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.DialogSettingsFeedbackBinding

class FeedbackDialogFragment : BaseSettingsDialogFragment() {
    private lateinit var binding: DialogSettingsFeedbackBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_settings_feedback, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.icon.setImageResource(R.drawable.dialog_feedback_24px)
        binding.title.text = getString(R.string.settings_dialog_feedback_title)
        binding.message.text = getString(R.string.settings_dialog_feedback_message)
        binding.feedbackInputLayout.hint = getString(R.string.settings_dialog_feedback_hint)
        binding.primaryButton.text = getString(R.string.settings_dialog_send)
        binding.secondaryButton.text = getString(R.string.settings_dialog_cancel)
        binding.primaryButton.isEnabled = false

        binding.feedbackInput.doAfterTextChangedCompat { text ->
            binding.primaryButton.isEnabled = !text.isNullOrBlank()
        }

        binding.primaryButton.setOnClickListener {
            val feedback = binding.feedbackInput.text?.toString().orEmpty().trim()
            parentFragmentManager.setFragmentResult(
                SettingsDialogResults.RESULT_KEY,
                bundleOf(
                    SettingsDialogResults.RESULT_ACTION to SettingsDialogResults.ACTION_SEND,
                    SettingsDialogResults.RESULT_FEEDBACK to feedback
                )
            )
            dismissAllowingStateLoss()
        }

        binding.secondaryButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                SettingsDialogResults.RESULT_KEY,
                bundleOf(SettingsDialogResults.RESULT_ACTION to SettingsDialogResults.ACTION_DISMISS)
            )
            dismissAllowingStateLoss()
        }

        dialog?.setOnShowListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.feedbackInput.requestFocus()
                showKeyboard(binding.feedbackInput.context)
            }, 200)
        }
    }

    private fun showKeyboard(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
        imm?.showSoftInput(binding.feedbackInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }

    companion object {
        const val TAG = "feedback_dialog"

        fun newInstance() = FeedbackDialogFragment()
    }
}

private inline fun com.google.android.material.textfield.TextInputEditText.doAfterTextChangedCompat(
    crossinline onChanged: (CharSequence?) -> Unit
) {
    addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: android.text.Editable?) {
            onChanged(s)
        }
    })
}

