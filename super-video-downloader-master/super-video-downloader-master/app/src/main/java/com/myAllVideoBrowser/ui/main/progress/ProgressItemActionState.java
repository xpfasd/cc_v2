package com.myAllVideoBrowser.ui.main.progress;

public final class ProgressItemActionState {
    private final ProgressPrimaryAction primaryAction;
    private final boolean showPrimaryAction;

    public ProgressItemActionState(ProgressPrimaryAction primaryAction, boolean showPrimaryAction) {
        this.primaryAction = primaryAction;
        this.showPrimaryAction = showPrimaryAction;
    }

    public ProgressPrimaryAction getPrimaryAction() {
        return primaryAction;
    }

    public boolean getShowPrimaryAction() {
        return showPrimaryAction;
    }
}
