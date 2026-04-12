package com.myAllVideoBrowser.ui.main.settings.password

import android.os.Bundle

class PasswordSetFragment : PasswordPinFragmentBase() {

    private var openPrivateSpaceOnFinish: Boolean = false
    private var resetPinOnly: Boolean = false

    override val screenTitle: String
        get() = if (resetPinOnly) "Reset Password" else "Set Password"
    override val screenPrompt: String
        get() = if (resetPinOnly) "Enter your new PIN code" else "Enter your new PIN code"
    override val screenSubtitle: String
        get() = if (resetPinOnly) {
            "Create a new PIN to regain access to your protected files"
        } else {
            "Using a PIN code enhances security and prevents unauthorized access to your sensitive information"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openPrivateSpaceOnFinish = arguments?.getBoolean(ARG_OPEN_PRIVATE_SPACE_ON_FINISH) == true
        resetPinOnly = arguments?.getBoolean(ARG_RESET_PIN_ONLY) == true
    }

    override fun onPinCompleted(pin: String) {
        PasswordFlowNavigator.goToConfirm(
            requireActivity().supportFragmentManager,
            pin,
            openPrivateSpaceOnFinish,
            resetPinOnly
        )
    }

    companion object {
        private const val ARG_OPEN_PRIVATE_SPACE_ON_FINISH = "arg_open_private_space_on_finish"
        private const val ARG_RESET_PIN_ONLY = "arg_reset_pin_only"

        fun newInstance(openPrivateSpaceOnFinish: Boolean = false) = PasswordSetFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_OPEN_PRIVATE_SPACE_ON_FINISH, openPrivateSpaceOnFinish)
            }
        }

        fun newResetPinInstance(openPrivateSpaceOnFinish: Boolean = true) =
            PasswordSetFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_OPEN_PRIVATE_SPACE_ON_FINISH, openPrivateSpaceOnFinish)
                    putBoolean(ARG_RESET_PIN_ONLY, true)
                }
            }
    }
}
