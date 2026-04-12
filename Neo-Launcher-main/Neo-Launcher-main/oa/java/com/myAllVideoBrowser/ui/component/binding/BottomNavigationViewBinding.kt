package com.myAllVideoBrowser.ui.component.binding

import androidx.databinding.BindingAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.myAllVideoBrowser.R

object BottomNavigationViewBinding {

    @BindingAdapter("selectedItemId")
    @JvmStatic
    fun BottomNavigationView.setSelectedItemId(position: Int) {
        disableActiveIndicatorCompat()
        selectedItemId = when (position) {
            0 -> R.id.tab_browser
            1 -> R.id.tab_progress
            2 -> R.id.tab_video
            else -> R.id.tab_settings
        }
    }

    private fun BottomNavigationView.disableActiveIndicatorCompat() {
        runCatching {
            javaClass.getMethod("setItemActiveIndicatorEnabled", java.lang.Boolean.TYPE)
                .invoke(this, false)
        }
    }
}
