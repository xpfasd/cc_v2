package com.myAllVideoBrowser.ui.main.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.ui.main.base.BaseActivity

class PrivacyPolicyActivity : BaseActivity() {

    private lateinit var privacyPolicyWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        privacyPolicyWebView = findViewById(R.id.privacyPolicyWebView)

        findViewById<android.view.View>(R.id.privacyPolicyBackButton).setOnClickListener {
            finish()
        }

        setupWebView()

        onBackPressedDispatcher.addCallback(this) {
            if (privacyPolicyWebView.canGoBack()) {
                privacyPolicyWebView.goBack()
            } else {
                finish()
            }
        }
    }

    private fun setupWebView() {
        privacyPolicyWebView.webViewClient = WebViewClient()
        privacyPolicyWebView.webChromeClient = WebChromeClient()
        privacyPolicyWebView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
        privacyPolicyWebView.loadUrl(PRIVACY_POLICY_URL)
    }

    override fun onDestroy() {
        if (this::privacyPolicyWebView.isInitialized) {
            privacyPolicyWebView.stopLoading()
            privacyPolicyWebView.destroy()
        }
        super.onDestroy()
    }

    companion object {
        private const val PRIVACY_POLICY_URL = "https://sites.google.com/view/vdd-privacy-policy"

        fun createIntent(context: Context): Intent {
            return Intent(context, PrivacyPolicyActivity::class.java)
        }
    }
}
