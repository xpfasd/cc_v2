package com.myAllVideoBrowser.ui.main.progress;

public final class ProgressPasteUiStateFactory {

    private ProgressPasteUiStateFactory() {
    }

    public static ProgressPasteUiState createIdleState() {
        return new ProgressPasteUiState("", false);
    }

    public static ProgressPasteUiState createFromClipboard(CharSequence clipboardText) {
        String normalized = clipboardText == null ? "" : clipboardText.toString().trim();
        return new ProgressPasteUiState(normalized, normalized.startsWith("http"));
    }
}
