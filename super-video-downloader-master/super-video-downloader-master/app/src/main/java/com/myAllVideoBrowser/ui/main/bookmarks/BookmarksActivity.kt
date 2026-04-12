package com.myAllVideoBrowser.ui.main.bookmarks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.commit
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.ui.main.base.BaseActivity
import com.myAllVideoBrowser.ui.main.home.browser.BrowserSelectionResultContract

class BookmarksActivity : BaseActivity(), BookmarksFragment.BookmarkSelectionHost {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.bookmarks_fragment_container, BookmarksFragment.newInstance())
            }
        }
    }

    override fun onBookmarkSelected(url: String, title: String?) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(BrowserSelectionResultContract.EXTRA_SELECTED_URL, url)
                putExtra(BrowserSelectionResultContract.EXTRA_SELECTED_TITLE, title)
            }
        )
        finish()
    }

    override fun onBookmarkClosed() {
        finish()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, BookmarksActivity::class.java)
        }
    }
}
