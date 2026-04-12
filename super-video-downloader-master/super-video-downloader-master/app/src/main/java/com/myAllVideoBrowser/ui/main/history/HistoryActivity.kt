package com.myAllVideoBrowser.ui.main.history

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.commit
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.ui.main.base.BaseActivity
import com.myAllVideoBrowser.ui.main.home.browser.BrowserSelectionResultContract

class HistoryActivity : BaseActivity(), HistoryFragment.HistorySelectionHost {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.history_fragment_container, HistoryFragment.newInstance())
            }
        }
    }

    override fun onHistorySelected(url: String, title: String?) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(BrowserSelectionResultContract.EXTRA_SELECTED_URL, url)
                putExtra(BrowserSelectionResultContract.EXTRA_SELECTED_TITLE, title)
            }
        )
        finish()
    }

    override fun onHistoryClosed() {
        finish()
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, HistoryActivity::class.java)
        }
    }
}
