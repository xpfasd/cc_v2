package com.myAllVideoBrowser.ui.main.progress;

public final class ProgressPasteUiState {
    private final String hintText;
    private final boolean shouldParse;

    public ProgressPasteUiState(String hintText, boolean shouldParse) {
        this.hintText = hintText;
        this.shouldParse = shouldParse;
    }

    public String getHintText() {
        return hintText;
    }

    public boolean getShouldParse() {
        return shouldParse;
    }
}
