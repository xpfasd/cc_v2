package com.myAllVideoBrowser.ui.main.home.browser.webTab

data class WebTabPreviewState(
    val showPreviewImage: Boolean,
    val showPlaceholder: Boolean
)

object WebTabPreviewStateFactory {
    fun create(previewBytes: ByteArray?, isHomeTab: Boolean): WebTabPreviewState {
        if (isHomeTab) {
            return WebTabPreviewState(
                showPreviewImage = false,
                showPlaceholder = false
            )
        }

        val hasPreview = previewBytes != null && previewBytes.isNotEmpty()
        val showPreview = hasPreview
        return WebTabPreviewState(
            showPreviewImage = showPreview,
            showPlaceholder = !showPreview
        )
    }
}
