package com.myAllVideoBrowser.ui.main.base

import org.junit.Assert.assertEquals
import org.junit.Test

class PopupPositioningTest {

    @Test
    fun `anchor placement uses container relative coordinates`() {
        val placement = PopupPositioning.calculateAnchorPlacement(
            containerXOnScreen = 24,
            containerYOnScreen = 180,
            sourceXOnScreen = 312,
            sourceYOnScreen = 468,
            sourceWidth = 32,
            sourceHeight = 32
        )

        assertEquals(288, placement.leftMargin)
        assertEquals(288, placement.topMargin)
        assertEquals(32, placement.width)
        assertEquals(32, placement.height)
    }

    @Test
    fun `end aligned offset uses actual anchor width instead of magic number`() {
        val xOffset = PopupPositioning.calculateEndAlignedXOffset(
            anchorWidth = 32,
            popupWidth = 168
        )

        assertEquals(-136, xOffset)
    }
}
