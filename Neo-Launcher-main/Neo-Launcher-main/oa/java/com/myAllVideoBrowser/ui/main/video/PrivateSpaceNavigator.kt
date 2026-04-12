package com.myAllVideoBrowser.ui.main.video

import androidx.fragment.app.FragmentManager
import com.myAllVideoBrowser.R

internal const val PRIVATE_SPACE_PIN_TAG = "private_space_pin"
internal const val PRIVATE_SPACE_CONTENT_TAG = "private_space_content"
internal const val PRIVATE_SPACE_FORGOT_PASSWORD_TAG = "private_space_forgot_password"

object PrivateSpaceNavigator {
    fun startAuth(fragmentManager: FragmentManager) {
        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, PrivateSpacePinFragment.newInstance())
            .commit()
    }

    fun open(fragmentManager: FragmentManager) {
        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, VideoFragment.newPrivateSpaceInstance())
            .commit()
    }

    fun openForgotPassword(fragmentManager: FragmentManager) {
        fragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container_view,
                com.myAllVideoBrowser.ui.main.settings.password.SecurityQuestionFragment
                    .newVerifyForResetInstance()
            )
            .addToBackStack(PRIVATE_SPACE_FORGOT_PASSWORD_TAG)
            .commit()
    }

    fun finishAuthAndOpen(fragmentManager: FragmentManager) {
        open(fragmentManager)
    }
}
