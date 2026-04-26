package com.android.launcher3.util;

final class SettingsValueReader {

    private SettingsValueReader() {}

    static boolean read(IntReader reader, String key, int defaultValue) {
        try {
            return reader.getInt(key, defaultValue) == 1;
        } catch (SecurityException e) {
            return defaultValue == 1;
        }
    }

    interface IntReader {
        int getInt(String key, int defaultValue);
    }
}
