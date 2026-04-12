package com.myAllVideoBrowser.ui.main.base

import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import com.google.android.material.color.MaterialColors
import com.myAllVideoBrowser.R
import dagger.android.support.DaggerFragment

abstract class BaseFragment : DaggerFragment() {
    fun fixPopup(container: FrameLayout, popupSource: View): View {
        val myView = View(container.context)
        val containerLocation = IntArray(2)
        container.getLocationOnScreen(containerLocation)
        val location = IntArray(2)
        popupSource.getLocationOnScreen(location)
        val placement = PopupPositioning.calculateAnchorPlacement(
            containerXOnScreen = containerLocation[0],
            containerYOnScreen = containerLocation[1],
            sourceXOnScreen = location[0],
            sourceYOnScreen = location[1],
            sourceWidth = popupSource.width,
            sourceHeight = popupSource.height
        )
        val params = FrameLayout.LayoutParams(placement.width, placement.height)
        myView.visibility = View.INVISIBLE
        params.topMargin = placement.topMargin
        params.leftMargin = placement.leftMargin
        myView.layoutParams = params

        container.removeAllViews()
        container.addView(myView)

        return myView
    }

    fun getThemeBackgroundColor(): Int {
        val color =
            MaterialColors.getColor(
                requireContext(),
                com.google.android.material.R.attr.colorSurface,
                Color.YELLOW
            )
        return color
    }
}
