package com.myAllVideoBrowser.ui.main.progress;

import com.myAllVideoBrowser.util.downloaders.generic_downloader.models.VideoTaskState;

public final class ProgressItemActionStateFactory {

    private ProgressItemActionStateFactory() {
    }

    public static ProgressItemActionState create(int downloadStatus) {
        if (downloadStatus == VideoTaskState.PAUSE) {
            return new ProgressItemActionState(ProgressPrimaryAction.RESUME, true);
        }

        if (downloadStatus == VideoTaskState.PENDING
                || downloadStatus == VideoTaskState.PREPARE
                || downloadStatus == VideoTaskState.START
                || downloadStatus == VideoTaskState.DOWNLOADING
                || downloadStatus == VideoTaskState.PROXYREADY) {
            return new ProgressItemActionState(ProgressPrimaryAction.PAUSE, true);
        }

        return new ProgressItemActionState(ProgressPrimaryAction.NONE, false);
    }
}
