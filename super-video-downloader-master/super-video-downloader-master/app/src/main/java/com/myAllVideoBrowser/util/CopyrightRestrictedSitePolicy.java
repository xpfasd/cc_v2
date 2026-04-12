package com.myAllVideoBrowser.util;

public final class CopyrightRestrictedSitePolicy {

    private CopyrightRestrictedSitePolicy() {
    }

    public static boolean isDownloadRestrictedUrl(String url) {
        if (url == null) {
            return false;
        }

        String normalized = url.trim().toLowerCase();
        if (normalized.isEmpty()) {
            return false;
        }

        return normalized.contains("youtube.com")
                || normalized.contains("youtu.be")
                || normalized.contains("youtube-nocookie.com");
    }
}
