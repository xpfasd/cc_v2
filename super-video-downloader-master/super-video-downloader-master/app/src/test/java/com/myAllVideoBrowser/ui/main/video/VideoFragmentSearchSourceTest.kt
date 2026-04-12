package com.myAllVideoBrowser.ui.main.video

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoFragmentSearchSourceTest {

    @Test
    fun `header search icon opens search dialog in regular downloads mode`() {
        val source = File(
            "src/main/java/com/myAllVideoBrowser/ui/main/video/VideoFragment.kt"
        ).readText()

        val bindUiSection = source.substringAfter("private fun bindUi() {")
            .substringBefore("    private fun ensureVideoContentReady() {")

        assertTrue(
            "regular downloads mode should open the search dialog from iv_header_right",
            bindUiSection.contains("showSearchDialog()")
        )
        assertTrue(
            "private space mode should keep using the options popup",
            bindUiSection.contains("showPrivateSpaceOptionsPopup(it)")
        )
    }
}
