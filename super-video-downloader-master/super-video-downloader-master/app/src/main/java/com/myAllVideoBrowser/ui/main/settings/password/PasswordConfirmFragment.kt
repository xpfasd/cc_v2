package com.myAllVideoBrowser.ui.main.settings.password

import android.os.Bundle
import androidx.core.view.isVisible
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.util.SharedPrefHelper
import javax.inject.Inject

class PasswordConfirmFragment : PasswordPinFragmentBase() {

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper

    private var expectedPin: String = ""
    private var openPrivateSpaceOnFinish: Boolean = false
    private var resetPinOnly: Boolean = false

    override val screenTitle: String
        get() = if (resetPinOnly) "Reset Password" else "Set Password"
    override val screenPrompt: String = "Please re-enter your PIN code"
    override val screenSubtitle: String = "Please re-enter your PIN code to confirm"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        expectedPin = arguments?.getString(ARG_PIN).orEmpty()
        openPrivateSpaceOnFinish = arguments?.getBoolean(ARG_OPEN_PRIVATE_SPACE_ON_FINISH) == true
        resetPinOnly = arguments?.getBoolean(ARG_RESET_PIN_ONLY) == true
    }

    override fun onPinCompleted(pin: String) {
        if (pin == expectedPin) {
            if (resetPinOnly) {
                sharedPrefHelper.savePasswordPin(pin)
                PasswordSuccessDialogFragment.newInstance(openPrivateSpaceOnFinish)
                    .show(requireActivity().supportFragmentManager, PASSWORD_SUCCESS_DIALOG_TAG)
            } else {
                PasswordFlowNavigator.goToSecurityQuestion(
                    requireActivity().supportFragmentManager,
                    pin,
                    openPrivateSpaceOnFinish
                )
            }
            return
        }

        binding.errorText.isVisible = true
        binding.errorText.text = getString(R.string.password_pin_mismatch)
        clearPin()
    }

    override fun onPinChanged() {
        binding.errorText.isVisible = false
    }

    companion object {
        private const val ARG_PIN = "arg_pin"
        private const val ARG_OPEN_PRIVATE_SPACE_ON_FINISH = "arg_open_private_space_on_finish"
        private const val ARG_RESET_PIN_ONLY = "arg_reset_pin_only"

        fun newInstance(
            pin: String,
            openPrivateSpaceOnFinish: Boolean = false,
            resetPinOnly: Boolean = false
        ) =
            PasswordConfirmFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PIN, pin)
                    putBoolean(ARG_OPEN_PRIVATE_SPACE_ON_FINISH, openPrivateSpaceOnFinish)
                    putBoolean(ARG_RESET_PIN_ONLY, resetPinOnly)
                }
            }
    }
}
