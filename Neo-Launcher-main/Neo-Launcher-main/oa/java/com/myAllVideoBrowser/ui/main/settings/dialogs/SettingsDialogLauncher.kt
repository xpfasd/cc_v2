package com.myAllVideoBrowser.ui.main.settings.dialogs

import androidx.fragment.app.FragmentManager

object SettingsDialogLauncher {
    fun showPermissionDialog(fragmentManager: FragmentManager) {
        if (fragmentManager.findFragmentByTag(PermissionDialogFragment.TAG) == null) {
            PermissionDialogFragment.newInstance().show(fragmentManager, PermissionDialogFragment.TAG)
        }
    }

    fun showRatingDialog(fragmentManager: FragmentManager) {
        if (fragmentManager.findFragmentByTag(RatingDialogFragment.TAG) == null) {
            RatingDialogFragment.newInstance().show(fragmentManager, RatingDialogFragment.TAG)
        }
    }

    fun showFeedbackDialog(fragmentManager: FragmentManager) {
        if (fragmentManager.findFragmentByTag(FeedbackDialogFragment.TAG) == null) {
            FeedbackDialogFragment.newInstance().show(fragmentManager, FeedbackDialogFragment.TAG)
        }
    }

    fun showPositiveReviewDialog(fragmentManager: FragmentManager) {
        if (fragmentManager.findFragmentByTag(PositiveReviewDialogFragment.TAG) == null) {
            PositiveReviewDialogFragment.newInstance()
                .show(fragmentManager, PositiveReviewDialogFragment.TAG)
        }
    }
}

