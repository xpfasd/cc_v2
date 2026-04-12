package com.myAllVideoBrowser.ui.main.video

import androidx.core.view.isVisible
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.ui.main.settings.password.PasswordPinFragmentBase
import com.myAllVideoBrowser.util.SharedPrefHelper
import javax.inject.Inject

class PrivateSpacePinFragment : PasswordPinFragmentBase() {

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper

    override val screenTitle: String = "Private Space"
    override val screenPrompt: String = "Enter your PIN code"
    override val screenSubtitle: String =
        "Unlock your private photos and videos with the security code you set previously"
    override val secondaryActionText: String?
        get() = if (
            PrivateSpaceStartDestinationResolver.canRecoverPassword(
                hasSecurityQuestion = sharedPrefHelper.getSecurityQuestion().isNotBlank(),
                hasSecurityAnswer = sharedPrefHelper.hasSecurityAnswer()
            )
        ) {
            getString(R.string.private_space_forgot_password)
        } else {
            null
        }

    override fun onPinCompleted(pin: String) {
        if (sharedPrefHelper.verifyPasswordPin(pin)) {
            PrivateSpaceNavigator.finishAuthAndOpen(requireActivity().supportFragmentManager)
            return
        }

        binding.errorText.isVisible = true
        binding.errorText.text = getString(R.string.private_space_pin_invalid)
        clearPin()
    }

    override fun onPinChanged() {
        binding.errorText.isVisible = false
    }

    override fun onSecondaryActionClicked() {
        PrivateSpaceNavigator.openForgotPassword(requireActivity().supportFragmentManager)
    }

    companion object {
        fun newInstance() = PrivateSpacePinFragment()
    }
}
