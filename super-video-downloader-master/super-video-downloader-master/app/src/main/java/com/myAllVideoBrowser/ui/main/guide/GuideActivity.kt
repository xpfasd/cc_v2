package com.myAllVideoBrowser.ui.main.guide

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.WindowCompat
import com.myAllVideoBrowser.databinding.ActivityGuideBinding
import com.myAllVideoBrowser.ui.main.base.BaseActivity

class GuideActivity : BaseActivity() {

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, GuideActivity::class.java)
    }

    private lateinit var binding: ActivityGuideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        binding = ActivityGuideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.headerBack.setOnClickListener {
            finish()
        }
    }
}
