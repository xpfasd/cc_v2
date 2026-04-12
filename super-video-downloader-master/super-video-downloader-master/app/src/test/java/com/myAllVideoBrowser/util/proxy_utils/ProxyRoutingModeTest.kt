package com.myAllVideoBrowser.util.proxy_utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProxyRoutingModeTest {

    @Test
    fun `direct connection stays enabled when proxy and doh are both off`() {
        assertFalse(
            ProxyRoutingMode.shouldEnableLocalProxy(
                isProxyEnabled = false,
                isDohEnabled = false
            )
        )
    }

    @Test
    fun `local proxy is enabled when explicit proxy is on`() {
        assertTrue(
            ProxyRoutingMode.shouldEnableLocalProxy(
                isProxyEnabled = true,
                isDohEnabled = false,
                isNativeProxyAvailable = true
            )
        )
    }

    @Test
    fun `local proxy is enabled when doh is on`() {
        assertTrue(
            ProxyRoutingMode.shouldEnableLocalProxy(
                isProxyEnabled = false,
                isDohEnabled = true,
                isNativeProxyAvailable = true
            )
        )
    }

    @Test
    fun `local proxy stays disabled when native proxy support is unavailable`() {
        assertFalse(
            ProxyRoutingMode.shouldEnableLocalProxy(
                isProxyEnabled = true,
                isDohEnabled = true,
                isNativeProxyAvailable = false
            )
        )
    }
}
