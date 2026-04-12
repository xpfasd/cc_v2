package com.myAllVideoBrowser.ui.main.bookmarks.dialogs

import androidx.fragment.app.FragmentManager

object BookmarksDialogLauncher {
    fun showAddBookmarkDialog(
        fragmentManager: FragmentManager,
        title: String? = null,
        url: String? = null
    ) {
        if (fragmentManager.findFragmentByTag(AddBookmarkDialogFragment.TAG) == null) {
            AddBookmarkDialogFragment.newInstance(title, url)
                .show(fragmentManager, AddBookmarkDialogFragment.TAG)
        }
    }
}
