package com.myAllVideoBrowser.ui.main.base

data class PopupAnchorPlacement(
    val leftMargin: Int,
    val topMargin: Int,
    val width: Int,
    val height: Int
)

object PopupPositioning {

    fun calculateAnchorPlacement(
        containerXOnScreen: Int,
        containerYOnScreen: Int,
        sourceXOnScreen: Int,
        sourceYOnScreen: Int,
        sourceWidth: Int,
        sourceHeight: Int
    ): PopupAnchorPlacement {
        return PopupAnchorPlacement(
            leftMargin = sourceXOnScreen - containerXOnScreen,
            topMargin = sourceYOnScreen - containerYOnScreen,
            width = sourceWidth,
            height = sourceHeight
        )
    }

    fun calculateEndAlignedXOffset(anchorWidth: Int, popupWidth: Int): Int {
        return anchorWidth - popupWidth
    }
}
