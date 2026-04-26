package com.android.launcher3.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SettingsCacheTest {

    @Test
    public void readSettingValue_returnsDefaultWhenSettingIsNotReadable() {
        boolean value = SettingsValueReader.read(
                (key, defaultValue) -> {
                    throw new SecurityException("Settings key is not readable");
                },
                "touchpad_natural_scrolling",
                0);

        assertFalse(value);
    }

    @Test
    public void readSettingValue_returnsReaderValueWhenReadable() {
        boolean value = SettingsValueReader.read(
                (key, defaultValue) -> 1,
                "touchpad_natural_scrolling",
                0);

        assertTrue(value);
    }
}
