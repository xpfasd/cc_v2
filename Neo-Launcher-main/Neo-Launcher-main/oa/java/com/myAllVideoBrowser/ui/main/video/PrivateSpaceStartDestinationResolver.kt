package com.myAllVideoBrowser.ui.main.video

enum class PrivateSpaceStartDestination {
    AUTH,
    PASSWORD_SETUP
}

object PrivateSpaceStartDestinationResolver {
    fun resolve(hasPasswordPin: Boolean): PrivateSpaceStartDestination {
        return if (hasPasswordPin) {
            PrivateSpaceStartDestination.AUTH
        } else {
            PrivateSpaceStartDestination.PASSWORD_SETUP
        }
    }

    fun canRecoverPassword(hasSecurityQuestion: Boolean, hasSecurityAnswer: Boolean): Boolean {
        return hasSecurityQuestion && hasSecurityAnswer
    }
}
