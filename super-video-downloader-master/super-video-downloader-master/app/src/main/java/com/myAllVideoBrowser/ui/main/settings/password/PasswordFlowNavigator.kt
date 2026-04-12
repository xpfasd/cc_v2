package com.myAllVideoBrowser.ui.main.settings.password

import androidx.fragment.app.FragmentManager
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.ui.main.video.PrivateSpaceNavigator

internal const val PASSWORD_FLOW_START = "password_flow_start"
internal const val PASSWORD_CONFIRM_TAG = "password_confirm"
internal const val SECURITY_QUESTION_TAG = "security_question"
internal const val SECURITY_QUESTION_DIALOG_TAG = "security_question_dialog"
internal const val PASSWORD_SUCCESS_DIALOG_TAG = "password_success_dialog"
internal const val SECURITY_QUESTION_RESULT_KEY = "security_question_result_key"
internal const val SECURITY_QUESTION_RESULT_VALUE = "security_question_result_value"

object PasswordFlowNavigator {
    fun start(fragmentManager: FragmentManager, openPrivateSpaceOnFinish: Boolean = false) {
        fragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container_view,
                PasswordSetFragment.newInstance(openPrivateSpaceOnFinish)
            )
            .addToBackStack(PASSWORD_FLOW_START)
            .commit()
    }

    fun startForPrivateSpace(fragmentManager: FragmentManager) {
        start(fragmentManager, true)
    }

    fun startChangePin(fragmentManager: FragmentManager) {
        startResetPin(fragmentManager, openPrivateSpaceOnFinish = false)
    }

    fun startResetPin(fragmentManager: FragmentManager, openPrivateSpaceOnFinish: Boolean = true) {
        fragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container_view,
                PasswordSetFragment.newResetPinInstance(openPrivateSpaceOnFinish)
            )
            .addToBackStack(PASSWORD_FLOW_START)
            .commit()
    }

    fun goToConfirm(
        fragmentManager: FragmentManager,
        pin: String,
        openPrivateSpaceOnFinish: Boolean = false,
        resetPinOnly: Boolean = false
    ) {
        fragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container_view,
                PasswordConfirmFragment.newInstance(pin, openPrivateSpaceOnFinish, resetPinOnly)
            )
            .addToBackStack(PASSWORD_CONFIRM_TAG)
            .commit()
    }

    fun goToSecurityQuestion(
        fragmentManager: FragmentManager,
        pin: String,
        openPrivateSpaceOnFinish: Boolean = false
    ) {
        fragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container_view,
                SecurityQuestionFragment.newInstance(pin, openPrivateSpaceOnFinish)
            )
            .addToBackStack(SECURITY_QUESTION_TAG)
            .commit()
    }

    fun finish(fragmentManager: FragmentManager) {
        fragmentManager.popBackStack(PASSWORD_FLOW_START, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun finishToPrivateSpace(fragmentManager: FragmentManager) {
        fragmentManager.popBackStackImmediate(
            null as String?,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
        PrivateSpaceNavigator.open(fragmentManager)
    }
}
